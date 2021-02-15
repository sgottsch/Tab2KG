package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

import java.util.regex.Pattern;

public class XSFloat implements XSDataType {

	private Pattern pattern = Pattern.compile("^-?(0|[1-9][0-9]*)(\\.[0-9]+)?$");

	@Override
	public boolean matches(String value) {
		return pattern.matcher(value).matches();
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_FLOAT;
	}
	@Override
	public boolean isTemporal() {
		return false;
	}
}
