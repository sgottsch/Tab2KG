package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

public class XSNegativeInteger implements XSDataType {

	@Override
	public boolean matches(String value) {
		try {
			int intValue = Integer.valueOf(value);
			// Streng negative Ganzzahlen beliebiger Länge
			return intValue < 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_NEGATIVE_INTEGER;
	}
	@Override
	public boolean isTemporal() {
		return false;
	}
}
