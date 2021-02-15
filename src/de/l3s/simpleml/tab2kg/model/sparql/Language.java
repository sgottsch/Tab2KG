package de.l3s.simpleml.tab2kg.model.sparql;

public enum Language {

	DE("de"), EN("en");

	private String languageTag;

	public static Language getLanguageByTag(String languageTag) {
		for (Language language : Language.values()) {
			if (language.getLanguageTag().equals(languageTag))
				return language;
		}
		return null;
		// throw new IllegalArgumentException("Language tag " + languageTag + "
		// does not exist.");
	}

	private Language(String languageTag) {
		this.languageTag = languageTag;
	}

	public String getLanguageTag() {
		return languageTag;
	}

}
