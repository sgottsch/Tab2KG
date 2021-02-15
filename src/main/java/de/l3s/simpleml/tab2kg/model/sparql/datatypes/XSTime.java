package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class XSTime implements XSDataType {

	// Source:
	// https://stackoverflow.com/questions/26355985/regular-expression-for-ddmmyyyy-date-including-validation-for-leap-year/26356146#26356146
	// https://www.regextester.com/96683
	// TODO
	// private Pattern pattern = Pattern.compile("^hh:mm:ss$");

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_TIME;

	@Override
	public boolean matches(String value) {
		try {
			dateTimeFormatter.parse(value.replace(".000000", "").replace(" ", "T"));
		} catch (DateTimeParseException e) {
			return false;
		}
		return true;
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_TIME;
	}
	@Override
	public boolean isTemporal() {
		return true;
	}
}
