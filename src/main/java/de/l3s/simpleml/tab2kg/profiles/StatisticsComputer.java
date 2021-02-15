package de.l3s.simpleml.tab2kg.profiles;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.github.sisyphsu.dateparser.DateParserUtils;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatistics;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL1;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL2;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.profiles.features.FeatureContext;
import de.l3s.simpleml.tab2kg.profiles.features.NumericProfileFeature;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeatureContextEnum;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeatureEnum;
import de.l3s.simpleml.tab2kg.profiles.features.TemporalProfileFeature;
import de.l3s.simpleml.tab2kg.util.GeoUtil;
import de.l3s.simpleml.tab2kg.util.Tab2KGConfiguration;

public class StatisticsComputer {

	public static void computeStatistics(Attribute attribute, List<Integer> numbersOfQuantiles,
			List<Integer> numbersOfIntervals) {

		if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.LONG) {
			computeStatisticsForLong(numbersOfQuantiles, numbersOfIntervals, attribute.getStatistics(Long.class));
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.INTEGER) {
			computeStatisticsForInteger(numbersOfQuantiles, numbersOfIntervals, attribute.getStatistics(Integer.class));
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.DOUBLE) {
			computeStatisticsForDouble(numbersOfQuantiles, numbersOfIntervals, attribute.getStatistics(Double.class));
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.BOOLEAN) {
			computeStatisticsForBoolean(numbersOfQuantiles, numbersOfIntervals, attribute.getStatistics(Boolean.class));
		} else if (attribute.getStatistics().getAttributeStatisticsType()
				.getTypeL1() == AttributeStatisticsTypeL1.TEMPORAL) {
			computeStatisticsForTime(numbersOfQuantiles, numbersOfIntervals, attribute.getStatistics(Date.class));
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.STRING) {
			computeStatisticsForString(numbersOfQuantiles, numbersOfIntervals, attribute.getStatistics(String.class));
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.GEO) {
			computeStatisticsForGeo(numbersOfQuantiles, numbersOfIntervals, attribute.getStatistics(Geometry.class));
		}

		addCategoryDistribution(attribute.getStatistics());

