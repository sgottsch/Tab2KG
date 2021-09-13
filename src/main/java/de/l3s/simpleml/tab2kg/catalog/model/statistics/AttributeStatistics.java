package de.l3s.simpleml.tab2kg.catalog.model.statistics;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.jena.rdf.model.Literal;

import de.l3s.simpleml.tab2kg.catalog.model.AttributeValue;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.profiles.features.FeatureContext;
import de.l3s.simpleml.tab2kg.profiles.features.NumericProfileFeature;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeature;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeatureContextEnum;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeatureEnum;
import de.l3s.simpleml.tab2kg.profiles.features.TemporalProfileFeature;

public class AttributeStatistics<T> {

	private List<T> valueList = null;
	private List<T> valueListWithNulls = null;
	private List<AttributeValue<T>> attributeValues = new ArrayList<AttributeValue<T>>();

	private Map<T, Integer> valueDistribution = new HashMap<T, Integer>();

	private AttributeStatistics<Integer> wordLengthStatistics;
	private AttributeStatistics<Double> geoDimensionStatistics;
	private AttributeStatistics<Long> millisecondsStatistics;

	private Boolean isWKB = null;
	private boolean isCategorical = false;

	private AttributeStatisticsTypeL2 attributeStatisticsType;
	private AttributeStatisticsTypeL3 attributeStatisticsTypeL3;

	private List<ProfileFeature> profileFeatures = new ArrayList<ProfileFeature>();
	private Map<ProfileFeatureEnum, Map<Integer, ProfileFeature>> profileFeaturesByEnum = new HashMap<ProfileFeatureEnum, Map<Integer, ProfileFeature>>();

	private List<NumericProfileFeature<?>> numericProfileFeatures = new ArrayList<NumericProfileFeature<?>>();
	private Map<ProfileFeatureEnum, Map<Integer, NumericProfileFeature<?>>> numericProfileFeaturesByEnum = new HashMap<ProfileFeatureEnum, Map<Integer, NumericProfileFeature<?>>>();

	private List<TemporalProfileFeature> temporalProfileFeatures = new ArrayList<TemporalProfileFeature>();
	private Map<ProfileFeatureEnum, Map<Integer, TemporalProfileFeature>> temporalProfileFeaturesByEnum = new HashMap<ProfileFeatureEnum, Map<Integer, TemporalProfileFeature>>();

	public AttributeStatistics(AttributeStatisticsTypeL2 attributeStatisticsType) {
		super();
		this.attributeStatisticsType = attributeStatisticsType;
		addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, 0));
		addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_NULL_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, 0));
		addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_VALID_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, 0));
		addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_VALID_NON_NULL_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, 0));
		addNumericProfileFeature(new NumericProfileFeature<Integer>(ProfileFeatureEnum.NUMBER_OF_INVALID_VALUES,
				DataTypeClass.XS_NON_NEGATIVE_INTEGER, 0));
	}

	public Integer getNumberOfNullValues() {
		return getNumericFeature(ProfileFeatureEnum.NUMBER_OF_NULL_VALUES).getIntValue();
	}

	public void addInvalidValue() {
		// addToValueListWithNulls(null);

		AttributeValue<T> value = new AttributeValue<T>(false, true);
		this.attributeValues.add(value);

		increaseIntValue(ProfileFeatureEnum.NUMBER_OF_INVALID_VALUES);
		increaseIntValue(ProfileFeatureEnum.NUMBER_OF_VALUES);
	}

	public void addNullValue() {
		// addToValueListWithNulls(null);

		AttributeValue<T> value = new AttributeValue<T>(true, false);
		this.attributeValues.add(value);

		increaseIntValue(ProfileFeatureEnum.NUMBER_OF_NULL_VALUES);
		increaseIntValue(ProfileFeatureEnum.NUMBER_OF_VALID_VALUES);
		increaseIntValue(ProfileFeatureEnum.NUMBER_OF_VALUES);
	}

	public void addValidNonNullValue() {
		increaseIntValue(ProfileFeatureEnum.NUMBER_OF_VALID_NON_NULL_VALUES);
		increaseIntValue(ProfileFeatureEnum.NUMBER_OF_VALID_VALUES);
		increaseIntValue(ProfileFeatureEnum.NUMBER_OF_VALUES);
	}

	public AttributeStatistics<T> copy() {

		AttributeStatistics<T> copy = new AttributeStatistics<T>(this.attributeStatisticsType);

		// Copy feature values
		for (TemporalProfileFeature profileFeature : this.temporalProfileFeatures) {
			copy.addTemporalProfileFeature(profileFeature.copy());
		}
		for (NumericProfileFeature<?> profileFeature : this.numericProfileFeatures) {
			copy.addNumericProfileFeature(profileFeature.copy());
		}

		if (wordLengthStatistics != null)
			copy.setWordLengthStatistics(wordLengthStatistics.copy());
		if (geoDimensionStatistics != null)
			copy.setGeoDimensionStatistics(geoDimensionStatistics.copy());
		if (millisecondsStatistics != null)
			copy.setMilliSecondsStatistics(millisecondsStatistics.copy());

		copy.setIsWKB(isWKB);

		return copy;
	}

	public boolean isNumeric() {
		return this.attributeStatisticsType.getTypeL1() == AttributeStatisticsTypeL1.NUMERIC;
	}

	public boolean isTemporal() {
		return this.attributeStatisticsType.getTypeL1() == AttributeStatisticsTypeL1.TEMPORAL;
	}

