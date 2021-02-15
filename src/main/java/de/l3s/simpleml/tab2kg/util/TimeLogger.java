package de.l3s.simpleml.tab2kg.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeLogger {

	public static final SimpleDateFormat LOG_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat LOG_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
	public static final SimpleDateFormat FILE_NAME_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

	public static String getTime() {
		return LOG_TIME_FORMAT.format(new Date());
	}

	public static String getDateTime() {
		return LOG_DATE_TIME_FORMAT.format(new Date());
	}

	public static String getFileNameDateTime() {
		return FILE_NAME_FORMAT.format(new Date());
	}

}
