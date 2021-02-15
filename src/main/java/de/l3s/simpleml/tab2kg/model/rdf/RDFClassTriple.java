package de.l3s.simpleml.tab2kg.model.rdf;

import java.io.IOException;

import org.apache.jena.rdf.model.Property;

import de.l3s.simpleml.tab2kg.util.FileLoader;

public class RDFClassTriple extends RDFTriple {

	private RDFClass subject;

	private RDFClass object;

	public RDFClassTriple(RDFClass subject, Property property, RDFClass object) {
		super(property);
		this.subject = subject;
		this.object = object;
	}

	public RDFClass getSubject() {
		return subject;
	}

	public RDFClass getObject() {
		return object;
	}

	public String toSPARQLQuery() {

		try {
			String queryString = FileLoader.readResourceFileToString("queries/class_relation_existence.sparql");
			queryString = queryString.replace("@property@", getProperty().getURI());
			queryString = queryString.replace("@type1@", this.subject.getResource().getURI());
			queryString = queryString.replace("@type2@", this.object.getResource().getURI());
			return queryString;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

}
