package de.l3s.simpleml.tab2kg.profiles.features;

import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;

public class FeatureContext<T> {

	private DataTypeClass dataType;
	private T value;
	private ProfileFeatureContextEnum profileFeatureContextEnum;

	public FeatureContext(DataTypeClass dataType, T value, ProfileFeatureContextEnum profileFeatureContextEnum) {
		super();
		this.dataType = dataType;
		this.value = value;
		this.profileFeatureContextEnum = profileFeatureContextEnum;
	}

	public DataTypeClass getDataType() {
		return dataType;
	}

	public T getValue() {
		return value;
	}

	public ProfileFeatureContextEnum getProfileFeatureContextEnum() {
		return profileFeatureContextEnum;
	}

}
