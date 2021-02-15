package de.l3s.simpleml.tab2kg.profiles.features;

public class ProfileFeaturePlaceholder {
	private ProfileFeatureEnum profileFeatureEnum;

	private Integer rank;

	public ProfileFeaturePlaceholder(ProfileFeatureEnum profileFeatureEnum) {
		super();
		this.profileFeatureEnum = profileFeatureEnum;
	}

	public ProfileFeaturePlaceholder(ProfileFeatureEnum profileFeatureEnum, Integer rank) {
		super();
		this.profileFeatureEnum = profileFeatureEnum;
		this.rank = rank;
	}

	public ProfileFeatureEnum getProfileFeatureEnum() {
		return profileFeatureEnum;
	}

	public void setProfileFeatureEnum(ProfileFeatureEnum profileFeatureEnum) {
		this.profileFeatureEnum = profileFeatureEnum;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	@Override
	public String toString() {
		if (this.rank == null)
			return this.profileFeatureEnum.getShortName();
		else
			return this.profileFeatureEnum.getShortName() + "-" + this.rank;
	}

}
