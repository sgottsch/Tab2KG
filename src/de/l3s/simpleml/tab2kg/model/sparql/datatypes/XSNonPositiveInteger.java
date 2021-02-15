package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

public class XSNonPositiveInteger implements XSDataType {

	@Override
	public boolean matches(String value) {
		try {
			int intValue = Integer.valueOf(value);
			// Ganzzahlen beliebiger Länge, die negativ oder 0 sind
			return intValue <= 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_NON_POSITIVE_INTEGER;
	}
	@Override
	public boolean isTemporal() {
		return false;
	}
}
