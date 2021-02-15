package de.l3s.simpleml.tab2kg.evaluation;

import java.util.List;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.model.graph.CandidateGraph;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;
import de.l3s.simpleml.tab2kg.rml.ColumnLiteralMapping;
import de.l3s.simpleml.tab2kg.rml.RMLMappingReader;

public class TableMappingEvaluator {

	private List<RDFNodeTriple> classRelations;
	private DataTable dataTable;
	private SimpleGraph simpleGraph;

	public TableMappingEvaluator(DataTable dataTable, SimpleGraph simpleGraph) {
		this.dataTable = dataTable;
		this.simpleGraph = simpleGraph;
	}

	public void annotateTable(String mappingFileName) {

		System.out.println("Original mapping (" + mappingFileName + ":");

		RMLMappingReader rmlReader = new RMLMappingReader();
		List<ColumnLiteralMapping> mappings = rmlReader.getMappings(dataTable, simpleGraph, mappingFileName);
		this.classRelations = rmlReader.getClassRelations(dataTable, simpleGraph);

		for (Attribute column : dataTable.getAttributes()) {
			for (Attribute attribute : simpleGraph.getAttributes()) {
				for (ColumnLiteralMapping mapping : mappings) {
					if (mapping.getColumn() == column && mapping.getAttribute() == attribute) {
						column.setRepresentedAttribute(attribute);

						System.out.println(column.getIdentifier() + " -> " + attribute.getSubjectClassURI() + " "
								+ attribute.getPredicateURI());

						break;
					}
				}
			}
		}

		for (RDFNodeTriple classRelation : this.classRelations) {
			System.out.println(classRelation.getSubject().getURI() + "---" + classRelation.getProperty().getURI()
					+ "---" + classRelation.getObject().getURI());
		}

	}

	public SingleEvaluationResult evaluate(CandidateGraph resultGraph, SimpleGraph simpleGraph) {
		SingleEvaluationResult evaluationResult = new SingleEvaluationResult();

		// map old and new attribute representation together
		for (Attribute attribute : resultGraph.getColumns().keySet()) {
			for (Attribute attributeOriginal : simpleGraph.getAttributes()) {
				if (attribute.getSubjectClassURI().equals(attributeOriginal.getSubjectClassURI())
						&& attribute.getPredicateURI().equals(attributeOriginal.getPredicateURI())) {
					resultGraph.getColumns().get(attribute).setPredictedAttribute(attributeOriginal);
				}
			}
		}

		boolean correct = true;
		for (Attribute column : this.dataTable.getAttributes()) {
			if (column.getPredictedAttribute() == column.getRepresentedAttribute())
				evaluationResult.incrementCorrectLiteralRelations();
			else {
				correct = false;
				evaluationResult.incrementWrongLiteralRelations();
			}
		}

		classRelationLoop1: for (RDFNodeTriple classTriplePredicted : resultGraph.getMiniSchema().getClassTriples()) {
			for (RDFNodeTriple classTripleGraph : classRelations) {
				if (classTriplePredicted.getSubject().getURI().equals(classTripleGraph.getSubject().getURI())
						&& classTriplePredicted.getProperty().getURI().equals(classTripleGraph.getProperty().getURI())
						&& classTriplePredicted.getObject().getURI().equals(classTripleGraph.getObject().getURI())) {
					evaluationResult.incrementCorrectClassRelations();
					continue classRelationLoop1;
				}
			}
			correct = false;
		}

		classRelationLoop2: for (RDFNodeTriple classTripleGraph : classRelations) {
			for (RDFNodeTriple classTriplePredicted : resultGraph.getMiniSchema().getClassTriples()) {
				if (classTriplePredicted.getSubject().getURI().equals(classTripleGraph.getSubject().getURI())
						&& classTriplePredicted.getProperty().getURI().equals(classTripleGraph.getProperty().getURI())
						&& classTriplePredicted.getObject().getURI().equals(classTripleGraph.getObject().getURI())) {
					continue classRelationLoop2;
				}
			}
			System.out.println("Missing class relation: " + classTripleGraph.getSubject().getURI() + " "
					+ classTripleGraph.getObject().getURI());
			evaluationResult.incrementWrongClassRelations();
			correct = false;
		}

		evaluationResult.setNumberOfClassRelations(this.classRelations.size());

		evaluationResult.setCorrect(correct);
		return evaluationResult;
	}

	public List<RDFNodeTriple> getClassRelations() {
		return classRelations;
	}

	// public void annotateWithPredictedGraph(CandidateGraph resultGraph) {
	// for (Attribute attribute : resultGraph.getColumns().keySet()) {
	// resultGraph.getColumns().get(attribute).setPredictedAttribute(attribute);
	// }
	// }

}
