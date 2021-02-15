package de.l3s.simpleml.tab2kg.graph;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.model.graph.MiniSchema;
import de.l3s.simpleml.tab2kg.model.rdf.RDFLiteral;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;
import de.l3s.simpleml.tab2kg.util.FileLoader;

public class MiniSchemaCreator {

	public static final int HISTOGRAM_SIZE = 10;

	public static final int NUMBER_OF_QUANTILES = 10;

	private TypeGraphBuilder graphBuilder;

	private MiniSchema miniSchema;

	private Map<String, Integer> uris = new HashMap<String, Integer>();

	public MiniSchemaCreator(TypeGraphBuilder graphBuilder) {
		super();
		this.graphBuilder = graphBuilder;
		this.miniSchema = new MiniSchema();
	}

	public MiniSchema createMiniSchema() {

		Model model = this.graphBuilder.getModel();

		// this.graphBuilder.showGraph();

		int connectionId = 0;
		try {
			String queryString = FileLoader.readResourceFileToString("queries/mini_schema.sparql");

			Query query = QueryFactory.create(queryString);
			query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			QueryExecution queryExec = QueryExecutionFactory.create(query, model);
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();

				RDFNodeTriple triple = new RDFNodeTriple(s.getResource("subjectType"),
						model.getProperty(s.getResource("property").getURI()), s.getResource("objectType"));
				triple.setId("c" + connectionId);
				connectionId += 1;
				this.miniSchema.addClassTriple(triple);

				// System.out.println(s.getResource("subjectType").getLocalName()
				// + " "
				// + s.getResource("property").getLocalName() + " " +
				// s.getResource("objectType").getLocalName());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		int literalRelationId = 0;
		try {
			String queryStringLiterals = FileLoader.readResourceFileToString("queries/mini_schema_literals.sparql");

			Query queryLiterals = QueryFactory.create(queryStringLiterals);
			queryLiterals.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			QueryExecution queryExec = QueryExecutionFactory.create(queryLiterals, model);
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();

				RDFNodeLiteralTriple triple = new RDFNodeLiteralTriple(s.getResource("subjectType"),
						model.getProperty(s.getResource("property").getURI()),
						new RDFLiteral(s.getLiteral("objectSample").getDatatype()));
				triple.setId("l" + literalRelationId);
				this.miniSchema.addLiteralTriple(triple);
				literalRelationId += 1;

				// System.out.println(s.getResource("subjectType").getLocalName()
				// + " "
				// + s.getResource("property").getLocalName() + " " +
				// s.getLiteral("objectSample").getDatatype());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return this.miniSchema;
	}

	public static Map<Resource, Set<Property>> identifyIdentitiferLiterals(Model model) {

		Map<Resource, Integer> instancesCounts = new HashMap<Resource, Integer>();

		try {
			String queryString = FileLoader.readResourceFileToString("queries/number_of_instances.sparql");

			Query query = QueryFactory.create(queryString);
			query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			QueryExecution queryExec = QueryExecutionFactory.create(query, model);
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();
				instancesCounts.put(s.getResource("class"), s.getLiteral("cnt").getInt());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<Resource, Map<Resource, Integer>> propertyCounts = new HashMap<Resource, Map<Resource, Integer>>();

		try {
			String queryString = FileLoader.readResourceFileToString("queries/number_of_properties_per_class.sparql");

			Query query = QueryFactory.create(queryString);
			query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			QueryExecution queryExec = QueryExecutionFactory.create(query, model);
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();
				if (!propertyCounts.containsKey(s.getResource("class")))
					propertyCounts.put(s.getResource("class"), new HashMap<Resource, Integer>());

				propertyCounts.get(s.getResource("class")).put(s.getResource("p"), s.getLiteral("cnt").getInt());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<Resource, Map<Resource, Integer>> uniquePropertyValueCounts = new HashMap<Resource, Map<Resource, Integer>>();

		try {
			String queryString = FileLoader
					.readResourceFileToString("queries/number_of_unique_properties_per_class.sparql");

			Query query = QueryFactory.create(queryString);
			query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			QueryExecution queryExec = QueryExecutionFactory.create(query, model);
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();
				if (!uniquePropertyValueCounts.containsKey(s.getResource("class")))
					uniquePropertyValueCounts.put(s.getResource("class"), new HashMap<Resource, Integer>());

				uniquePropertyValueCounts.get(s.getResource("class")).put(s.getResource("p"),
						s.getLiteral("cnt").getInt());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		Map<Resource, Set<Property>> idProperties = new HashMap<Resource, Set<Property>>();

		for (Resource type : instancesCounts.keySet()) {

			int instancesCount = instancesCounts.get(type);

			if (propertyCounts.get(type) != null)
				for (Resource property : propertyCounts.get(type).keySet()) {
					int propertyCount = propertyCounts.get(type).get(property);
					int uniquePropertyValueCount = uniquePropertyValueCounts.get(type).get(property);
					if (instancesCount == propertyCount && instancesCount == uniquePropertyValueCount) {
						if (!idProperties.containsKey(type)) {
							Property rdfProperty = model.getProperty(property.getURI());
							if (!idProperties.containsKey(rdfProperty))
								idProperties.put(type, new HashSet<Property>());
							idProperties.get(type).add(rdfProperty);
						}
					}
				}
		}

		return idProperties;
	}

	public Map<String, Integer> getURIs() {
		return uris;
	}

	public MiniSchema getMiniSchema() {
		return miniSchema;
	}

}
