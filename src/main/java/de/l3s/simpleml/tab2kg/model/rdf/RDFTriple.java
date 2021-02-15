package de.l3s.simpleml.tab2kg.model.rdf;

import org.apache.jena.rdf.model.Property;

public abstract class RDFTriple {

	private String id;

	private Property property;

	private int frequency;

	public RDFTriple(Property property) {
		super();
		this.property = property;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

}
