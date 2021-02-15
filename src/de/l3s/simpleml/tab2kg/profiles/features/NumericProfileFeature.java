package de.l3s.simpleml.tab2kg.profiles.features;

import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;

public class NumericProfileFeature<T extends Number> extends ProfileFeature {

	private Number value;

	public NumericProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass, T value) {
		super(profileFeatureEnum, dataTypeClass);
		this.value = value;
	}

	public NumericProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass, T value,
			FeatureContext<?> featureContext) {
		super(profileFeatureEnum, dataTypeClass);
		this.value = value;
		addFeatureContext(featureContext);
	}

	public NumericProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass, T value,
			Integer rank) {
		super(profileFeatureEnum, dataTypeClass, rank);
		this.value = value;
	}

	public NumericProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass, T value,
			Integer rank, FeatureContext<?> featureContext) {
		super(profileFeatureEnum, dataTypeClass, rank);
		this.value = value;
		addFeatureContext(featureContext);
	}

	public void setValue(Number value) {
		this.value = value;
	}

	public Number getValue() {
		return value;
	}

	public int getIntValue() {
		return (Integer) value;
	}

	@Override
	public double getDoubleValue() {
		return value.doubleValue();
	}

	public long getLongValue() {
		return (Long) value;
	}

	@Override
	public NumericProfileFeature<Number> copy() {
		NumericProfileFeature<Number> copy = new NumericProfileFeature<Number>(getProfileFeatureEnum(),
				getDataTypeClass(), this.value);
		copy.setRank(getRank());

		return copy;
	}

}
