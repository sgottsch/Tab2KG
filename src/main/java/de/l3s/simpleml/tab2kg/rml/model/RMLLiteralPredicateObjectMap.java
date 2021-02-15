package de.l3s.simpleml.tab2kg.rml.model;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.rdf.model.Property;

public class RMLLiteralPredicateObjectMap {

	private Property property;

	private String columnId;

	private RDFDatatype dataType;

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

	public RDFDatatype getDataType() {
		return dataType;
	}

	public void setDataType(RDFDatatype dataType) {
		this.dataType = dataType;
	}

}
