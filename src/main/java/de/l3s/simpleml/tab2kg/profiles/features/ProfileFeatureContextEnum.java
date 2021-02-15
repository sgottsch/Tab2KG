package de.l3s.simpleml.tab2kg.profiles.features;

import de.l3s.simpleml.tab2kg.model.sparql.Prefix;

public enum ProfileFeatureContextEnum {

	HISTOGRAM_BUCKET_MIN(Prefix.ANON, "bucketMinimum"), HISTOGRAM_BUCKET_MAX(Prefix.ANON, "bucketMaximum"),
	COMMENT(Prefix.ANON, "comment");

	private ProfileFeatureContextEnum(Prefix prefix, String name) {
		this.prefix = prefix;
		this.name = name;
	}

	private Prefix prefix;

	private String name;

	public Prefix getPrefix() {
		return prefix;
	}

	public String getName() {
		return name;
	}
	
	public String getURI() {
		return prefix.getUrl() + getName();
	}

	public static ProfileFeatureContextEnum getByPropertyURI(String uri) {

		for (ProfileFeatureContextEnum contextEnum : values())
			if (contextEnum.getURI().equals(uri))
				return contextEnum;

		return null;
	}

}