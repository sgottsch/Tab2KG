package de.l3s.simpleml.tab2kg.rml.model;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.model.rdf.RDFClass;

public class RMLPredicateObjectMap {

	private Property property;

	private String columnId;

	private RDFClass object;
	private Resource objectResource;

	private String identifier;

	public Property getProperty() {
		return property;
	}

	public void setProperty(Property property) {
		this.property = property;
	}

	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	public RDFClass getObject() {
		return object;
	}

	public void setObject(RDFClass object) {
		this.object = object;
	}

	public Resource getObjectResource() {
		return objectResource;
	}

	public void setObjectResource(Resource objectResource) {
		this.objectResource = objectResource;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}
