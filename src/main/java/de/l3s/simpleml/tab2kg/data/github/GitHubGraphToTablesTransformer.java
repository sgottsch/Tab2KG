package de.l3s.simpleml.tab2kg.data.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.Row;
import de.l3s.simpleml.tab2kg.data.ModelFilesCreator;
import de.l3s.simpleml.tab2kg.data.semtab.SemTabTableCreator;
import de.l3s.simpleml.tab2kg.graph.DataTableFromInputGraphCreator;
import de.l3s.simpleml.tab2kg.graph.MiniSchemaCreator;
import de.l3s.simpleml.tab2kg.graph.TemplateCreator;
import de.l3s.simpleml.tab2kg.graph.TypeGraphBuilder;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.model.rdf.QueryGraph;
import de.l3s.simpleml.tab2kg.model.rdf.QueryTemplate;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClass;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClassLiteralTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClassTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFLiteral;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.model.template.TemplateTriple;
import de.l3s.simpleml.tab2kg.rml.RMLMappingCreator;
import de.l3s.simpleml.tab2kg.rml.RMLMappingExecutor;
import de.l3s.simpleml.tab2kg.rml.model.RMLMapping;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.MapUtil;

public class GitHubGraphToTablesTransformer {

	private TypeGraphBuilder graphBuilder;

	private static final double LITERAL_STOP_PROBABILITY = 0.25;
	private static final int QUERY_GRAPH_MINIMUM_FREQUENCY = 10;
	private static final int MAX_NUMBER_OF_LINES = 3000;

	private static final int MINIMUM_NUMBER_OF_ROWS = 10;

	private int threadNumber;

