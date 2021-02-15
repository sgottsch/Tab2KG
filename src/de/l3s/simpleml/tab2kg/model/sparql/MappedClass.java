package de.l3s.simpleml.tab2kg.model.sparql;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntResource;

public class MappedClass {

	private Integer instanceNumber;
	private OntClass targetClass;
	private OntResource subject;

	public MappedClass(OntClass targetClass, Integer instanceNumber, OntResource subject) {
		super();
		this.instanceNumber = instanceNumber;
		this.targetClass = targetClass;
		this.subject = subject;
	}

	public Integer getInstanceNumber() {
		return instanceNumber;
	}

	public OntClass getTargetClass() {
		return targetClass;
	}

	public OntResource getSubject() {
		return subject;
	}

	public MappedClass copy() {
		return new MappedClass(this.targetClass, this.instanceNumber, this.subject);
	}

}
