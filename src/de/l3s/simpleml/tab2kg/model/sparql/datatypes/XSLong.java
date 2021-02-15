package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

public class XSLong implements XSDataType {

	@Override
	public boolean matches(String value) {
		try {
			Long.valueOf(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_LONG;
	}
	@Override
	public boolean isTemporal() {
		return false;
	}
}
