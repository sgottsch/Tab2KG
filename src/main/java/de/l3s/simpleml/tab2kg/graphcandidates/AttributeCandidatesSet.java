package de.l3s.simpleml.tab2kg.graphcandidates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;

public class AttributeCandidatesSet {

	private List<ColumnCandidate> attributes = new ArrayList<ColumnCandidate>();

	private Set<String> classes = new HashSet<String>();

	private Double averageConfidence;

	public List<ColumnCandidate> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<ColumnCandidate> attributes) {
		this.attributes = attributes;
	}

	public void addCandidate(ColumnCandidate attribute) {
		this.attributes.add(attribute);
	}

	public double getAverageConfidence() {
		if (this.averageConfidence == null) {
			double sum = 0;
			for (ColumnCandidate candidate : attributes)
				sum += candidate.getConfidence();
			this.averageConfidence = sum / attributes.size();
		}
		return this.averageConfidence;
	}

	public void addClass(String classURI) {
		this.classes.add(classURI);
	}

	public int getSize() {
		return this.classes.size();
	}

	public Attribute getColumnyByAttribute(Attribute attribute) {
		for (ColumnCandidate cc : this.attributes) {
			if (cc.getAttribute() == attribute)
				return cc.getColumn();
		}
		return null;
	}

}
