package de.l3s.simpleml.tab2kg.model.sparql.datatypes;

import java.time.format.DateTimeParseException;

import com.github.sisyphsu.dateparser.DateParserUtils;

public class XSDateTime implements XSDataType {

	// Source:
	// https://stackoverflow.com/questions/26355985/regular-expression-for-ddmmyyyy-date-including-validation-for-leap-year/26356146#26356146
	// https://www.regextester.com/96683
	// TODO
	// private Pattern pattern = Pattern.compile("^yyyy-MM-dd
	// hh:mm:ss(.SSSSSS)?$");
	//
	// private SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd
	// hh:mm:ss.SSSSSS");
	// private SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd
	// hh:mm:ss.SSSSSS");

	// private DateTimeFormatter dateTimeFormatter =
	// DateTimeFormatter.ISO_DATE_TIME;

	// private DateTimeFormatter dateTimeFormatter2 =
	// DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss.SSSSSS");

	@Override
	public boolean matches(String value) {
		if (value.isEmpty())
			return false;
		try {
			// must contains numbers (otherwise, "Thu" would count as a date)
			if (!value.matches(".*\\d.*"))
				return false;

			DateParserUtils.parseDate(value);
			// dateTimeFormatter.parse(value.replace(".000000", "").replace(" ",
			// "T"));
		} catch (DateTimeParseException e) {
			return false;
		}
		return true;
	}

	@Override
	public DataTypeClass getRDFClass() {
		return DataTypeClass.XS_DATE_TIME;
	}
	@Override
	public boolean isTemporal() {
		return true;
	}
}
