package de.l3s.simpleml.tab2kg.util;

import java.io.File;

public class Config {

	private static final String LOCAL_PREFIX = ""; // anonymized
	private static final String SERVER_PREFIX = "";// anonymized
	public static final String GITHUB_ACCESS_TOKEN = ""; // anonymized

	public static String getPath(FileLocation location) {
		if (isLocal())
			return LOCAL_PREFIX + location.getServerPath();
		else
			return SERVER_PREFIX + location.getServerPath();
	}

	public static String getPath(String fileName) {
		if (isLocal())
			return LOCAL_PREFIX + fileName;
		else
			return SERVER_PREFIX + fileName;
	}

	public static String getPath(FileLocation location, Mode mode) {
		if (isLocal())
			return LOCAL_PREFIX + location.getServerPath().replace("$tt$", mode.toString().toLowerCase());
		else
			return SERVER_PREFIX + location.getServerPath().replace("$tt$", mode.toString().toLowerCase());
	}

	public static boolean isLocal() {
		if (SERVER_PREFIX == null)
			return true;
		return !((new File(SERVER_PREFIX)).exists());
	}

	public static String getFolderNameRelativeToBase(String folderName) {
		if (isLocal())
			return folderName.replace("\\", "/").replace(LOCAL_PREFIX, "");
		else
			return folderName.replace("\\", "/").replace(SERVER_PREFIX, "");
	}

	public static boolean useIntegerBooleanStatistics() {
		return false;
	}
}
