package de.l3s.simpleml.tab2kg.profiles.features;

import java.util.HashSet;
import java.util.Set;

import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;

public abstract class ProfileFeature {

	private ProfileFeatureEnum profileFeatureEnum;

	private DataTypeClass dataTypeClass;

	private Integer rank;

	private Set<FeatureContext<?>> featureContexts = new HashSet<FeatureContext<?>>();

	public ProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass) {
		super();
		this.profileFeatureEnum = profileFeatureEnum;
		this.dataTypeClass = dataTypeClass;
	}

	public ProfileFeature(ProfileFeatureEnum profileFeatureEnum, Integer rank) {
		super();
		this.profileFeatureEnum = profileFeatureEnum;
		this.rank = rank;
	}

	public ProfileFeature(ProfileFeatureEnum profileFeatureEnum, DataTypeClass dataTypeClass, Integer rank) {
		super();
		this.profileFeatureEnum = profileFeatureEnum;
		this.dataTypeClass = dataTypeClass;
		this.rank = rank;
	}

	public ProfileFeatureEnum getProfileFeatureEnum() {
		return profileFeatureEnum;
	}

	public DataTypeClass getDataTypeClass() {
		return dataTypeClass;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public Set<FeatureContext<?>> getFeatureContexts() {
		return featureContexts;
	}

	public void addFeatureContext(FeatureContext<?> featureContext) {
		this.featureContexts.add(featureContext);
	}

	public abstract double getDoubleValue();

	public abstract ProfileFeature copy();

}
