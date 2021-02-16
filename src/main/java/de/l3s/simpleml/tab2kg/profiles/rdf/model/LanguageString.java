package de.l3s.simpleml.tab2kg.profiles.rdf.model;

import de.l3s.simpleml.tab2kg.model.sparql.Language;

public class LanguageString {

	private Language language;
	private String value;

	public LanguageString(Language language, String value) {
		super();
		this.language = language;
		this.value = value;
	}

	public Language getLanguage() {
		return language;
	}

	public String getValue() {
		return value;
	}

	public String getString() {
		return "\"" + value + "\"@" + this.language.getLanguageTag();
	}
}
