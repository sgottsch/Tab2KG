package de.l3s.simpleml.tab2kg.model.rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class RDFNodeTriple extends RDFTriple {

	private Resource subject;

	private Resource object;

	public RDFNodeTriple(Resource subject, Property property, Resource object) {
		super(property);
		this.subject = subject;
		this.object = object;
	}

	public Resource getSubject() {
		return subject;
	}

	public Resource getObject() {
		return object;
	}

	public RDFNodeTriple copy() {
		return new RDFNodeTriple(subject, getProperty(), object);
	}

	public String getString() {
		return this.subject.getURI() + "-" + this.getProperty().getURI() + "-" + this.getObject().getURI();
	}

}