//	public void setNumeric(boolean isNumeric) {
//		this.isNumeric = isNumeric;
//	}

	public AttributeStatisticsTypeL2 getAttributeStatisticsType() {
		return attributeStatisticsType;
	}

	public Integer getNumberOfDistinctValues() {
		NumericProfileFeature<?> feature = getNumericFeature(ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES);
		if (feature == null)
			return null;

		return feature.getIntValue();
	}

	public void setAttributeStatisticsType(AttributeStatisticsTypeL2 attributeStatisticsType) {
		this.attributeStatisticsType = attributeStatisticsType;
	}

	public Integer getNumberOfNonNullValues() {
		return getNumericFeature(ProfileFeatureEnum.NUMBER_OF_VALID_NON_NULL_VALUES).getIntValue();
	}

	public NumericProfileFeature<?> setOrUpdateDoubleFeatureValue(ProfileFeatureEnum profileFeatureEnum, Integer rank,
			double value) {
		NumericProfileFeature<?> feature = getNumericFeature(profileFeatureEnum, rank);
		if (feature == null) {
			feature = new NumericProfileFeature<Double>(profileFeatureEnum, DataTypeClass.XS_DOUBLE, value, rank);
			addNumericProfileFeature(feature);
		} else
			feature.setValue(value);
		return feature;
	}

	public NumericProfileFeature<?> setOrUpdateDoubleFeatureValue(ProfileFeatureEnum profileFeatureEnum, double value) {
		return setOrUpdateDoubleFeatureValue(profileFeatureEnum, null, value);
	}

	public NumericProfileFeature<?> setOrUpdateIntegerFeatureValue(ProfileFeatureEnum profileFeatureEnum, Integer rank,
			int value, DataTypeClass dataTypeClass) {
		NumericProfileFeature<?> feature = getNumericFeature(profileFeatureEnum, rank);
		if (feature == null) {
			feature = new NumericProfileFeature<Integer>(profileFeatureEnum, dataTypeClass, value, rank);
			addNumericProfileFeature(feature);
		} else
			feature.setValue(value);

		return feature;
	}

	public NumericProfileFeature<?> setOrUpdateLongFeatureValue(ProfileFeatureEnum profileFeatureEnum, Integer rank,
			long value) {
		NumericProfileFeature<?> feature = getNumericFeature(profileFeatureEnum, rank);
		if (feature == null) {
			feature = new NumericProfileFeature<Long>(profileFeatureEnum, DataTypeClass.XS_LONG, value, rank);
			addNumericProfileFeature(feature);
		} else
			feature.setValue(value);

		return feature;
	}

	public TemporalProfileFeature setOrUpdateTemporalFeatureValue(ProfileFeatureEnum profileFeatureEnum, Integer rank,
			Date value, DataTypeClass dataTypeClass) {
		TemporalProfileFeature feature = getTemporalFeature(profileFeatureEnum, rank);
		if (feature == null) {
			feature = new TemporalProfileFeature(profileFeatureEnum, dataTypeClass, value, rank);
			addTemporalProfileFeature(feature);
		} else
			feature.setValue(value);

		return feature;
	}

