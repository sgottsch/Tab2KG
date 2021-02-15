package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

public class XSInteger implements XSDataType {

	@Override
	public boolean matches(String value) {
		try {
			Integer.valueOf(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_INTEGER;
	}
	@Override
	public boolean isTemporal() {
		return false;
	}
}
