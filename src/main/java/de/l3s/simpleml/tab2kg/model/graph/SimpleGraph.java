package de.l3s.simpleml.tab2kg.model.graph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;

public class SimpleGraph {

	private String fileName;

	private List<Attribute> attributes = new ArrayList<Attribute>();

	private MiniSchema miniSchema;

	private Model model;

	private Map<String, Double> correlationScores;

	public SimpleGraph() {
		super();
	}

	public SimpleGraph(String fileName) {
		super();
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public MiniSchema getMiniSchema() {
		return miniSchema;
	}

	public void setMiniSchema(MiniSchema miniSchema) {
		this.miniSchema = miniSchema;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Attribute> attributes) {
		this.attributes = attributes;
	}

	public Attribute getAttributeBySubjectClassAndPredicateURL(String subjectClassURI, String predicateURI) {

		for (Attribute attribute : this.attributes) {
			if (attribute.getPredicateURI().equals(predicateURI)
					&& attribute.getSubjectClassURI().equals(subjectClassURI))
				return attribute;
		}

		return null;
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}

	public Map<String, Double> getCorrelationScores() {
		return correlationScores;
	}

	public void setCorrelationScores(Map<String, Double> correlationScores) {
		this.correlationScores = correlationScores;
	}

}
