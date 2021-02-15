package de.l3s.simpleml.tab2kg.util;

import java.text.SimpleDateFormat;

import de.l3s.simpleml.tab2kg.model.sparql.Language;

public final class Tab2KGConfiguration {

	private SimpleDateFormat dateFormat;
	private SimpleDateFormat dateTimeFormat;
	private SimpleDateFormat timeFormat;

	public static final String NULL_VALUE = "";
	public static final String STRING_STATISTICS_COMMENT = "string length";
	public static final String GEO_STATISTICS_COMMENT = "geo characterstics";

	public Tab2KGConfiguration() {
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		this.timeFormat = new SimpleDateFormat("hh:mm:ss");
	}

	public Language getLanguage() {
		return Language.EN;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public SimpleDateFormat getDateTimeFormat() {
		return dateTimeFormat;
	}

	public SimpleDateFormat getTimeFormat() {
		return timeFormat;
	}

}