		ProfileCreator.identifyTypeL3(attribute);
	}

	private static <T> void addCategoryDistribution(AttributeStatistics<T> statistics) {

		if (statistics.isCategorical()) {
			for (T value : statistics.getValueList()) {
				statistics.increaseCountPerEntity(value);
			}
		}

	}

	private static void computeStatisticsForString(List<Integer> numbersOfQuantiles, List<Integer> numbersOfIntervals,
			AttributeStatistics<String> stats) {

		Collections.sort(stats.getValueList());

		// count number of distinct values. Given a sorted list, just compare
		// each value to the predecessor in the list.
		String previousValue = null;
		int numberOfDistinctValues = 0;
		long numberOfDigits = 0;
		long numberOfCharacters = 0;
		long numberOfTokens = 0;
		long numberOfSpecialCharacters = 0;
		long numberOfCapitalisedValues = 0;
		// long numberOfValues = 0;

		for (String value : stats.getValueList()) {

			if (value != null) {
				int[] numberOfDigitsAndCharacters = countDigitsAndCharacters(String.valueOf(value));
				numberOfDigits += numberOfDigitsAndCharacters[0];
				numberOfCharacters += numberOfDigitsAndCharacters[1];
				numberOfTokens += numberOfDigitsAndCharacters[2];
				numberOfSpecialCharacters += numberOfDigitsAndCharacters[3];
				numberOfCapitalisedValues += numberOfDigitsAndCharacters[4];
				// numberOfValues += 1;
			}

			if (previousValue == null || !value.equals(previousValue)) {
				previousValue = value;
				numberOfDistinctValues += 1;
			}
		}

		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_DIGITS,
				(double) numberOfDigits / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CHARACTERS,
				(double) numberOfCharacters / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_TOKENS,
				(double) numberOfTokens / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CAPITALISED_VALUES,
				(double) numberOfCapitalisedValues / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_SPECIAL_CHARACTERS,
				(double) numberOfSpecialCharacters / stats.getNumberOfNonNullValues());

		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfDistinctValues));

		computeStatisticsForInteger(numbersOfQuantiles, numbersOfIntervals, stats.getWordLengthStatistics());

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Integer>(ProfileFeatureEnum.MIN, DataTypeClass.XS_NON_NEGATIVE_INTEGER,
						stats.getWordLengthStatistics().getNumericFeature(ProfileFeatureEnum.MIN).getIntValue(),
						new FeatureContext<String>(DataTypeClass.XS_STRING,
								Tab2KGConfiguration.STRING_STATISTICS_COMMENT, ProfileFeatureContextEnum.COMMENT)));
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Integer>(ProfileFeatureEnum.MAX, DataTypeClass.XS_NON_NEGATIVE_INTEGER,
						stats.getWordLengthStatistics().getNumericFeature(ProfileFeatureEnum.MAX).getIntValue(),
						new FeatureContext<String>(DataTypeClass.XS_STRING,
								Tab2KGConfiguration.STRING_STATISTICS_COMMENT, ProfileFeatureContextEnum.COMMENT)));
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MEDIAN, DataTypeClass.XS_DOUBLE,
						stats.getWordLengthStatistics().getNumericFeature(ProfileFeatureEnum.MEDIAN).getDoubleValue(),
						new FeatureContext<String>(DataTypeClass.XS_STRING,
								Tab2KGConfiguration.STRING_STATISTICS_COMMENT, ProfileFeatureContextEnum.COMMENT)));
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MEAN, DataTypeClass.XS_DOUBLE,
						stats.getWordLengthStatistics().getNumericFeature(ProfileFeatureEnum.MEAN).getDoubleValue(),
						new FeatureContext<String>(DataTypeClass.XS_STRING,
								Tab2KGConfiguration.STRING_STATISTICS_COMMENT, ProfileFeatureContextEnum.COMMENT)));
		stats.addNumericProfileFeature(new NumericProfileFeature<Double>(ProfileFeatureEnum.SD, DataTypeClass.XS_DOUBLE,
				stats.getWordLengthStatistics().getNumericFeature(ProfileFeatureEnum.SD).getDoubleValue(),
				new FeatureContext<String>(DataTypeClass.XS_STRING, Tab2KGConfiguration.STRING_STATISTICS_COMMENT,
						ProfileFeatureContextEnum.COMMENT)));

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_BELOW, DataTypeClass.XS_DOUBLE,
						stats.getWordLengthStatistics().getNumericFeature(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_BELOW)
								.getDoubleValue(),
						new FeatureContext<String>(DataTypeClass.XS_STRING,
								Tab2KGConfiguration.STRING_STATISTICS_COMMENT, ProfileFeatureContextEnum.COMMENT)));
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_ABOVE, DataTypeClass.XS_DOUBLE,
						stats.getWordLengthStatistics().getNumericFeature(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_ABOVE)
								.getDoubleValue(),
						new FeatureContext<String>(DataTypeClass.XS_STRING,
								Tab2KGConfiguration.STRING_STATISTICS_COMMENT, ProfileFeatureContextEnum.COMMENT)));

		// histograms
		int rank = 0;
		while (true) {
			NumericProfileFeature<?> feature = stats.getWordLengthStatistics()
					.getNumericFeature(ProfileFeatureEnum.HISTOGRAM, rank);
			if (feature == null)
				break;
			else {
				stats.addNumericProfileFeature(feature);
				feature.addFeatureContext(new FeatureContext<String>(DataTypeClass.XS_STRING,
						Tab2KGConfiguration.STRING_STATISTICS_COMMENT, ProfileFeatureContextEnum.COMMENT));
			}
			rank += 1;
		}

		// quantiles
		List<ProfileFeatureEnum> quantiles = new ArrayList<ProfileFeatureEnum>();
		quantiles.add(ProfileFeatureEnum.QUARTILE);
		quantiles.add(ProfileFeatureEnum.DECILE);
		quantiles.add(ProfileFeatureEnum.PERCENTILE);

		for (ProfileFeatureEnum quantile : quantiles) {
			rank = 0;
			while (true) {
				NumericProfileFeature<?> feature = stats.getWordLengthStatistics().getNumericFeature(quantile, rank);
				if (feature == null)
					break;
				else {
					stats.addNumericProfileFeature(feature);
					feature.addFeatureContext(new FeatureContext<String>(DataTypeClass.XS_STRING,
							Tab2KGConfiguration.STRING_STATISTICS_COMMENT, ProfileFeatureContextEnum.COMMENT));
				}
				rank += 1;
			}
		}
	}

	private static void computeStatisticsForBoolean(List<Integer> numbersOfQuantiles, List<Integer> numbersOfIntervals,
			AttributeStatistics<Boolean> stats) {

		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_DIGITS, 1d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CHARACTERS, 1d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_TOKENS, 1d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CAPITALISED_VALUES, 0d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_SPECIAL_CHARACTERS, 0d);

		Collections.sort(stats.getValueList());

		int countTrue = 0;
		int countFalse = 0;

		for (Boolean value : stats.getValueList()) {
			if (value != null) {
				if (value)
					countTrue += 1;
				else
					countFalse += 1;
			}
		}

		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_TRUE_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, countTrue));
		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_FALSE_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, countFalse));

		if (countTrue != 0 && countFalse != 0) {
			stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(
					ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES, DataTypeClass.XS_NON_NEGATIVE_INTEGER, 2));
		} else if (countTrue == 0 && countFalse != 0 || countTrue != 0 && countFalse == 0) {
			stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(
					ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES, DataTypeClass.XS_NON_NEGATIVE_INTEGER, 1));
		} else {
			stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(
					ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES, DataTypeClass.XS_NON_NEGATIVE_INTEGER, 0));
		}

		stats.setIsCategorical(true);
	}

	private static void computeStatisticsForInteger(List<Integer> numbersOfQuantiles, List<Integer> numbersOfIntervals,
			AttributeStatistics<Integer> stats) {

		long numberOfDigits = 0;
		double average = 0d;

		int i = 0;
		for (Integer value : stats.getValueList()) {
			average = (average * i + value) / (i + 1);
			numberOfDigits += String.valueOf(value).length();
			i += 1;
		}

		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_DIGITS,
				(double) numberOfDigits / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CHARACTERS,
				(double) numberOfDigits / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_TOKENS, 1d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CAPITALISED_VALUES, 0d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_SPECIAL_CHARACTERS, 0d);

		Collections.sort(stats.getValueList());

		int min = stats.getValueList().get(0);
		int max = stats.getValueList().get(stats.getValueList().size() - 1);
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Integer>(ProfileFeatureEnum.MIN, DataTypeClass.XS_INTEGER, min));
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Integer>(ProfileFeatureEnum.MAX, DataTypeClass.XS_INTEGER, max));

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MEAN, DataTypeClass.XS_DOUBLE, average));

		// count number of distinct values. Given a sorted list, just compare
		// each value to the predecessor in the list.
		Integer previousValue = null;
		int numberOfDistinctValues = 0;
		double standardDeviation = 0;
		for (int value : stats.getValueList()) {
			standardDeviation += Math.pow(value - average, 2);
			if (previousValue == null || value != previousValue) {
				previousValue = value;
				numberOfDistinctValues += 1;
			}
		}

		if (stats.getNumberOfNonNullValues() == 1)
			standardDeviation = 0d;
		else
			standardDeviation = Math.sqrt(standardDeviation / (stats.getNumberOfNonNullValues() - 1));

		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfDistinctValues));
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.SD, DataTypeClass.XS_DOUBLE, standardDeviation));

		double median;
		if (stats.getValueList().size() % 2 == 0) {
			double lowerMedian = stats.getValueList().get((stats.getValueList().size()) / 2 - 1);
			double upperMedian = stats.getValueList().get((stats.getValueList().size()) / 2);
			median = (lowerMedian + upperMedian) / 2d;
		} else {
			median = stats.getValueList().get((stats.getValueList().size() + 1) / 2 - 1);
		}

		List<Integer> numbersOfQuantilesTmp = new ArrayList<Integer>();
		numbersOfQuantilesTmp.addAll(numbersOfQuantiles);
		if (!numbersOfQuantilesTmp.contains(4))
			numbersOfQuantilesTmp.add(4);

		for (int numberOfQuantiles : numbersOfQuantilesTmp) {
			int rank = 1;
			List<Double> quantiles = new ArrayList<Double>();

			ProfileFeatureEnum quantileType = null;

			if (numberOfQuantiles == 4)
				quantileType = ProfileFeatureEnum.QUARTILE;
			else if (numberOfQuantiles == 10)
				quantileType = ProfileFeatureEnum.DECILE;
			else if (numberOfQuantiles == 100)
				quantileType = ProfileFeatureEnum.PERCENTILE;

			if (quantileType == null) {
				System.out.println("Invalid Quantile: " + numberOfQuantiles);
				continue;
			}

			stats.addNumericProfileFeature(
					new NumericProfileFeature<Integer>(quantileType, DataTypeClass.XS_INTEGER, min, 0));

			for (int q = 1; q < numberOfQuantiles; q++) {
				double nq = stats.getValueList().size() * ((double) q / numberOfQuantiles);

				if (nq % 1 == 0) {
					// whole number
					double lower = stats.getValueList().get((int) (nq - 1));
					double upper = stats.getValueList().get((int) (nq));
					quantiles.add((lower + upper) / 2d);
					stats.addNumericProfileFeature(new NumericProfileFeature<Double>(quantileType,
							DataTypeClass.XS_DOUBLE, (lower + upper) / 2d, rank));
				} else {
					quantiles.add((double) stats.getValueList().get((int) Math.ceil(nq) - 1));
					stats.addNumericProfileFeature(new NumericProfileFeature<Double>(quantileType,
							DataTypeClass.XS_DOUBLE, (double) stats.getValueList().get((int) Math.ceil(nq) - 1), rank));
				}

				rank += 1;
			}

			stats.addNumericProfileFeature(
					new NumericProfileFeature<Integer>(quantileType, DataTypeClass.XS_INTEGER, max, rank));
		}

		for (int numberOfIntervals : numbersOfIntervals) {
			createHistogramWithoutOutliers(stats, numberOfIntervals);
		}

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MEDIAN, DataTypeClass.XS_DOUBLE, median));
	}

	private static void computeStatisticsForLong(List<Integer> numbersOfQuantiles, List<Integer> numbersOfIntervals,
			AttributeStatistics<Long> stats) {

		double average = 0;
		int i = 0;
		for (Long value : stats.getValueList()) {
			average = (average * i + value) / (i + 1);
			i += 1;
		}

		Collections.sort(stats.getValueList());

		long min = stats.getValueList().get(0);
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Long>(ProfileFeatureEnum.MIN, DataTypeClass.XS_LONG, min));
		long max = stats.getValueList().get(stats.getValueList().size() - 1);
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Long>(ProfileFeatureEnum.MAX, DataTypeClass.XS_LONG, max));
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MEAN, DataTypeClass.XS_DOUBLE, average));

		// count number of distinct values. Given a sorted list, just compare
		// each value to the predecessor in the list.
		Long previousValue = null;
		int numberOfDistinctValues = 0;
		double standardDeviation = 0;
		for (long value : stats.getValueList()) {
			standardDeviation += Math.pow(value - average, 2);
			if (previousValue == null || value != previousValue) {
				previousValue = value;
				numberOfDistinctValues += 1;
			}
		}

		if (stats.getNumberOfNonNullValues() == 1)
			standardDeviation = 0d;
		else
			standardDeviation = Math.sqrt(standardDeviation / (stats.getNumberOfNonNullValues() - 1));

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.SD, DataTypeClass.XS_DOUBLE, standardDeviation));

		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfDistinctValues));

		double median;
		if (stats.getValueList().size() % 2 == 0) {
			double lowerMedian = stats.getValueList().get((stats.getValueList().size()) / 2 - 1);
			double upperMedian = stats.getValueList().get((stats.getValueList().size()) / 2);
			median = (lowerMedian + upperMedian) / 2d;
		} else {
			median = stats.getValueList().get((stats.getValueList().size() + 1) / 2 - 1);
		}

		List<Integer> numbersOfQuantilesTmp = new ArrayList<Integer>();
		numbersOfQuantilesTmp.addAll(numbersOfQuantiles);
		if (!numbersOfQuantilesTmp.contains(4))
			numbersOfQuantilesTmp.add(4);

		for (int numberOfQuantiles : numbersOfQuantilesTmp) {
			int rank = 1;
			List<Double> quantiles = new ArrayList<Double>();

			ProfileFeatureEnum quantileType = null;

			if (numberOfQuantiles == 4)
				quantileType = ProfileFeatureEnum.QUARTILE;
			else if (numberOfQuantiles == 10)
				quantileType = ProfileFeatureEnum.DECILE;
			else if (numberOfQuantiles == 100)
				quantileType = ProfileFeatureEnum.PERCENTILE;

			if (quantileType == null) {
				System.out.println("Invalid Quantile: " + numberOfQuantiles);
				continue;
			}

			stats.addNumericProfileFeature(
					new NumericProfileFeature<Long>(quantileType, DataTypeClass.XS_LONG, min, 0));

			for (int q = 1; q < numberOfQuantiles; q++) {
				double nq = stats.getValueList().size() * ((double) q / numberOfQuantiles);

				if (nq % 1 == 0) {
					// whole number
					double lower = stats.getValueList().get((int) (nq - 1));
					double upper = stats.getValueList().get((int) (nq));
					quantiles.add((lower + upper) / 2d);

					stats.addNumericProfileFeature(new NumericProfileFeature<Double>(quantileType,
							DataTypeClass.XS_DOUBLE, (lower + upper) / 2d, rank));
				} else {
					stats.addNumericProfileFeature(new NumericProfileFeature<Double>(quantileType,
							DataTypeClass.XS_DOUBLE, (double) stats.getValueList().get((int) Math.ceil(nq) - 1), rank));
					quantiles.add((double) stats.getValueList().get((int) Math.ceil(nq) - 1));
				}
				rank += 1;
			}

			stats.addNumericProfileFeature(
					new NumericProfileFeature<Long>(quantileType, DataTypeClass.XS_LONG, max, rank));
		}

		for (int numberOfIntervals : numbersOfIntervals)
			createHistogramWithoutOutliers(stats, numberOfIntervals);

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MEDIAN, DataTypeClass.XS_DOUBLE, median));
	}

	private static void computeStatisticsForDouble(List<Integer> numbersOfQuantiles, List<Integer> numbersOfIntervals,
			AttributeStatistics<Double> stats) {

		long numberOfDigits = 0;
		long numberOfCharacters = 0;
		double average = 0;

		int i = 0;
		for (Double value : stats.getValueList()) {
			average = (average * i + value) / (i + 1);
			int[] numberOfDigitsAndCharacters = countDigitsAndCharacters(String.valueOf(value));
			numberOfDigits += numberOfDigitsAndCharacters[0];
			numberOfCharacters += numberOfDigitsAndCharacters[1];
			i += 1;
		}

		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_DIGITS,
				(double) numberOfDigits / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CHARACTERS,
				(double) numberOfCharacters / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_TOKENS, 1d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CAPITALISED_VALUES, 0d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_SPECIAL_CHARACTERS, 0d);

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MEAN, DataTypeClass.XS_DOUBLE, average));

		Collections.sort(stats.getValueList());
		double min = stats.getValueList().get(0);
		double max = stats.getValueList().get(stats.getValueList().size() - 1);

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MIN, DataTypeClass.XS_DOUBLE, min));
		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MAX, DataTypeClass.XS_DOUBLE, max));

		// count number of distinct values. Given a sorted list, just compare
		// each value to the predecessor in the list.
		Double previousValue = null;
		int numberOfDistinctValues = 0;
		double standardDeviation = 0;
		for (double value : stats.getValueList()) {
			standardDeviation += Math.pow(value - average, 2);
			if (previousValue == null || value != previousValue) {
				previousValue = value;
				numberOfDistinctValues += 1;
			}
		}

		if (stats.getNumberOfNonNullValues() <= 1)
			standardDeviation = 0d;
		else
			standardDeviation = Math.sqrt(standardDeviation / (stats.getNumberOfNonNullValues() - 1));

		if (Double.isInfinite(standardDeviation)) {
			System.out.println("Error.");
			for (double value : stats.getValueList()) {
				System.out.println("   " + value);
			}
		}

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.SD, DataTypeClass.XS_DOUBLE, standardDeviation));

		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfDistinctValues));

		double median;
		if (stats.getValueList().size() % 2 == 0) {
			double lowerMedian = stats.getValueList().get((stats.getValueList().size()) / 2 - 1);
			double upperMedian = stats.getValueList().get((stats.getValueList().size()) / 2);
			median = (lowerMedian + upperMedian) / 2d;
		} else {
			median = stats.getValueList().get((stats.getValueList().size() + 1) / 2 - 1);
		}

		List<Integer> numbersOfQuantilesTmp = new ArrayList<Integer>();
		numbersOfQuantilesTmp.addAll(numbersOfQuantiles);
		if (!numbersOfQuantilesTmp.contains(4))
			numbersOfQuantilesTmp.add(4);

		for (int numberOfQuantiles : numbersOfQuantilesTmp) {
			int rank = 1;

			List<Double> quantiles = new ArrayList<Double>();
			ProfileFeatureEnum quantileType = null;

			if (numberOfQuantiles == 4)
				quantileType = ProfileFeatureEnum.QUARTILE;
			else if (numberOfQuantiles == 10)
				quantileType = ProfileFeatureEnum.DECILE;
			else if (numberOfQuantiles == 100)
				quantileType = ProfileFeatureEnum.PERCENTILE;

			stats.addNumericProfileFeature(
					new NumericProfileFeature<Double>(quantileType, DataTypeClass.XS_DOUBLE, min, 0));

			for (int q = 1; q < numberOfQuantiles; q++) {
				double nq = stats.getValueList().size() * ((double) q / numberOfQuantiles);

				if (nq % 1 == 0) {
					// whole number
					double lower = stats.getValueList().get((int) (nq - 1));
					double upper = stats.getValueList().get((int) (nq));
					quantiles.add((lower + upper) / 2d);
					stats.addNumericProfileFeature(new NumericProfileFeature<Double>(quantileType,
							DataTypeClass.XS_DOUBLE, (lower + upper) / 2d, rank));
				} else {
					quantiles.add((double) stats.getValueList().get((int) Math.ceil(nq) - 1));
					stats.addNumericProfileFeature(new NumericProfileFeature<Double>(quantileType,
							DataTypeClass.XS_DOUBLE, (double) stats.getValueList().get((int) Math.ceil(nq) - 1), rank));
				}
				rank += 1;

			}
			stats.addNumericProfileFeature(
					new NumericProfileFeature<Double>(quantileType, DataTypeClass.XS_DOUBLE, max, rank));
		}

		for (int numberOfIntervals : numbersOfIntervals)
			createHistogramWithoutOutliers(stats, numberOfIntervals);

		stats.addNumericProfileFeature(
				new NumericProfileFeature<Double>(ProfileFeatureEnum.MEDIAN, DataTypeClass.XS_DOUBLE, median));
	}

	private static int[] countDigitsAndCharacters(String value) {

		int[] counts = new int[5];

		if (value.isEmpty()) {
			counts[0] = 0;
			counts[1] = 0;
			counts[2] = 0;
			counts[3] = 0;
			counts[4] = 0;
			return counts;
		}

		counts[0] = (int) value.chars().filter(Character::isDigit).count();
		counts[1] = value.length(); // - (int) value.chars().filter(Character::isDigit).count();
		counts[2] = value.trim().split("\\s+").length; // number of tokens
		counts[3] = value.length() - value.replaceAll("\\W", "").length(); // number of special characters
		if (value.substring(0, 1).matches("[A-Z]")) {
			counts[4] = 1; // first word is capitalised
		} else
			counts[4] = 0;

		return counts;
	}

	public static void computeStatisticsForTime(List<Integer> numbersOfQuantiles, List<Integer> numbersOfIntervals,
			AttributeStatistics<Date> stats) {

		// int numberOfNullValues = 0;
		long numberOfCharacters = 0;

		for (Date value : stats.getValueList()) {
			// if (value == null) {
			// numberOfNullValues += 1;
			// stats.getTimeInMillisStatistics().addToValueList(null);
			// } else {
			// stats.addValidNonNullValue();
			numberOfCharacters += String.valueOf(value.getTime()).length();
			// }
		}
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_DIGITS,
				(double) numberOfCharacters / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CHARACTERS,
				(double) numberOfCharacters / stats.getNumberOfNonNullValues());
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_TOKENS, 1d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CAPITALISED_VALUES, 0d);
		stats.setOrUpdateDoubleFeatureValue(ProfileFeatureEnum.AVERAGE_NUMBER_OF_SPECIAL_CHARACTERS, 0d);

		Collections.sort(stats.getValueList());

		Date previousValue = null;
		int numberOfDistinctValues = 0;
		for (Date value : stats.getValueList()) {
			if (previousValue == null || !value.equals(previousValue)) {
				previousValue = value;
				numberOfDistinctValues += 1;
			}
		}
		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfDistinctValues));

		Date min = stats.getValueList().get(0);
		Date max = stats.getValueList().get(stats.getValueList().size() - 1);

		stats.addTemporalProfileFeature(new TemporalProfileFeature(ProfileFeatureEnum.MIN, DataTypeClass.XS_DATE, min));
		stats.addTemporalProfileFeature(new TemporalProfileFeature(ProfileFeatureEnum.MAX, DataTypeClass.XS_DATE, max));

		Date median;
		if (stats.getValueList().size() % 2 == 0) {
			Date lowerMedian = stats.getValueList().get((stats.getValueList().size()) / 2 - 1);
			Date upperMedian = stats.getValueList().get((stats.getValueList().size()) / 2);
			median = new Date((lowerMedian.getTime() + upperMedian.getTime()) / 2);
		} else {
			median = stats.getValueList().get((stats.getValueList().size() + 1) / 2 - 1);
		}
		stats.addTemporalProfileFeature(
				new TemporalProfileFeature(ProfileFeatureEnum.MEDIAN, DataTypeClass.XS_DATE, median));

		List<Integer> numbersOfQuantilesTmp = new ArrayList<Integer>();
		numbersOfQuantilesTmp.addAll(numbersOfQuantiles);
		if (!numbersOfQuantilesTmp.contains(4))
			numbersOfQuantilesTmp.add(4);

		for (int numberOfQuantiles : numbersOfQuantilesTmp) {

			int rank = 1;

			List<Date> quantiles = new ArrayList<Date>();
			ProfileFeatureEnum quantileType = null;

			if (numberOfQuantiles == 4)
				quantileType = ProfileFeatureEnum.QUARTILE;
			else if (numberOfQuantiles == 10)
				quantileType = ProfileFeatureEnum.DECILE;
			else if (numberOfQuantiles == 100)
				quantileType = ProfileFeatureEnum.PERCENTILE;

			stats.addTemporalProfileFeature(new TemporalProfileFeature(quantileType, DataTypeClass.XS_DATE, min, 0));

			for (int q = 1; q < numberOfQuantiles; q++) {
				double nq = stats.getValueList().size() * ((double) q / numberOfQuantiles);

				if (nq % 1 == 0) {
					// whole number
					Date lower = stats.getValueList().get((int) (nq - 1));
					Date upper = stats.getValueList().get((int) (nq));
					quantiles.add(new Date((lower.getTime() + upper.getTime()) / 2));
					stats.addTemporalProfileFeature(new TemporalProfileFeature(quantileType, DataTypeClass.XS_DATE,
							new Date((lower.getTime() + upper.getTime()) / 2), rank));
				} else {
					quantiles.add(stats.getValueList().get((int) Math.ceil(nq) - 1));
					stats.addTemporalProfileFeature(new TemporalProfileFeature(quantileType, DataTypeClass.XS_DATE,
							stats.getValueList().get((int) Math.ceil(nq) - 1), rank));
				}

				rank += 1;
			}
			stats.addTemporalProfileFeature(new TemporalProfileFeature(quantileType, DataTypeClass.XS_DATE, max, rank));
		}

		for (int numberOfIntervals : numbersOfIntervals)
			createTemporalHistogramWithoutOutliers(stats, numberOfIntervals);
	}

	public static void computeStatisticsForGeo(List<Integer> numbersOfQuantiles, List<Integer> numbersOfIntervals,
			AttributeStatistics<Geometry> stats) {

		// we can't sort geometries, so we transform them into strings
		List<String> geoStrings = new ArrayList<String>();
		for (Geometry value : stats.getValueList()) {
			geoStrings.add(value.toText());
		}

		Collections.sort(geoStrings);

		int numberOfDistinctValues = 0;
		String previousValue = null;
		for (String value : geoStrings) {
			if (previousValue == null || !value.equals(previousValue)) {
				previousValue = value;
				numberOfDistinctValues += 1;
			}
		}

		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfDistinctValues));

		// TODO: Re-add