	int createTables(String simpleGraphFileName, int numberOfFiles, int maxNumberOfTemplateNodes, int threadNumber) {

		this.threadNumber = threadNumber;
		SimpleGraph inputGraph = new SimpleGraph(simpleGraphFileName);

		this.graphBuilder = new TypeGraphBuilder();
		System.out.println(this.threadNumber + ": Init model for " + simpleGraphFileName + ".");
		boolean successfull = this.graphBuilder.initModel(inputGraph.getFileName(), false);

		if (!successfull)
			return 0;

		int numberOfLiteralRelations = this.graphBuilder.collectLiteralRelations();
		if (numberOfLiteralRelations == 0) {
			System.out.println(this.threadNumber + ": No literal relations.");
			return 0;
		}

		System.out.println(this.threadNumber + ": Get query graphs.");
		Set<QueryGraph> queryGraphsWithoutLiterals = getQueryGraphsWithoutLiterals(maxNumberOfTemplateNodes);

		if (queryGraphsWithoutLiterals.isEmpty()) {
			System.out.println(this.threadNumber + ": queryGraphsWithoutLiterals empty.");
			return 0;
		}

		System.out.println(this.threadNumber + ": Select random query graphs.");
		// create more graphs than needed, so we skip erroneuos ones and still
		// get enough
		Set<QueryGraph> queryGraphs = selectRandomQueryGraphsWithLiterals(queryGraphsWithoutLiterals,
				10 * numberOfFiles);
		if (queryGraphs == null) {
			System.out.println(this.threadNumber + ": Could not create query graph with literal. Continue.");
			return 0;
		}

		int i = 0;
		String fileNamePrefix = simpleGraphFileName.substring(simpleGraphFileName.lastIndexOf("/") + 1);
		fileNamePrefix = fileNamePrefix.substring(0, fileNamePrefix.lastIndexOf("."));
		fileNamePrefix += "_" + System.currentTimeMillis() + "_";

		// fileNamePrefix = Config.getPath(FileLocation.SUB_AND_FILE_GRAPHS_FOLDER) +
		// fileNamePrefix;

		MiniSchemaCreator schemaCreator = new MiniSchemaCreator(this.graphBuilder);
		schemaCreator.createMiniSchema();

		Map<Resource, Set<Property>> identifiers = MiniSchemaCreator
				.identifyIdentitiferLiterals(this.graphBuilder.getModel());

		File graphsFolder = new File(
				Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_GRAPHS);
		File mappingsFolder = new File(
				Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_MAPPINGS);
		File tablesFolder = new File(
				Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_TABLES);
		File modelsFolder = new File(
				Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_MODELS);

		for (QueryGraph queryGraph : queryGraphs) {
			// ensure that the query graph covers both split graphs
			if (!coversQueryGraph(this.graphBuilder.getModel(), queryGraph))
				continue;

			DataTableFromInputGraphCreator dataTableFromInputGraphCreator = new DataTableFromInputGraphCreator(
					this.graphBuilder.getModel(), queryGraph);
			int numberOfLines = generateRandomNumberOfLines(queryGraph.getFrequency());

			dataTableFromInputGraphCreator.createQuery(numberOfLines);

			DataTable dataTable = dataTableFromInputGraphCreator.createDataTable(QUERY_GRAPH_MINIMUM_FREQUENCY,
					schemaCreator.getURIs());

			if (dataTable == null)
				continue;

			if (!fulfillsConstraints(dataTable))
				continue;

//			System.out.println(queryGraph.getStringRepresentation());
//			for (Row row : dataTable.getRows()) {
//				System.out.println(row.getValues());
//			}
//			System.out.println("=================");

			String folderName = fileNamePrefix + i;

			String tableFileName = tablesFolder + "/" + folderName + ".csv";
			String modelFileName = modelsFolder + "/" + folderName + ".csv.model.json";
			String mappingFileName = mappingsFolder + "/" + folderName + ".rml";
			String graphFileName = graphsFolder + "/" + folderName + ".ttl";

			dataTable.setFileName(tableFileName);

			RMLMapping rmlMapping = RMLMappingCreator.createMapping(queryGraph, identifiers,
					"../" + SemTabTableCreator.FOLDER_NAME_TABLES + "/" + folderName + ".csv");

			RMLMappingExecutor mappingExecutor = new RMLMappingExecutor(mappingFileName);

			// ~~~ write output ~~~

			// data table file
			DataTableFromInputGraphCreator.writeDataTableToFile(dataTable);

			// mapping file (RML)
			RMLMappingCreator.createMappingString(rmlMapping, mappingFileName);

			// file graph file
			boolean successfullFileCreation = mappingExecutor.run(graphFileName, null);

			if (successfullFileCreation)
				successfullFileCreation = ModelFilesCreator.createModelFile(dataTable.getFileName(), mappingFileName,
						modelFileName);

			if (!successfullFileCreation) {
				// model file creation failed. Delete files
				System.out.println(this.threadNumber + ": Delete " + tableFileName);
				try {
					if (Files.exists(Paths.get(tableFileName)))
						Files.delete(Paths.get(tableFileName));
					if (Files.exists(Paths.get(mappingFileName)))
						Files.delete(Paths.get(mappingFileName));
					if (Files.exists(Paths.get(graphFileName)))
						Files.delete(Paths.get(graphFileName));
					if (Files.exists(Paths.get(modelFileName)))
						Files.delete(Paths.get(modelFileName));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			i += 1;

			if (i == numberOfFiles)
				break;
		}

		return i;
	}

	private boolean coversQueryGraph(Model graph, QueryGraph queryGraph) {

		// covers literal triples
		for (RDFClassLiteralTriple literalTriple : queryGraph.getLiteralTriples()) {
			String queryString = literalTriple.toSPARQLQuery();

			Query query = QueryFactory.create(queryString);
			QueryExecution queryExec = QueryExecutionFactory.create(query, graph);

			boolean covered = queryExec.execAsk();

			if (!covered)
				return false;
		}

		// covers relation triples
		for (RDFClassTriple classTriple : queryGraph.getClassTriples()) {
			String queryString = classTriple.toSPARQLQuery();

			Query query = QueryFactory.create(queryString);
			QueryExecution queryExec = QueryExecutionFactory.create(query, graph);

			boolean covered = queryExec.execAsk();

			if (!covered)
				return false;
		}

		return true;
	}

	private int generateRandomNumberOfLines(int count) {

		int min = QUERY_GRAPH_MINIMUM_FREQUENCY;
		int max = (int) Math.ceil(0.8 * Math.min(MAX_NUMBER_OF_LINES, count));

		Random r = new Random();

		return r.nextInt((max - min) + 1) + min;
	}

	private Set<QueryGraph> selectRandomQueryGraphsWithLiterals(Set<QueryGraph> queryGraphsWithoutLiterals,
			int numberOfFiles) {

		Set<QueryGraph> queryGraphs = new HashSet<QueryGraph>();

		int totalFrequency = 0;
		for (QueryGraph queryGraph : queryGraphsWithoutLiterals)
			totalFrequency += queryGraph.getFrequency();

		List<QueryGraph> queryGraphsByFrequency = new ArrayList<QueryGraph>(totalFrequency);

		for (QueryGraph queryGraph : queryGraphsWithoutLiterals) {
			for (int i = 0; i < queryGraph.getFrequency(); i++) {
				queryGraphsByFrequency.add(queryGraph);
			}
		}

		Random r = new Random();
		int min = 0;
		int max = queryGraphsByFrequency.size() - 1;

		// System.out.println("Max: " + max);

		for (int i = 0; i < numberOfFiles; i++) {

			int numberOfLiterals = 0;

			// it could happen that the query graph cannot have any literals.
			// Try another one then.
			QueryGraph queryGraph = null;

			int tries = 0;
			// two literals at least
			while (numberOfLiterals <= 1) {
				int randomPosition = r.nextInt((max - min) + 1) + min;

				queryGraph = queryGraphsByFrequency.get(randomPosition).copy();

				numberOfLiterals = addLiterals(queryGraph);
				tries += 1;
				if (tries == 40)
					return null;
			}
			queryGraphs.add(queryGraph);

		}

		return queryGraphs;
	}

	private Set<QueryGraph> getQueryGraphsWithoutLiterals(int maximumNumberOfNodes) {

		TemplateCreator tc = new TemplateCreator();

		Map<Integer, Set<QueryTemplate>> queryGraphsBySize = tc.createQueryTemplates(maximumNumberOfNodes);

		Map<QueryGraph, Integer> queryGraphsByFrequency = new HashMap<QueryGraph, Integer>();

		int pruned = 0;
		Set<QueryTemplate> prunedGraphs = new HashSet<QueryTemplate>();
		for (int size : queryGraphsBySize.keySet()) {
			System.out.println(this.threadNumber + ": Size: " + size);
			for (QueryTemplate queryTemplate : queryGraphsBySize.get(size)) {

				Set<Resource> typeNodes = new HashSet<Resource>();

				if (prunedGraphs.contains(queryTemplate)) {
					pruned += 1;
					continue;
				}

				Query query = QueryFactory.create(queryTemplate.getQuery());
				// query.setPrefix("rdf:",
				// "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

				QueryExecution queryExec = QueryExecutionFactory.create(query, this.graphBuilder.getModel());
				ResultSet rs = queryExec.execSelect();

				while (rs.hasNext()) {

					QueryGraph queryGraph = new QueryGraph();

//					System.out.println(queryGraph.getStringRepresentation());
//					System.out.println("");

					queryGraph.setQueryTemplate(queryTemplate);

					QuerySolution s = rs.nextSolution();

					int frequency = s.getLiteral(queryTemplate.getCountVariableName().substring(1)).getInt();
					queryGraph.setFrequency(frequency);

					// only prune, if zero results. If e.g. only few results,
					// there could still be dependent trees with more results
					// (e.g. four vehicle types, but lots of records with one of
					// those four types)
					if (frequency == 0) {
						prunedGraphs.addAll(queryTemplate.getDependentQueryTemplates());
						continue;
					}

					Map<String, RDFClass> rdfClassesByPlaceHolders = new HashMap<String, RDFClass>();
					Map<String, Integer> typeCounts = new HashMap<String, Integer>();
					for (String type : queryTemplate.getTypePlaceHolders()) {
						Integer instance = typeCounts.get(type);
						if (!typeCounts.containsKey(type))
							instance = 0;
						typeCounts.put(type, instance + 1);

						RDFClass rdfClass = new RDFClass(
								this.graphBuilder.getModel().getResource(s.getResource(type.substring(1)).getURI()),
								type, instance);
						rdfClassesByPlaceHolders.put(type, rdfClass);
						queryGraph.addType(rdfClass);

						typeNodes.add(
								this.graphBuilder.getModel().getResource(s.getResource(type.substring(1)).getURI()));
					}

					for (TemplateTriple template : queryTemplate.getTemplateTriples()) {
						RDFClass subject = rdfClassesByPlaceHolders.get(template.getSubjectPlaceHolder());
						Property property = this.graphBuilder.getModel()
								.getProperty(s.getResource(template.getPredicatePlaceHolder()).getURI());
						RDFClass object = rdfClassesByPlaceHolders.get(template.getObjectPlaceHolder());

						queryGraph.addClassTriple(new RDFClassTriple(subject, property, object));
					}

					queryGraphsByFrequency.put(queryGraph, frequency);
				}

			}
		}

		System.out.println(this.threadNumber + ": Pruned: " + pruned);

		System.out.println(this.threadNumber + ": #Graphs: " + queryGraphsByFrequency.size());

		Set<QueryGraph> queryGraphs = new HashSet<QueryGraph>();
		for (QueryGraph graph : MapUtil.sortByValueDescending(queryGraphsByFrequency).keySet()) {
			if (graph.getFrequency() <= QUERY_GRAPH_MINIMUM_FREQUENCY)
				continue;

			queryGraphs.add(graph);
		}

		System.out.println(this.threadNumber + ": #Frequent graphs: " + queryGraphs.size());

		return queryGraphs;
	}

	private int addLiterals(QueryGraph queryGraph) {

		// for each leaf node: find at least one literal
		// for nodes within the graph: randomly choose literals
		Set<RDFClass> resourcesAsSubjects = new HashSet<RDFClass>();
		Set<RDFClass> resourcesAsObjects = new HashSet<RDFClass>();

		if (queryGraph.getClassTriples().isEmpty()) {
			resourcesAsSubjects.add(queryGraph.getTypes().get(0));
			resourcesAsObjects.add(queryGraph.getTypes().get(0));
		} else {
			for (RDFClassTriple triple : queryGraph.getClassTriples()) {
				resourcesAsSubjects.add(triple.getSubject());
				resourcesAsObjects.add(triple.getObject());
			}
		}

		Set<RDFClass> leafNodes = new HashSet<RDFClass>();
		Set<RDFClass> withinNodes = new HashSet<RDFClass>();

		if (queryGraph.getClassTriples().isEmpty()) {
			leafNodes.add(queryGraph.getTypes().get(0));
		} else {
			leafNodes.addAll(Sets.difference(resourcesAsSubjects, resourcesAsObjects));
			leafNodes.addAll(Sets.difference(resourcesAsObjects, resourcesAsSubjects));
			withinNodes.addAll(Sets.intersection(resourcesAsObjects, resourcesAsSubjects));
		}

		// for leaf nodes: choose 1 to max literals
		for (RDFClass resource : leafNodes) {
			addLiterals(queryGraph, resource);
		}

		// for within nodes: choose 0 to max literals
		for (RDFClass resource : withinNodes) {
			if (Math.random() > 0.5)
				addLiterals(queryGraph, resource);
		}

		return queryGraph.getLiteralTriples().size();
	}

	private void addLiterals(QueryGraph queryGraph, RDFClass resource) {
		int totalFrequency = 0;

		if (graphBuilder.getLiteralRelationsBySubject(resource.getResource()) != null)
			for (RDFNodeLiteralTriple literalRelation : graphBuilder
					.getLiteralRelationsBySubject(resource.getResource())) {
				totalFrequency += literalRelation.getFrequency();
			}
		// System.out.println("TOTALFREQ: " + totalFrequency);
		List<RDFNodeLiteralTriple> literalRelations = new ArrayList<RDFNodeLiteralTriple>(totalFrequency);

		if (graphBuilder.getLiteralRelationsBySubject(resource.getResource()) != null)
			for (RDFNodeLiteralTriple literalRelation : graphBuilder
					.getLiteralRelationsBySubject(resource.getResource())) {
				for (int i = 0; i < literalRelation.getFrequency(); i++) {
					literalRelations.add(literalRelation);
				}
			}
		Set<RDFNodeLiteralTriple> selectedLiteralRelations = new HashSet<RDFNodeLiteralTriple>();

		Random r = new Random();
		int min = 0;
		int max = literalRelations.size() - 1;

		if (graphBuilder.getLiteralRelationsBySubject(resource.getResource()) != null)
			for (int i = 0; i < graphBuilder.getLiteralRelationsBySubject(resource.getResource()).size(); i++) {
				int randomPosition = r.nextInt((max - min) + 1) + min;
				selectedLiteralRelations.add(literalRelations.get(randomPosition));
				// with a given probabilty: don't add more relations
				if (Math.random() <= LITERAL_STOP_PROBABILITY)
					break;
			}

		for (RDFNodeLiteralTriple literalRelation : selectedLiteralRelations) {
			RDFClassLiteralTriple literalClassRelation = new RDFClassLiteralTriple(resource,
					literalRelation.getProperty(), new RDFLiteral(literalRelation.getObject().getDataType()));
			queryGraph.addLiteralTriple(literalClassRelation);
		}

	}

	public boolean fulfillsConstraints(DataTable dataTable) {

		// enough rows
		if (dataTable.getRows().size() < MINIMUM_NUMBER_OF_ROWS) {
			System.out.println(this.threadNumber + ": Not enough rows.");
			return false;
		}

		// two columns at least
		if (dataTable.getAttributes().size() < 2) {
			System.out.println(this.threadNumber + ": Not enough attributes.");
			return false;
		}

		// no column with null values only
		for (Attribute column1 : dataTable.getAttributes()) {
			boolean nullValueOnly = true;
			for (Row row : dataTable.getRows()) {
				String value = row.getValues().get(column1.getColumnIndex());
				if (value != null && !value.isEmpty()) {
					nullValueOnly = false;
					break;
				}
			}
			if (nullValueOnly) {
				System.out.println(this.threadNumber + ": Column with null values only.");
				return false;
			}
		}

		// no column with identical values
		for (Attribute column1 : dataTable.getAttributes()) {
			col2Loop: for (Attribute column2 : dataTable.getAttributes()) {
				if (column1 == column2)
					continue;

				for (Row row : dataTable.getRows()) {
					if (!row.getValues().get(column1.getColumnIndex())
							.equals(row.getValues().get(column2.getColumnIndex()))) {
						continue col2Loop;
					}
				}

				System.out.println(this.threadNumber + ": Column with same values.");
				return false;
			}
		}

		return true;
	}

}