//	public void setOrUpdateNNIntegerFeatureValue(ProfileFeatureEnum profileFeatureEnum, int value) {
//		NumericProfileFeature<?> feature = getNumericFeature(profileFeatureEnum);
//		if (feature == null)
//			addNumericProfileFeature(new NumericProfileFeature<Integer>(profileFeatureEnum,
//					DataTypeClass.XS_NON_NEGATIVE_INTEGER, value));
//		else
//			feature.setValue(value);
//	}

	public int getNumberOfValues() {
		return getNumericFeature(ProfileFeatureEnum.NUMBER_OF_NULL_VALUES).getIntValue();
	}

	public int getNumberOfValidValues() {
		return getNumericFeature(ProfileFeatureEnum.NUMBER_OF_VALID_VALUES).getIntValue();
	}

	public int getNumberOfValidNonNullValues() {
		return getNumericFeature(ProfileFeatureEnum.NUMBER_OF_VALID_NON_NULL_VALUES).getIntValue();
	}

	public int getNumberOfInvalidNonNullValues() {
		return getNumericFeature(ProfileFeatureEnum.NUMBER_OF_INVALID_VALUES).getIntValue();
	}

	public void addNumericProfileFeature(NumericProfileFeature<?> feature) {
		this.numericProfileFeatures.add(feature);

		if (!this.numericProfileFeaturesByEnum.containsKey(feature.getProfileFeatureEnum()))
			this.numericProfileFeaturesByEnum.put(feature.getProfileFeatureEnum(),
					new HashMap<Integer, NumericProfileFeature<?>>());

		this.numericProfileFeaturesByEnum.get(feature.getProfileFeatureEnum()).put(feature.getRank(), feature);

		addProfileFeature(feature);
	}

	public void addTemporalProfileFeature(TemporalProfileFeature feature) {
		this.temporalProfileFeatures.add(feature);

		if (!this.temporalProfileFeaturesByEnum.containsKey(feature.getProfileFeatureEnum()))
			this.temporalProfileFeaturesByEnum.put(feature.getProfileFeatureEnum(),
					new HashMap<Integer, TemporalProfileFeature>());

		this.temporalProfileFeaturesByEnum.get(feature.getProfileFeatureEnum()).put(feature.getRank(), feature);

		addProfileFeature(feature);
	}

	public void addProfileFeature(ProfileFeature feature) {
		this.profileFeatures.add(feature);

		if (!this.profileFeaturesByEnum.containsKey(feature.getProfileFeatureEnum()))
			this.profileFeaturesByEnum.put(feature.getProfileFeatureEnum(), new HashMap<Integer, ProfileFeature>());

		this.profileFeaturesByEnum.get(feature.getProfileFeatureEnum()).put(feature.getRank(), feature);
	}

	public List<ProfileFeature> getProfileFeatures() {
		return profileFeatures;
	}

	public List<NumericProfileFeature<?>> getNumericProfileFeatures() {
		return numericProfileFeatures;
	}

	public List<TemporalProfileFeature> getTemporalProfileFeatures() {
		return temporalProfileFeatures;
	}

	public NumericProfileFeature<?> getNumericFeature(ProfileFeatureEnum profileFeatureEnum) {

		if (!this.numericProfileFeaturesByEnum.containsKey(profileFeatureEnum))
			return null;

		return this.numericProfileFeaturesByEnum.get(profileFeatureEnum).get(null);
	}

	public ProfileFeature getProfileFeature(ProfileFeatureEnum profileFeatureEnum, Integer rank) {
		if (!this.profileFeaturesByEnum.containsKey(profileFeatureEnum))
			return null;

		return this.profileFeaturesByEnum.get(profileFeatureEnum).get(rank);
	}

	public TemporalProfileFeature getTemporalFeature(ProfileFeatureEnum profileFeatureEnum) {
		if (!this.temporalProfileFeaturesByEnum.containsKey(profileFeatureEnum))
			return null;

		return this.temporalProfileFeaturesByEnum.get(profileFeatureEnum).get(null);
	}

	public TemporalProfileFeature getTemporalFeature(ProfileFeatureEnum profileFeatureEnum, Integer rank) {
		for (TemporalProfileFeature feature : this.temporalProfileFeatures) {
			if (feature.getProfileFeatureEnum() == profileFeatureEnum && feature.getRank() == rank)
				return feature;
		}
		return null;
	}

	public NumericProfileFeature<?> getNumericFeature(ProfileFeatureEnum profileFeatureEnum, Integer rank) {
		for (NumericProfileFeature<?> feature : this.numericProfileFeatures) {
			if (feature.getProfileFeatureEnum() == profileFeatureEnum && feature.getRank().equals(rank))
				return feature;
		}
		return null;
	}

	public void increaseIntValue(ProfileFeatureEnum profileFeatureEnum) {
		NumericProfileFeature<?> feature = getNumericFeature(profileFeatureEnum);
		getNumericFeature(profileFeatureEnum).setValue(feature.getIntValue() + 1);
	}

	public void setOrUpdateLiteralFeatureValue(ProfileFeatureEnum profileFeatureEnum, Literal literal) {
		setOrUpdateLiteralFeatureValue(profileFeatureEnum, null, literal);
	}

	public ProfileFeature setOrUpdateLiteralFeatureValue(ProfileFeatureEnum profileFeatureEnum, Integer rank,
			Literal literal) {

		DataTypeClass dataTypeClass = DataTypeClass.getDataTypeClassByURI(literal.getDatatypeURI());

		if (dataTypeClass == DataTypeClass.XS_STRING) {
			System.out.println("Warning: No strings in setOrUpdateLiteralFeatureValue? Check!");
		}

		if (dataTypeClass == DataTypeClass.XS_DATE) {
			Date date = DatatypeConverter.parseDate(literal.getString()).getTime();
			return setOrUpdateTemporalFeatureValue(profileFeatureEnum, rank, date, dataTypeClass);
		} else if (dataTypeClass == DataTypeClass.XS_DATE_TIME) {
			Date date = DatatypeConverter.parseDateTime(literal.getString()).getTime();
			return setOrUpdateTemporalFeatureValue(profileFeatureEnum, rank, date, dataTypeClass);
		} else if (dataTypeClass == DataTypeClass.XS_TIME) {
			Date date = DatatypeConverter.parseTime(literal.getString()).getTime();
			return setOrUpdateTemporalFeatureValue(profileFeatureEnum, rank, date, dataTypeClass);
		} else if (dataTypeClass == DataTypeClass.XS_LONG) {
			return setOrUpdateLongFeatureValue(profileFeatureEnum, rank, literal.getLong());
		} else if (dataTypeClass == DataTypeClass.XS_DOUBLE) {
			return setOrUpdateDoubleFeatureValue(profileFeatureEnum, rank, literal.getDouble());
		} else if (dataTypeClass == DataTypeClass.XS_INTEGER || dataTypeClass == DataTypeClass.XS_NON_NEGATIVE_INTEGER
				|| dataTypeClass == DataTypeClass.XS_NON_POSITIVE_INTEGER
				|| dataTypeClass == DataTypeClass.XS_NEGATIVE_INTEGER) {
			return setOrUpdateIntegerFeatureValue(profileFeatureEnum, rank, literal.getInt(), dataTypeClass);
		}

		return null;
	}

	public void addContext(ProfileFeature feature, ProfileFeatureContextEnum contextEnum, Literal literal) {

		DataTypeClass dataTypeClass = DataTypeClass.getDataTypeClassByURI(literal.getDatatypeURI());

		if (dataTypeClass == DataTypeClass.XS_DATE) {
			Date date = DatatypeConverter.parseDate(literal.getString()).getTime();
			FeatureContext<Date> featureContext = new FeatureContext<Date>(dataTypeClass, date, contextEnum);
			feature.addFeatureContext(featureContext);
		} else if (dataTypeClass == DataTypeClass.XS_DATE_TIME) {
			Date date = DatatypeConverter.parseDateTime(literal.getString()).getTime();
			FeatureContext<Date> featureContext = new FeatureContext<Date>(dataTypeClass, date, contextEnum);
			feature.addFeatureContext(featureContext);
		} else if (dataTypeClass == DataTypeClass.XS_TIME) {
			Date date = DatatypeConverter.parseTime(literal.getString()).getTime();
			FeatureContext<Date> featureContext = new FeatureContext<Date>(dataTypeClass, date, contextEnum);
			feature.addFeatureContext(featureContext);
		} else if (dataTypeClass == DataTypeClass.XS_LONG) {
			FeatureContext<Long> featureContext = new FeatureContext<Long>(dataTypeClass, literal.getLong(),
					contextEnum);
			feature.addFeatureContext(featureContext);
		} else if (dataTypeClass == DataTypeClass.XS_DOUBLE) {
			FeatureContext<Double> featureContext = new FeatureContext<Double>(dataTypeClass, literal.getDouble(),
					contextEnum);
			feature.addFeatureContext(featureContext);
		} else if (dataTypeClass == DataTypeClass.XS_STRING) {
			FeatureContext<String> featureContext = new FeatureContext<String>(dataTypeClass, literal.getString(),
					contextEnum);
			feature.addFeatureContext(featureContext);
		} else if (dataTypeClass == DataTypeClass.XS_INTEGER || dataTypeClass == DataTypeClass.XS_NON_NEGATIVE_INTEGER
				|| dataTypeClass == DataTypeClass.XS_NON_POSITIVE_INTEGER
				|| dataTypeClass == DataTypeClass.XS_NEGATIVE_INTEGER) {
			FeatureContext<Integer> featureContext = new FeatureContext<Integer>(dataTypeClass, literal.getInt(),
					contextEnum);
			feature.addFeatureContext(featureContext);
		}
	}

