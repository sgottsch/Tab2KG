package de.l3s.simpleml.tab2kg.profiles.features;

import java.util.Date;

import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;

public class TemporalProfileFeature extends ProfileFeature {

	private Date value;

	public TemporalProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass, Date value) {
		super(profileFeatureEnum, dataTypeClass);
		this.value = value;
	}

	public TemporalProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass, Date value,
			Integer rank) {
		super(profileFeatureEnum, dataTypeClass, rank);
		this.value = value;
	}

	public TemporalProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass, Date value,
			Integer rank, FeatureContext<?> featureContext) {
		super(profileFeatureEnum, dataTypeClass, rank);
		this.value = value;
		addFeatureContext(featureContext);
	}

	public void setValue(Date value) {
		this.value = value;
	}

	public Date getValue() {
		return value;
	}

	public void setIntValue(Date value) {
		this.value = value;
	}

	@Override
	public double getDoubleValue() {
		return Double.valueOf(this.value.getTime());
	}

	@Override
	public TemporalProfileFeature copy() {
		TemporalProfileFeature copy = new TemporalProfileFeature(getProfileFeatureEnum(), getDataTypeClass(),
				this.value);
		copy.setRank(getRank());

		return copy;
	}
}
