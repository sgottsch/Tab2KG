package de.l3s.simpleml.tab2kg.rml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.graph.TypeGraphBuilder;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;
import de.l3s.simpleml.tab2kg.util.FileLoader;

public class RMLMappingReader {

	private TypeGraphBuilder graphBuilder;

	public List<ColumnLiteralMapping> getMappings(DataTable dataTable, SimpleGraph simpleGraph,
			String mappingFileName) {
		List<ColumnLiteralMapping> mappings;
		try {
			mappings = getMappings(mappingFileName);

			for (ColumnLiteralMapping mapping : mappings) {

				// identify column
				mapping.setColumn(dataTable.getAttributeByIdentifier(mapping.getColumnId()));

				// identify graph attribute
				mapping.setAttribute(simpleGraph.getAttributeBySubjectClassAndPredicateURL(
						mapping.getSubjectClass().getURI(), mapping.getProperty().getURI()));
			}

			return mappings;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public List<RDFNodeTriple> getClassRelations(DataTable dataTable, SimpleGraph simpleGraph) {

		List<RDFNodeTriple> relations = new ArrayList<RDFNodeTriple>();

		try {
			String queryString = FileLoader.readResourceFileToString("queries/rml_mapping_class_relations.sparql");

			Query query = QueryFactory.create(queryString);
			QueryExecution queryExec = QueryExecutionFactory.create(query, this.graphBuilder.getModel());
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();

				Property property = this.graphBuilder.getModel().getProperty(s.getResource("predicate").getURI());
				Resource subjectClass = s.getResource("subject");
				Resource objectClass = s.getResource("object");

				RDFNodeTriple classRelation = new RDFNodeTriple(subjectClass, property, objectClass);
				relations.add(classRelation);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return relations;
	}

	public List<ColumnLiteralMapping> getMappings(String fileName) throws IOException {

		if (this.graphBuilder == null) {
			this.graphBuilder = new TypeGraphBuilder();
			if (!this.graphBuilder.initModel(fileName, "ttl"))
				return null;
		}

		List<ColumnLiteralMapping> mappings = new ArrayList<ColumnLiteralMapping>();

		String queryString = FileLoader.readResourceFileToString("queries/rml_mapping.sparql");

		Query query = QueryFactory.create(queryString);
		query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		QueryExecution queryExec = QueryExecutionFactory.create(query, graphBuilder.getModel());
		ResultSet rs = queryExec.execSelect();

		while (rs.hasNext()) {
			QuerySolution s = rs.nextSolution();

			String columnNumber = s.getLiteral("columnId").getString();
			Resource property = s.getResource("predicate");
			Resource subjectClass = s.getResource("class");

			ColumnLiteralMapping mapping = new ColumnLiteralMapping(columnNumber, property, subjectClass);

			mappings.add(mapping);
		}

		return mappings;
	}

	public List<RDFNodeTriple> getClassMappings(String fileName) {
		TypeGraphBuilder graphBuilder = new TypeGraphBuilder();

		graphBuilder.initModel(fileName, "ttl");

		List<RDFNodeTriple> relations = new ArrayList<RDFNodeTriple>();

		try {
			String queryString = FileLoader.readResourceFileToString("queries/rml_mapping_class_relations.sparql");

			Query query = QueryFactory.create(queryString);
			query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
			QueryExecution queryExec = QueryExecutionFactory.create(query, graphBuilder.getModel());
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();

				Property property = graphBuilder.getModel().getProperty(s.getResource("predicate").getURI());
				Resource subjectClass = s.getResource("subject");
				Resource objectClass = s.getResource("object");

				RDFNodeTriple classRelation = new RDFNodeTriple(subjectClass, property, objectClass);
				relations.add(classRelation);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return relations;
	}

}
