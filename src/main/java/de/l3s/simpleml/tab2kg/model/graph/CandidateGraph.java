package de.l3s.simpleml.tab2kg.model.graph;

import java.util.HashMap;
import java.util.Map;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.graphcandidates.AttributeCandidatesSet;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;

public class CandidateGraph extends SimpleGraph {

	private int size;
	private Double sizeNormalised = null;
	private Double score = null;

	private AttributeCandidatesSet attributeCandidatesSet;

	private Map<Attribute, Attribute> columns = new HashMap<Attribute, Attribute>();

	public double getConfidenceScore() {
		return attributeCandidatesSet.getAverageConfidence();
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public AttributeCandidatesSet getAttributeCandidatesSet() {
		return attributeCandidatesSet;
	}

	public void setAttributeCandidatesSet(AttributeCandidatesSet attributeCandidatesSet) {
		this.attributeCandidatesSet = attributeCandidatesSet;
	}

	public Double getSizeScore() {
		return sizeNormalised;
	}

	public void setSizeNormalised(double sizeNormalised) {
		this.sizeNormalised = sizeNormalised;
	}

	public Map<Attribute, Attribute> getColumns() {
		return columns;
	}

	public void setColumns(Map<Attribute, Attribute> columns) {
		this.columns = columns;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}

	public void print() {
		System.out.println("Graph (size: " + this.getSizeScore() + ", confidence: " + this.getConfidenceScore() + " => "
				+ this.getScore() + ")");

		for (RDFNodeLiteralTriple literalTriple : this.getMiniSchema().getLiteralTriples()) {
			System.out.println("L: " + literalTriple.getSubject().getURI() + " " + literalTriple.getProperty().getURI()
					+ " - " + literalTriple.getId());
		}
		for (RDFNodeTriple classTriple : this.getMiniSchema().getClassTriples()) {
			System.out.println("C: " + classTriple.getSubject().getURI() + " " + classTriple.getProperty().getURI()
					+ " " + classTriple.getObject().getURI());
		}
	}

}
