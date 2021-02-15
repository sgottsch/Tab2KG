package de.l3s.simpleml.tab2kg.graph;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.util.FileManager;

import de.l3s.simpleml.tab2kg.model.rdf.RDFLiteral;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.util.FileLoader;

public class TypeGraphBuilder {

	private Model model;

	private Map<Resource, Set<RDFNodeLiteralTriple>> literalRelationsBySubject = new HashMap<Resource, Set<RDFNodeLiteralTriple>>();

	private Map<Resource, Integer> subjectFrequencies = new HashMap<Resource, Integer>();

	public boolean initModel(String fileName) {
		return initModel(fileName, true);
	}

	public boolean initModel(String fileName, String type) {
		return initModel(fileName, type, true);
	}

	public boolean initModel(String fileName, String type, boolean printException) {
		if (fileName.endsWith(".html") || fileName.endsWith(".htm"))
			return false;

		try {
			System.out.println("Load model " + fileName + ", " + type);
			this.model = FileManager.get().loadModel(fileName, type);

		} catch (RiotException e) {
			if (printException) {
				System.out.println("Can't load model: " + e.getMessage());
				// e.printStackTrace();
			}
			return false;
		}

		// System.out.println("DONE");
		return true;

	}

	public boolean initModel(String fileName, boolean printException) {
		return initModel(fileName, null, printException);
	}

	public int collectLiteralRelations() {

		int numberOfLiteralRelations = 0;

		// ARQ.setExecutionLogging(Explain.InfoLevel.NONE);
		// Logger log = Logger.getLogger(TypeGraphBuilder.class);
		// log.setLevel(Level.OFF);

		String queryString;
		try {
			queryString = FileLoader.readResourceFileToString("queries/literal_relations2.sparql");

			Query query = QueryFactory.create(queryString);
			query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			QueryExecution queryExec = QueryExecutionFactory.create(query, model);
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();
				// GraphResource subjectType =
				// createGraphResource(s.getResource("subjectType"));

				Resource subject = s.getResource("subjectType");

				// TODO: objectType comes from group concat, because there could
				// be multiple object types (e.g. string and langString). We
				// only take one here. It is not used anyway?

				if (s.getLiteral("objectSample") == null) {
					System.out.println("Missing object sample in the following query: ");
					System.out.println(queryString);
				}

				RDFDatatype objectType = s.getLiteral("objectSample").getDatatype();
				int count = s.getLiteral("count").getInt();

				Property property = model.getProperty(s.getResource("property").getURI());
				RDFNodeLiteralTriple triple = new RDFNodeLiteralTriple(subject, property, new RDFLiteral(objectType));
				triple.setFrequency(count);
				numberOfLiteralRelations += 1;
				// subject.addLiteralRelation(triple);

				// subject.setFrequency(subject.getFrequency() + count);

				if (!subjectFrequencies.containsKey(subject))
					subjectFrequencies.put(subject, count);
				else
					subjectFrequencies.put(subject, subjectFrequencies.get(subject) + count);

				if (!literalRelationsBySubject.containsKey(subject))
					literalRelationsBySubject.put(subject, new HashSet<RDFNodeLiteralTriple>());

				literalRelationsBySubject.get(subject).add(triple);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return numberOfLiteralRelations;

	}

	public Model getModel() {
		return model;
	}

	public Set<RDFNodeLiteralTriple> getLiteralRelationsBySubject(Resource resource) {
		return literalRelationsBySubject.get(resource);
	}

	/**
	 * Simplifies a given graph in a way that only certain triples are allowed
	 * (e.g., no anonymous nodes).
	 * 
	 * @param fileName File name where to store the simplifies graph (int TTL)
	 * @return number of statements in the simplified graph
	 */
	public int createSimpleRDFGraph(String fileName) {

		Model newModel = ModelFactory.createDefaultModel();

		int numberOfStatements = 0;

		String queryString;

		try {
			queryString = FileLoader.readResourceFileToString("queries/simple_schema.sparql");

			Query query = QueryFactory.create(queryString);
			query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			QueryExecution queryExec = QueryExecutionFactory.create(query, model);
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();

				if (model.getProperty(s.getResource("p").getURI()) == null)
					continue;

				if (s.getResource("s").getURI() == null)
					continue;

				if (s.get("o").isLiteral()) {
					newModel.addLiteral(s.getResource("s"), model.getProperty(s.getResource("p").getURI()),
							s.get("o").asLiteral());
				} else if (s.get("o").isAnon()) {
					continue;
				} else {
					if (s.getResource("o").getURI() == null)
						continue;
					newModel.add(s.getResource("s"), model.getProperty(s.getResource("p").getURI()),
							s.getResource("o"));
				}

				int type = s.getLiteral("type").getInt();
				if (type != 3)
					numberOfStatements += 1;
			}

			FileOutputStream outputFile = null;
			try {
				outputFile = new FileOutputStream(fileName);
				RDFDataMgr.write(outputFile, newModel, Lang.TTL);
			} catch (FileNotFoundException e) {
				e.printStackTrace(System.err);
			} finally {
				try {
					outputFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		System.out.println(" number of lines: " + numberOfStatements + ".");

		return numberOfStatements;
	}

}
