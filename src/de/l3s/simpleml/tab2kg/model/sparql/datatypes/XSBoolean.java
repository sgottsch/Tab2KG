package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

public class XSBoolean implements XSDataType {

	// private Pattern pattern = Pattern.compile("^true|false$",
	// Pattern.CASE_INSENSITIVE);

	@Override
	public boolean matches(String value) {
		String valueLow = value.toLowerCase();
		return valueLow.matches("true") || valueLow.matches("false");
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_BOOLEAN;
	}
	@Override
	public boolean isTemporal() {
		return false;
	}
}
