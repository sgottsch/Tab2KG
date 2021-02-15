package de.l3s.simpleml.tab2kg.model.rdf;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

public class RDFNodeLiteralTriple extends RDFTriple {

	private Resource subject;

	private RDFLiteral object;

	public RDFNodeLiteralTriple(Resource subject, Property property, RDFLiteral literal) {
		super(property);
		this.subject = subject;
		this.object = literal;
	}

	public Resource getSubject() {
		return subject;
	}

	public RDFLiteral getObject() {
		return object;
	}

	public void setObject(RDFLiteral object) {
		this.object = object;
	}

	public RDFNodeLiteralTriple copy() {
		RDFNodeLiteralTriple copy = new RDFNodeLiteralTriple(this.subject, this.getProperty(), this.object);
		return copy;
	}
}
