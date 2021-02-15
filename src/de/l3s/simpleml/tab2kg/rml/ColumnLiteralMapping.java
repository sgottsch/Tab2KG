package de.l3s.simpleml.tab2kg.rml;

import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;

public class ColumnLiteralMapping {

	private String columnId;

	private Resource property;

	private Resource subjectClass;

	private Attribute column;

	private Attribute attribute;

	public ColumnLiteralMapping(Attribute column, Attribute attribute) {
		super();
		this.column = column;
		this.attribute = attribute;
	}

	public ColumnLiteralMapping(String columnId, Resource property, Resource subjectClass) {
		super();
		this.columnId = columnId;
		this.property = property;
		this.subjectClass = subjectClass;
	}

	public String getColumnId() {
		return columnId;
	}

	public void setColumnId(String columnId) {
		this.columnId = columnId;
	}

	public Resource getProperty() {
		return property;
	}

	public void setProperty(Resource property) {
		this.property = property;
	}

	public Resource getSubjectClass() {
		return subjectClass;
	}

	public void setSubjectClass(Resource subjectClass) {
		this.subjectClass = subjectClass;
	}

	public Attribute getColumn() {
		return column;
	}

	public void setColumn(Attribute column) {
		this.column = column;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

}
