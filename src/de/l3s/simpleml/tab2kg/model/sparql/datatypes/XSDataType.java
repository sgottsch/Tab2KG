package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

public interface XSDataType {

	boolean matches(String value);
	
	boolean isTemporal();

	DataTypeClass getRDFClass();

}
