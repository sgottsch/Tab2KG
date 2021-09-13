package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

import de.l3s.simpleml.tab2kg.model.sparql.Prefix;

public enum DataTypeClass {

	XS_NEGATIVE_INTEGER("negativeInteger", Prefix.XSD), XS_NON_NEGATIVE_INTEGER("nonNegativeInteger", Prefix.XSD),
	XS_POSITIVE_INTEGER("positiveInteger", Prefix.XSD), XS_NON_POSITIVE_INTEGER("nonPositiveInteger", Prefix.XSD),
	XS_LONG("long", Prefix.XSD), XS_INTEGER("integer", Prefix.XSD), XS_INT("int", Prefix.XSD),
	XS_STRING("string", Prefix.XSD), RDF_LANG_STRING("langString", Prefix.RDF), XS_ANY_URI("anyURI", Prefix.XSD),
	XS_BOOLEAN("boolean", Prefix.XSD), XS_DOUBLE("double", Prefix.XSD), XS_DECIMAL("decimal", Prefix.XSD),
	XS_DATE("date", Prefix.XSD), XS_TIME("time", Prefix.XSD), XS_BYTE("byte", Prefix.XSD),
	XS_DATE_TIME("dateTime", Prefix.XSD), SML_WKT("wellKnownText", Prefix.ANON),
	SML_WKB("wellKnownBinary", Prefix.ANON), XS_FLOAT("float", Prefix.XSD), SML_VECTOR("vector", Prefix.ANON);

	private String name;
	private Prefix prefix;

	private DataTypeClass(String name, Prefix prefix) {
		this.name = name;
		this.prefix = prefix;
	}

	public String getPrefixedName() {
		return this.prefix.getPrefix() + this.name;
	}

	public String getPrefixedName(Prefix basePrefix) {
		if (this.prefix == basePrefix)
			return "<" + this.name + ">";
		else
			return this.prefix.getPrefix() + this.name;
	}

	public String getURL() {
		return this.prefix.getUrl() + this.name;
	}

	public String getName() {
		return name;
	}

	public Prefix getPrefix() {
		return prefix;
	}

	public static DataTypeClass getDataTypeClassByURI(String uri) {
		String uriLowerCase = uri.toLowerCase();
		for (DataTypeClass dataTypeClass : values()) {
			if (dataTypeClass.getURL().toLowerCase().equals(uriLowerCase))
				return dataTypeClass;
		}
		return null;
	}

}
