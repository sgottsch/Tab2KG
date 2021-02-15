package de.l3s.simpleml.tab2kg.util.time;

import java.text.DateFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Class to extract date information from strings
 */
public class DateTimeParser {

	public static Set<Integer> getDateFormats(String expression) {

		Set<Integer> dateFormats = new HashSet<Integer>();

		if (expression.contains("y"))
			dateFormats.add(DateFormat.YEAR_FIELD);
		if (expression.contains("M"))
			dateFormats.add(DateFormat.MONTH_FIELD);
		if (expression.contains("d"))
			dateFormats.add(DateFormat.DATE_FIELD);
		if (expression.contains("D"))
			dateFormats.add(DateFormat.DAY_OF_YEAR_FIELD);
		if (expression.contains("u"))
			dateFormats.add(DateFormat.DAY_OF_WEEK_FIELD);
		if (expression.contains("H"))
			dateFormats.add(DateFormat.HOUR0_FIELD);

		return dateFormats;
	}

}
