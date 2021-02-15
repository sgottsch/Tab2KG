package de.l3s.simpleml.tab2kg.profiles.features;

import org.apache.commons.lang.StringUtils;

import de.l3s.simpleml.tab2kg.model.sparql.Prefix;

public enum ProfileFeatureEnum {

	MIN(Prefix.SEAS, "minimum", true), MAX(Prefix.SEAS, "maximum", true), MEAN(Prefix.SEAS, "mean", true),
	SD(Prefix.SEAS, "standardDeviation", true), MEDIAN(Prefix.SEAS, "median", true),
	QUARTILE(Prefix.SEAS, "quartile", true), DECILE(Prefix.SEAS, "decile", true),
	PERCENTILE(Prefix.SEAS, "percentile", true), NUMBER_OF_VALUES(Prefix.ANON, "numberOfValues", false),
	NUMBER_OF_DISTINCT_VALUES(Prefix.ANON, "numberOfDistinctValues", false),
	NUMBER_OF_TRUE_VALUES(Prefix.ANON, "numberOfTrueValues", false, NUMBER_OF_VALUES),
	NUMBER_OF_FALSE_VALUES(Prefix.ANON, "numberOfFalseValues", false, NUMBER_OF_VALUES),
	NUMBER_OF_VALID_NON_NULL_VALUES(Prefix.ANON, "numberOfValidNonNullValues", false),
	NUMBER_OF_VALID_VALUES(Prefix.ANON, "numberOfValidValues", false),
	NUMBER_OF_NULL_VALUES(Prefix.ANON, "numberOfNullValues", false),
	NUMBER_OF_INVALID_VALUES(Prefix.ANON, "numberOfInvalidValues", false),
	NUMBER_OF_OUTLIERS_BELOW(Prefix.ANON, "numberOfOutliersBelow", false),
	NUMBER_OF_OUTLIERS_ABOVE(Prefix.ANON, "numberOfOutliersAbove", false),
	AVERAGE_NUMBER_OF_DIGITS(Prefix.ANON, "averageNumberOfDigits", false),
	AVERAGE_NUMBER_OF_CHARACTERS(Prefix.ANON, "averageNumberOfCharacters", false),
	AVERAGE_NUMBER_OF_TOKENS(Prefix.ANON, "averageNumberOfTokens", false),
	AVERAGE_NUMBER_OF_SPECIAL_CHARACTERS(Prefix.ANON, "averageNumberOfSpecialCharacters", false),
	AVERAGE_NUMBER_OF_CAPITALISED_VALUES(Prefix.ANON, "averageNumberOfCapitalisedValues", false),
	HISTOGRAM(Prefix.ANON, "histogram", false),
	NORMALISED_NUMBER_OF_DISTINCT_VALUES(NUMBER_OF_DISTINCT_VALUES, Prefix.ANON, "normalisedNumberOfDistinctValues",
			false, NUMBER_OF_VALUES),
	NORMALISED_NUMBER_OF_VALID_NON_NULL_VALUES(NUMBER_OF_VALID_NON_NULL_VALUES, Prefix.ANON,
			"normalisedNumberOfValidNonNullValues", false, NUMBER_OF_VALUES),
	NORMALISED_NUMBER_OF_VALID_VALUES(NUMBER_OF_VALID_VALUES, Prefix.ANON, "normalisedNumberOfValidValues", false,
			NUMBER_OF_VALUES),
	NORMALISED_NUMBER_OF_NULL_VALUES(NUMBER_OF_NULL_VALUES, Prefix.ANON, "normalisedNumberOfNullValues", false,
			NUMBER_OF_VALUES),
	NORMALISED_NUMBER_OF_INVALID_VALUES(NUMBER_OF_INVALID_VALUES, Prefix.ANON, "normalisedNumberOfInvalidValues", false,
			NUMBER_OF_VALUES),
	NORMALISED_NUMBER_OF_OUTLIERS_BELOW(NUMBER_OF_OUTLIERS_BELOW, Prefix.ANON, "normalisedNumberOfOutliersBelow", false,
			NUMBER_OF_VALUES),
	NORMALISED_NUMBER_OF_OUTLIERS_ABOVE(NUMBER_OF_OUTLIERS_ABOVE, Prefix.ANON, "normalisedNumberOfOutliersAbove", false,
			NUMBER_OF_VALUES),
	NORMALISED_HISTOGRAM(HISTOGRAM, Prefix.ANON, "normalisedQuartile", false, NUMBER_OF_VALID_VALUES);

	private ProfileFeatureEnum normaliser;
	private ProfileFeatureEnum baseFeature;

	private ProfileFeatureEnum(Prefix prefix, String shortName, boolean isEvaluation) {
		this.prefix = prefix;
		this.shortName = shortName;
		this.isEvaluation = isEvaluation;
	}

	private ProfileFeatureEnum(Prefix prefix, String shortName, boolean isEvaluation, ProfileFeatureEnum normaliser) {
		this.prefix = prefix;
		this.shortName = shortName;
		this.isEvaluation = isEvaluation;
		this.normaliser = normaliser;
	}

	private ProfileFeatureEnum(ProfileFeatureEnum baseFeature, Prefix prefix, String shortName, boolean isEvaluation,
			ProfileFeatureEnum normaliser) {
		this.baseFeature = baseFeature;
		this.prefix = prefix;
		this.shortName = shortName;
		this.isEvaluation = isEvaluation;
		this.normaliser = normaliser;
	}

	private ProfileFeatureEnum(ProfileFeatureEnum baseFeature, Prefix prefix, String shortName, boolean isEvaluation) {
		this.baseFeature = baseFeature;
		this.prefix = prefix;
		this.shortName = shortName;
		this.isEvaluation = isEvaluation;
	}

	private Prefix prefix;

	private String shortName;

	private boolean isEvaluation;

	public String getURI() {
		return prefix.getUrl() + getName();
	}

	public Prefix getPrefix() {
		return prefix;
	}

	public String getName() {
		if (isEvaluation)
			return "Distribution" + StringUtils.capitalize(this.shortName) + "Evaluation";
		else
			return this.shortName;
	}

	public String getShortName() {
		return shortName;
	}

	public boolean isEvaluation() {
		return isEvaluation;
	}

	public static ProfileFeatureEnum getByType(String uri) {

		for (ProfileFeatureEnum feature : values()) {
			if (feature.isEvaluation() && feature.getURI().equals(uri))
				return feature;
		}

		return null;
	}

	public static ProfileFeatureEnum getByPropertyURI(String uri) {

		for (ProfileFeatureEnum feature : values()) {
			if (!feature.isEvaluation() && feature.getURI().equals(uri))
				return feature;
		}

		return null;
	}

	public ProfileFeatureEnum getNormaliser() {
		return normaliser;
	}

	public ProfileFeatureEnum getBaseFeature() {
		return baseFeature;
	}

}