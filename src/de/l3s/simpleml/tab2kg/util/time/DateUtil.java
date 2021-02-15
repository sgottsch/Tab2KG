package de.l3s.simpleml.tab2kg.util.time;

import java.util.Date;

import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL2;
import de.l3s.simpleml.tab2kg.util.Tab2KGConfiguration;

public class DateUtil {

	public static String formatDate(Date date, AttributeStatisticsTypeL2 type, Tab2KGConfiguration config) {
		switch (type) {
		case DATETIME:
			return config.getDateTimeFormat().format(date);
		case TIME:
			return config.getTimeFormat().format(date);
		case DATE:
			return config.getDateFormat().format(date);
		default:
			throw new IllegalArgumentException("Can't format date " + type + ".");
		}
	}

}
