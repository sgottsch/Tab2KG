package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

public class XSDouble implements XSDataType {

	@Override
	public boolean matches(String value) {
		try {
			Double.valueOf(value);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_DOUBLE;
	}
	@Override
	public boolean isTemporal() {
		return false;
	}
}