//		List<Location> locations = OSMPolygonsLoader.getLandkreise();
//
//		for (Geometry value : stats.getValueList()) {
//			for (Location location : locations) {
//				if (location.getGeometry().contains(value)) {
//					stats.increaseCountPerLocation(location);
//					break;
//				}
//			}
//		}

		// POINT() does not have statistics
		if (stats.getGeoDimensionStatistics() != null
				&& !stats.getGeoDimensionStatistics().getAttributeValues().isEmpty()) {
			computeStatisticsForDouble(numbersOfQuantiles, numbersOfIntervals, stats.getGeoDimensionStatistics());

			String comment = Tab2KGConfiguration.GEO_STATISTICS_COMMENT;
			stats.addNumericProfileFeature(new NumericProfileFeature<Double>(ProfileFeatureEnum.MIN,
					DataTypeClass.XS_DOUBLE,
					stats.getGeoDimensionStatistics().getNumericFeature(ProfileFeatureEnum.MIN).getDoubleValue(),
					new FeatureContext<String>(DataTypeClass.XS_STRING, comment, ProfileFeatureContextEnum.COMMENT)));
			stats.addNumericProfileFeature(new NumericProfileFeature<Double>(ProfileFeatureEnum.MAX,
					DataTypeClass.XS_DOUBLE,
					stats.getGeoDimensionStatistics().getNumericFeature(ProfileFeatureEnum.MAX).getDoubleValue(),
					new FeatureContext<String>(DataTypeClass.XS_STRING, comment, ProfileFeatureContextEnum.COMMENT)));
			stats.addNumericProfileFeature(new NumericProfileFeature<Double>(ProfileFeatureEnum.MEDIAN,
					DataTypeClass.XS_DOUBLE,
					stats.getGeoDimensionStatistics().getNumericFeature(ProfileFeatureEnum.MEDIAN).getDoubleValue(),
					new FeatureContext<String>(DataTypeClass.XS_STRING, comment, ProfileFeatureContextEnum.COMMENT)));
			stats.addNumericProfileFeature(new NumericProfileFeature<Double>(ProfileFeatureEnum.MEAN,
					DataTypeClass.XS_DOUBLE,
					stats.getGeoDimensionStatistics().getNumericFeature(ProfileFeatureEnum.MEAN).getDoubleValue(),
					new FeatureContext<String>(DataTypeClass.XS_STRING, comment, ProfileFeatureContextEnum.COMMENT)));
			stats.addNumericProfileFeature(new NumericProfileFeature<Double>(ProfileFeatureEnum.SD,
					DataTypeClass.XS_DOUBLE,
					stats.getGeoDimensionStatistics().getNumericFeature(ProfileFeatureEnum.SD).getDoubleValue(),
					new FeatureContext<String>(DataTypeClass.XS_STRING, comment, ProfileFeatureContextEnum.COMMENT)));

			// histograms
			int rank = 0;
			while (true) {
				NumericProfileFeature<?> feature = stats.getGeoDimensionStatistics()
						.getNumericFeature(ProfileFeatureEnum.HISTOGRAM, rank);
				if (feature == null)
					break;
				else {
					stats.addNumericProfileFeature(feature);
					feature.addFeatureContext(new FeatureContext<String>(DataTypeClass.XS_STRING, comment,
							ProfileFeatureContextEnum.COMMENT));
				}
				rank += 1;
			}

			// quantiles
			List<ProfileFeatureEnum> quantiles = new ArrayList<ProfileFeatureEnum>();
			quantiles.add(ProfileFeatureEnum.QUARTILE);
			quantiles.add(ProfileFeatureEnum.DECILE);
			quantiles.add(ProfileFeatureEnum.PERCENTILE);

			for (ProfileFeatureEnum quantile : quantiles) {
				rank = 0;
				while (true) {
					NumericProfileFeature<?> feature = stats.getGeoDimensionStatistics().getNumericFeature(quantile,
							rank);
					if (feature == null)
						break;
					else {
						stats.addNumericProfileFeature(feature);
						feature.addFeatureContext(new FeatureContext<String>(DataTypeClass.XS_STRING, comment,
								ProfileFeatureContextEnum.COMMENT));
					}
					rank += 1;
				}
			}
		}

	}

	private static <T> void createHistogramWithoutOutliers(AttributeStatistics<T> stats, int numberOfIntervals) {
		double q1 = stats.getNumericFeature(ProfileFeatureEnum.QUARTILE, 1).getDoubleValue();
		double q3 = stats.getNumericFeature(ProfileFeatureEnum.QUARTILE, 3).getDoubleValue();

		double iqp = q3 - q1;

		double minAllowed = q1 - 1.5 * iqp;
		double maxAllowed = q3 + 1.5 * iqp;

		createHistogram(stats, numberOfIntervals, minAllowed, maxAllowed);
	}

