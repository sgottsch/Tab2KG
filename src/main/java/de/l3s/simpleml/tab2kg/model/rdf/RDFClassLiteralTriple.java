package de.l3s.simpleml.tab2kg.model.rdf;

import java.io.IOException;

import org.apache.jena.rdf.model.Property;

import de.l3s.simpleml.tab2kg.util.FileLoader;

/**
 * A relation between a class instance and a literal.
 */
public class RDFClassLiteralTriple extends RDFTriple {

	private RDFClass subject;

	private RDFLiteral object;

	public RDFClassLiteralTriple(RDFClass subject, Property property, RDFLiteral literal) {
		super(property);
		this.subject = subject;
		this.object = literal;
	}

	public RDFClass getSubject() {
		return subject;
	}

	public RDFLiteral getObject() {
		return object;
	}

	public void setObject(RDFLiteral object) {
		this.object = object;
	}

	public void setSubject(RDFClass subject) {
		this.subject = subject;
	}

	public String toSPARQLQuery() {

		try {
			String queryString = FileLoader.readResourceFileToString("queries/literal_relation_existence.sparql");
			queryString = queryString.replace("@property@", getProperty().getURI());
			queryString = queryString.replace("@type@", this.subject.getResource().getURI());
			return queryString;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
