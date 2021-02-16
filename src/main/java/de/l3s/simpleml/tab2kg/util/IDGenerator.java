package de.l3s.simpleml.tab2kg.util;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Generates random alpha-numeric IDs.
 */
public class IDGenerator {

	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	// Source: https://dzone.com/articles/generate-random-alpha-numeric
	public static String randomAlphaNumeric(int count) {

		StringBuilder builder = new StringBuilder();

		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}

		return builder.toString();
	}

	public static String createURLString(List<String> parts) {
		String result = "";

		for (String part : parts) {
			String camelCased = "";
			String[] wordParts = part.split("_");

			for (String wordPart : wordParts) {
				camelCased += StringUtils.capitalize(wordPart);
			}

			result += camelCased;
		}

		return result;
	}

	public static String createURLString(String part1) {
		return createURLString(Arrays.asList(part1));
	}

	public static String createURLString(String part1, String part2) {
		return createURLString(Arrays.asList(part1, part2));
	}

	public static String createURLString(String part1, String part2, String part3) {
		return createURLString(Arrays.asList(part1, part2, part3));
	}

	public static String createURLString(String part1, String part2, String part3, String part4) {
		return createURLString(Arrays.asList(part1, part2, part3, part4));
	}

}