//	private static void createHistogramWithoutOutliers(AttributeStatistics<?> stats, int numberOfIntervals) {
//		long q1 = stats.getQuantiles().get(4).get(1).getTime();
//		long q3 = stats.getQuantiles().get(4).get(3).getTime();
//
//		double iqp = q3 - q1;
//
//		double minAllowed = q1 - 1.5 * iqp;
//		double maxAllowed = q3 + 1.5 * iqp;
//
//		createHistogram(stats, numberOfIntervals, minAllowed, maxAllowed);
//	}

	private static void createTemporalHistogramWithoutOutliers(AttributeStatistics<Date> stats, int numberOfIntervals) {

		long q1 = stats.getTemporalFeature(ProfileFeatureEnum.QUARTILE, 1).getValue().getTime();
		long q3 = stats.getTemporalFeature(ProfileFeatureEnum.QUARTILE, 3).getValue().getTime();

		double iqp = q3 - q1;

		Date minAllowed = new Date((long) Math.floor(q1 - 1.5 * iqp));
		Date maxAllowed = new Date((long) Math.ceil(q3 + 1.5 * iqp));

		createTemporalHistogram(stats, numberOfIntervals, minAllowed, maxAllowed);
	}

	private static <T> void createTemporalHistogram(AttributeStatistics<Date> stats, int numberOfIntervals,
			Date minAllowed, Date maxAllowed) {

		int underThreshold = 0;
		int aboveThreshold = 0;
		List<Date> valueListWithoutOutliers = new ArrayList<Date>();
		for (Date val : stats.getValueList()) {
			if (minAllowed != null && val.before(minAllowed))
				underThreshold += 1;
			else if (maxAllowed != null && val.after(maxAllowed))
				aboveThreshold += 1;
			else
				valueListWithoutOutliers.add(val);
		}

		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_BELOW,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, underThreshold));
		stats.addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_ABOVE,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, aboveThreshold));

		Date totalMin = valueListWithoutOutliers.get(0);
		Date totalMax = valueListWithoutOutliers.get(valueListWithoutOutliers.size() - 1);

		double intervalLength = (totalMax.getTime() - totalMin.getTime()) / numberOfIntervals;
		int listIndex = 0;
		// go through the list only once (keep positions at end of buckets)
		for (int bucketNumber = 0; bucketNumber < numberOfIntervals; bucketNumber++) {
			long minD = (long) Math.ceil(totalMin.getTime() + bucketNumber * intervalLength);
			long maxD = (long) Math.floor(totalMin.getTime() + (bucketNumber + 1) * intervalLength);
			Date min = new Date(minD);
			Date max = new Date(maxD);
			int newListIndex = listIndex;
			for (int i = listIndex; i < valueListWithoutOutliers.size(); i++) {
				if (valueListWithoutOutliers.get(i).getTime() > maxD) {
					newListIndex = i;
					break;
				}
			}

			// the last bucket never breaks above loop, so we need to count
			// instances until end of list
			if (bucketNumber == numberOfIntervals - 1)
				newListIndex = valueListWithoutOutliers.size();

			int numberOfInstances = newListIndex - listIndex;
			listIndex = newListIndex;

			NumericProfileFeature<Integer> bucketFeature = new NumericProfileFeature<Integer>(
					ProfileFeatureEnum.HISTOGRAM, DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfInstances,
					bucketNumber);
			bucketFeature.addFeatureContext(new FeatureContext<Date>(DataTypeClass.XS_DATE_TIME, min,
					ProfileFeatureContextEnum.HISTOGRAM_BUCKET_MIN));
			bucketFeature.addFeatureContext(new FeatureContext<Date>(DataTypeClass.XS_DATE_TIME, max,
					ProfileFeatureContextEnum.HISTOGRAM_BUCKET_MAX));
			stats.addNumericProfileFeature(bucketFeature);

		}

	}

	/**
	 * Adds an histogram to the stats of an attribute. Value list in stats must be
	 * sorted!
	 */
	@SuppressWarnings("unchecked")
	private static <T> void createHistogram(AttributeStatistics<T> statsTmp, int numberOfIntervals, Double minAllowed,
			Double maxAllowed) {

		if (statsTmp.getAttributeStatisticsType() == AttributeStatisticsTypeL2.INTEGER) {

			AttributeStatistics<Integer> stats = (AttributeStatistics<Integer>) statsTmp;

			int underThreshold = 0;
			int aboveThreshold = 0;
			List<Integer> valueListWithoutOutliers = new ArrayList<Integer>();
			for (Integer val : stats.getValueList()) {
				if (minAllowed != null && val < minAllowed)
					underThreshold += 1;
				else if (maxAllowed != null && val > maxAllowed)
					aboveThreshold += 1;
				else
					valueListWithoutOutliers.add(val);
			}

			stats.addNumericProfileFeature(
					new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_BELOW,
							DataTypeClass.XS_NON_NEGATIVE_INTEGER, underThreshold));
			stats.addNumericProfileFeature(
					new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_ABOVE,
							DataTypeClass.XS_NON_NEGATIVE_INTEGER, aboveThreshold));

			int totalMin = valueListWithoutOutliers.get(0);
			int totalMax = valueListWithoutOutliers.get(valueListWithoutOutliers.size() - 1);

			double intervalLength = (double) (totalMax - totalMin) / numberOfIntervals;
			int listIndex = 0;
			// go through the list only once (keep positions at end of buckets)
			for (int bucketNumber = 0; bucketNumber < numberOfIntervals; bucketNumber++) {
				double minD = totalMin + bucketNumber * intervalLength;
				double maxD = totalMin + (bucketNumber + 1) * intervalLength;
				int min = (int) Math.ceil(minD);
				int max = (int) Math.floor(maxD);
				int newListIndex = listIndex;
				for (int i = listIndex; i < valueListWithoutOutliers.size(); i++) {
					if (valueListWithoutOutliers.get(i) > maxD) {
						newListIndex = i;
						break;
					}
				}

				// the last bucket never breaks above loop, so we need to count
				// instances until end of list
				if (bucketNumber == numberOfIntervals - 1)
					newListIndex = valueListWithoutOutliers.size();

				int numberOfInstances = newListIndex - listIndex;
				listIndex = newListIndex;

				FeatureContext<Integer> featureContext = new FeatureContext<Integer>(DataTypeClass.XS_INTEGER, min,
						ProfileFeatureContextEnum.HISTOGRAM_BUCKET_MIN);
				NumericProfileFeature<Integer> bucketFeature = new NumericProfileFeature<Integer>(
						ProfileFeatureEnum.HISTOGRAM, DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfInstances,
						bucketNumber, featureContext);
				bucketFeature.addFeatureContext(new FeatureContext<Integer>(DataTypeClass.XS_INTEGER, max,
						ProfileFeatureContextEnum.HISTOGRAM_BUCKET_MAX));
				stats.addNumericProfileFeature(bucketFeature);

			}
		} else if (statsTmp.getAttributeStatisticsType() == AttributeStatisticsTypeL2.LONG) {

			AttributeStatistics<Long> stats = (AttributeStatistics<Long>) statsTmp;

			int underThreshold = 0;
			int aboveThreshold = 0;
			List<Long> valueListWithoutOutliers = new ArrayList<Long>();
			for (Long val : stats.getValueList()) {
				if (minAllowed != null && val < minAllowed)
					underThreshold += 1;
				else if (maxAllowed != null && val > maxAllowed)
					aboveThreshold += 1;
				else
					valueListWithoutOutliers.add(val);
			}

			stats.addNumericProfileFeature(
					new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_BELOW,
							DataTypeClass.XS_NON_NEGATIVE_INTEGER, underThreshold));
			stats.addNumericProfileFeature(
					new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_ABOVE,
							DataTypeClass.XS_NON_NEGATIVE_INTEGER, aboveThreshold));

			long totalMin = valueListWithoutOutliers.get(0);
			long totalMax = valueListWithoutOutliers.get(valueListWithoutOutliers.size() - 1);

			float intervalLength = (float) Math.floor(totalMax - totalMin) / numberOfIntervals;

			int listIndex = 0;
			// go through the list only once (keep positions at end of buckets)
			for (int bucketNumber = 0; bucketNumber < numberOfIntervals; bucketNumber++) {
				float minD = totalMin + bucketNumber * intervalLength;
				float maxD = totalMin + (bucketNumber + 1) * intervalLength;
				long min = (long) Math.ceil(minD);
				long max = (long) Math.floor(maxD);
				if (bucketNumber == numberOfIntervals - 1) {
					max = stats.getNumericFeature(ProfileFeatureEnum.MAX).getLongValue();
				}

				int newListIndex = listIndex;
				for (int i = listIndex; i < valueListWithoutOutliers.size(); i++) {
					if (valueListWithoutOutliers.get(i) > max) {
						newListIndex = i;
						break;
					}
				}

				// the last bucket never breaks above loop, so we need to count
				// instances until end of list
				if (bucketNumber == numberOfIntervals - 1)
					newListIndex = valueListWithoutOutliers.size();

				int numberOfInstances = newListIndex - listIndex;
				listIndex = newListIndex;

				FeatureContext<Long> featureContext = new FeatureContext<Long>(DataTypeClass.XS_LONG, min,
						ProfileFeatureContextEnum.HISTOGRAM_BUCKET_MIN);
				NumericProfileFeature<Integer> bucketFeature = new NumericProfileFeature<Integer>(
						ProfileFeatureEnum.HISTOGRAM, DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfInstances,
						bucketNumber, featureContext);
				bucketFeature.addFeatureContext(new FeatureContext<Long>(DataTypeClass.XS_LONG, max,
						ProfileFeatureContextEnum.HISTOGRAM_BUCKET_MAX));
				stats.addNumericProfileFeature(bucketFeature);

				min = max;
			}

			// stats.addHistogram(numberOfIntervals, histogram);
		} else {

			AttributeStatistics<Double> stats = (AttributeStatistics<Double>) statsTmp;

			int underThreshold = 0;
			int aboveThreshold = 0;
			List<Double> valueListWithoutOutliers = new ArrayList<Double>();
			for (Double val : stats.getValueList()) {
				if (minAllowed != null && val < minAllowed)
					underThreshold += 1;
				else if (maxAllowed != null && val > maxAllowed)
					aboveThreshold += 1;
				else
					valueListWithoutOutliers.add(val);
			}

			stats.addNumericProfileFeature(
					new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_BELOW,
							DataTypeClass.XS_NON_NEGATIVE_INTEGER, underThreshold));
			stats.addNumericProfileFeature(
					new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_ABOVE,
							DataTypeClass.XS_NON_NEGATIVE_INTEGER, aboveThreshold));

			double totalMin = valueListWithoutOutliers.get(0);
			double totalMax = valueListWithoutOutliers.get(valueListWithoutOutliers.size() - 1);

			double intervalLength = (totalMax - totalMin) / numberOfIntervals;

			int listIndex = 0;
			// go through the list only once (keep positions at end of buckets)
			for (int bucketNumber = 0; bucketNumber < numberOfIntervals; bucketNumber++) {
				double min = totalMin + bucketNumber * intervalLength;
				double max = totalMin + (bucketNumber + 1) * intervalLength;

				int newListIndex = listIndex;
				for (int i = listIndex; i < valueListWithoutOutliers.size(); i++) {
					if (valueListWithoutOutliers.get(i) > max) {
						newListIndex = i;
						break;
					}
				}

				// the last bucket never breaks above loop, so we need to count
				// instances until end of list
				if (bucketNumber == numberOfIntervals - 1)
					newListIndex = valueListWithoutOutliers.size();

				int numberOfInstances = newListIndex - listIndex;
				listIndex = newListIndex;

				NumericProfileFeature<Integer> bucketFeature = new NumericProfileFeature<Integer>(
						ProfileFeatureEnum.HISTOGRAM, DataTypeClass.XS_NON_NEGATIVE_INTEGER, numberOfInstances,
						bucketNumber);
				bucketFeature.addFeatureContext(new FeatureContext<Double>(DataTypeClass.XS_DOUBLE, min,
						ProfileFeatureContextEnum.HISTOGRAM_BUCKET_MIN));
				bucketFeature.addFeatureContext(new FeatureContext<Double>(DataTypeClass.XS_DOUBLE, max,
						ProfileFeatureContextEnum.HISTOGRAM_BUCKET_MAX));
				stats.addNumericProfileFeature(bucketFeature);

				min = max;
			}

			// stats.addHistogram(numberOfIntervals, histogram);
		}

	}

	public static void addToStatistics(Attribute attribute, String value, WKTReader wktReader, WKBReader wkbReader,
			GeoUtil geoUtil) {

		boolean printWarnings = false;
		String originalValue = value;

		if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.LONG) {
			AttributeStatistics<Long> stats = attribute.getStatistics(Long.class);
			if (value == null)
				stats.addNullValue();
			else {
				try {
					long longValue = Long.valueOf(value);
					stats.addToValueList(longValue);
					stats.addValidNonNullValue();
				} catch (NumberFormatException e) {
					if (printWarnings)
						System.out.println("Warning. Can't parse long: " + value + ".");
					stats.addInvalidValue();
				}
			}
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.INTEGER) {
			AttributeStatistics<Integer> stats = attribute.getStatistics(Integer.class);

			if (value == null)
				stats.addNullValue();
			else {
				try {
					int intValue = Integer.valueOf(value);
					stats.addToValueList(intValue);
					stats.addValidNonNullValue();
				} catch (NumberFormatException e) {
					if (printWarnings)
						System.out.println("Warning. Can't parse int: " + value + ".");
					stats.addInvalidValue();
				}
			}
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.DOUBLE) {
			AttributeStatistics<Double> stats = attribute.getStatistics(Double.class);
			if (value == null)
				stats.addNullValue();
			else {
				try {
					double doubleValue = Double.valueOf(value);
					stats.addToValueList(doubleValue);
					stats.addValidNonNullValue();
				} catch (NumberFormatException e) {
					if (printWarnings)
						System.out.println("Warning. Can't parse double: " + value + ".");
					stats.addInvalidValue();
				}
			}
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.BOOLEAN) {

			AttributeStatistics<Boolean> stats = attribute.getStatistics(Boolean.class);
			if (value == null)
				stats.addNullValue();
			else {
				if (value.toLowerCase().equals("true")) {
					stats.addToValueList(true);
					stats.addValidNonNullValue();
				} else if (value.toLowerCase().equals("false")) {
					stats.addToValueList(false);
					stats.addValidNonNullValue();
				} else {
					if (printWarnings)
						System.out.println("Warning. Can't parse boolean: " + value + ".");
					stats.addInvalidValue();

				}
			}
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.GEO) {

			AttributeStatistics<Geometry> stats = attribute.getStatistics(Geometry.class);

			try {
				Geometry geometry = null;
				if (stats.isWKB())
					geometry = wkbReader.read(WKBReader.hexToBytes(value));
				else
					geometry = wktReader.read(value);

				if (attribute.getDataSet() != null && attribute.getDataSet().latBeforeLon() != null
						&& attribute.getDataSet().latBeforeLon())
					GeoUtil.invertCoordinates(geometry);

				if (geometry.getSRID() != 0 && geometry.getSRID() != 4326) // 3857
					geometry = geoUtil.convertGeometry(geometry, 4326);
				// System.out.println("Warning: SRID " + geometry.getSRID() + " not
				// supported.");

				stats.addToValueList(geometry);
				stats.addValidNonNullValue();

				AttributeStatistics<Double> geoStats = attribute.getStatistics().getGeoDimensionStatistics();

				if (value == null || geometry == null || originalValue.isEmpty()) {
					geoStats.addNullValue();
				} else {
					// TODO: Transform to (kilo)meters
					if (geometry.getDimension() == 1) {
						geoStats.addToValueList(geometry.getLength());
					} else if (geometry.getDimension() == 2) {
						geoStats.addToValueList(geometry.getArea());
						geoStats.addValidNonNullValue();
					}
				}

			} catch (ParseException | IllegalArgumentException | FactoryException | TransformException e) {
				if (printWarnings)
					System.out.println("Warning. Can't parse geo: " + value + ".");
				stats.addInvalidValue();
			}

		} else if (attribute.getStatistics().getAttributeStatisticsType()
				.getTypeL1() == AttributeStatisticsTypeL1.TEMPORAL) {
			AttributeStatistics<Date> stats = attribute.getStatistics(Date.class);
			AttributeStatistics<Long> msStats = attribute.getStatistics().getMillisecondsStatistics();

			if (value == null) {
				stats.addNullValue();
				msStats.addNullValue();
			} else {
				Date date;
				try {
					date = DateParserUtils.parseDate(value);// DataTable.TIME_FORMAT.parse(value);
					stats.addToValueList(date);
					stats.addValidNonNullValue();
					msStats.addToValueList(date.getTime());
					msStats.addValidNonNullValue();
				} catch (DateTimeParseException e) {
					stats.addInvalidValue();
					msStats.addNullValue();
					if (printWarnings)
						System.out.println("Warning. Can't parse date time: " + value + ".");
					// e.printStackTrace();
				}
			}
		} else if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.STRING) {
			AttributeStatistics<String> stats = attribute.getStatistics(String.class);
			AttributeStatistics<Integer> wordLengthStats = attribute.getStatistics().getWordLengthStatistics();

			if (value == null || originalValue.isEmpty()) {
				stats.addNullValue();
				wordLengthStats.addNullValue();
			} else {
				stats.addToValueList(value);
				stats.addValidNonNullValue();
				wordLengthStats.addToValueList(value.length());
				wordLengthStats.addValidNonNullValue();
			}
		}

	}

}
