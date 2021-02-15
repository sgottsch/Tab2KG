package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class XSDate implements XSDataType {

	// Source:
	// https://stackoverflow.com/questions/26355985/regular-expression-for-ddmmyyyy-date-including-validation-for-leap-year/26356146#26356146
	// https://www.regextester.com/96683
	// private Pattern pattern =
	// Pattern.compile("^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$");

	private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE;

	@Override
	public boolean matches(String value) {
		try {
			dateTimeFormatter.parse(value);
		} catch (DateTimeParseException e) {
			return false;
		}
		return true;
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_DATE;
	}
	@Override
	public boolean isTemporal() {
		return true;
	}
}