//	public void addContext(ProfileFeatureEnum profileFeatureEnum, Integer rank, ProfileFeatureContextEnum contextEnum,
//			Literal literal) {
//
//		DataTypeClass dataTypeClass = DataTypeClass.getDataTypeClassByURI(literal.getDatatypeURI());
//
//		if (dataTypeClass == DataTypeClass.XS_DATE) {
//			Date date = DatatypeConverter.parseDate(literal.getString()).getTime();
//			addContext(profileFeatureEnum, rank, date, dataTypeClass, contextEnum);
//		} else if (dataTypeCl = ass = DataTypeClass.XS_DATE_TIME) {
//			Date date = DatatypeConverter.parseDateTime(literal.getString()).getTime();
//			addContext(profileFeatureEnum, rank, date, dataTypeClass, contextEnum);
//		} else if (dataTypeClass == DataTypeClass.XS_TIME) {
//			Date date = DatatypeConverter.parseTime(literal.getString()).getTime();
//			addContext(profileFeatureEnum, rank, date, dataTypeClass, contextEnum);
//		} else if (dataTypeClass == DataTypeClass.XS_LONG) {
//			addContext(profileFeatureEnum, rank, literal.getLong(), contextEnum);
//		} else if (dataTypeClass == DataTypeClass.XS_DOUBLE) {
//			addContext(profileFeatureEnum, rank, literal.getDouble(), contextEnum);
//		} else if (dataTypeClass == DataTypeClass.XS_INTEGER || dataTypeClass == DataTypeClass.XS_NON_NEGATIVE_INTEGER
//				|| dataTypeClass == DataTypeClass.XS_NON_POSITIVE_INTEGER
//				|| dataTypeClass == DataTypeClass.XS_NEGATIVE_INTEGER) {
//			addContext(profileFeatureEnum, rank, literal.getInt(), dataTypeClass, contextEnum);
//		}
//	}

	public void addToValueList(T value, String originalValue) {
//		this.valueList.add(value);
//		this.valueListWithNulls.add(value);

		AttributeValue<T> attributeValue = new AttributeValue<T>(value, originalValue);
		if (value instanceof Boolean)
			attributeValue.setStringValue(String.valueOf(value));
		else if (value instanceof String)
			attributeValue.setStringValue((String) value);

		this.attributeValues.add(attributeValue);
	}

