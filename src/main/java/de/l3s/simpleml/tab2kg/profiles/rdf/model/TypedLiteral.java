package de.l3s.simpleml.tab2kg.profiles.rdf.model;

import de.l3s.simpleml.tab2kg.model.sparql.Prefix;

public class TypedLiteral {

	private Prefix dataTypePrefix;
	private String dataType;
	private String value;

	public TypedLiteral(Prefix dataTypePrefix, String dataType, String value) {
		super();
		this.dataTypePrefix = dataTypePrefix;
		this.dataType = dataType;
		this.value = value;
	}

	public Prefix getDataTypePrefix() {
		return dataTypePrefix;
	}

	public String getString() {
		return "\"" + value + "\"" + "^^" + dataTypePrefix.getPrefix() + dataType;
	}

}
