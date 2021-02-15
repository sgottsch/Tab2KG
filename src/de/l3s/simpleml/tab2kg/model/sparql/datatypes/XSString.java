package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

public class XSString implements XSDataType {

	// private Pattern pattern = Pattern.compile("^.*$");

	@Override
	public boolean matches(String value) {
		return true;
		// return pattern.matcher(value).matches();
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_STRING;
	}

	@Override
	public boolean isTemporal() {
		return false;
	}
}