//	public void addToValueListWithNulls(T value) {
//		this.valueListWithNulls.add(value);
//	}

	public Boolean isWKB() {
		return isWKB;
	}

	public void setIsWKB(Boolean isWKB) {
		this.isWKB = isWKB;
	}

	public AttributeStatistics<Integer> getWordLengthStatistics() {
		return wordLengthStatistics;
	}

	public void setWordLengthStatistics(AttributeStatistics<Integer> wordLengthStatistics) {
		this.wordLengthStatistics = wordLengthStatistics;
	}

	public AttributeStatistics<Double> getGeoDimensionStatistics() {
		return geoDimensionStatistics;
	}

	public void setGeoDimensionStatistics(AttributeStatistics<Double> geoDimensionStatistics) {
		this.geoDimensionStatistics = geoDimensionStatistics;
	}

	public AttributeStatistics<Long> getMillisecondsStatistics() {
		return millisecondsStatistics;
	}

	public void setMilliSecondsStatistics(AttributeStatistics<Long> millisecondsStatistics) {
		this.millisecondsStatistics = millisecondsStatistics;
	}

	public void updateValueList() {

		this.valueList = new ArrayList<T>();

		for (AttributeValue<T> value : this.attributeValues) {
			if (value.isValidNonNull())
				this.valueList.add(value.getValue());
		}

		if (this.wordLengthStatistics != null) {
			this.wordLengthStatistics.updateValueList();
		}

		if (this.geoDimensionStatistics != null) {
			this.geoDimensionStatistics.updateValueList();
		}

		if (this.millisecondsStatistics != null) {
			this.millisecondsStatistics.updateValueList();
		}

	}

	public void updateValueListWithNulls() {

		this.valueListWithNulls = new ArrayList<T>();

		for (AttributeValue<T> value : this.attributeValues) {
			if (value.isValidNonNull())
				this.valueListWithNulls.add(value.getValue());
			else
				this.valueListWithNulls.add(null);

		}

		if (this.wordLengthStatistics != null)
			this.wordLengthStatistics.updateValueListWithNulls();

		if (this.geoDimensionStatistics != null)
			this.geoDimensionStatistics.updateValueListWithNulls();

		if (this.millisecondsStatistics != null)
			this.millisecondsStatistics.updateValueListWithNulls();

	}

	public List<T> getValueList() {
		return valueList;
	}

	public void increaseCountPerEntity(T key) {
		if (!this.valueDistribution.containsKey(key))
			this.valueDistribution.put(key, 1);
		else
			this.valueDistribution.put(key, this.valueDistribution.get(key) + 1);
	}

	public Map<T, Integer> getValueDistribution() {
		return valueDistribution;
	}

	public List<T> getValueListWithNulls() {
		return valueListWithNulls;
	}

	@SuppressWarnings("unchecked")
	public void addToValueDistribution(Object instance, int numberOfInstances) {
		this.valueDistribution.put((T) instance, numberOfInstances);
	}

	public List<AttributeValue<T>> getAttributeValues() {
		return attributeValues;
	}

	public boolean isCategorical() {
		return isCategorical;
	}

	public void setIsCategorical(Boolean isCategorical) {
		this.isCategorical = isCategorical;
	}

	@SuppressWarnings("unchecked")
	public AttributeStatistics<Number> getNumberStatistics() {

		if (this.isNumeric())
			return (AttributeStatistics<Number>) this;
		else if (this.isTemporal())
			return (AttributeStatistics<Number>) (AttributeStatistics<?>) getMillisecondsStatistics();
		else if (getWordLengthStatistics() != null)
			return (AttributeStatistics<Number>) (AttributeStatistics<?>) getWordLengthStatistics();
		else if (getGeoDimensionStatistics() != null)
			return (AttributeStatistics<Number>) (AttributeStatistics<?>) getGeoDimensionStatistics();

		return null;
	}

	public AttributeStatisticsTypeL3 getAttributeStatisticsTypeL3() {
		return attributeStatisticsTypeL3;
	}

	public void setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3 attributeStatisticsTypeL3) {
		this.attributeStatisticsTypeL3 = attributeStatisticsTypeL3;
	}

}
