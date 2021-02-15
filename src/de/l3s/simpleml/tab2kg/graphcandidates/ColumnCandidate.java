package de.l3s.simpleml.tab2kg.graphcandidates;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;

public class ColumnCandidate {

	private Attribute column;

	private Attribute attribute;

	private double confidence;

	public ColumnCandidate(Attribute column, Attribute attribute, double confidence) {
		super();
		this.column = column;
		this.attribute = attribute;
		this.confidence = confidence;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public double getConfidence() {
		return confidence;
	}

	public Attribute getColumn() {
		return column;
	}

	public void setColumn(Attribute column) {
		this.column = column;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

}
