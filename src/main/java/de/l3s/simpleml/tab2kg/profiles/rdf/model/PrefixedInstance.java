package de.l3s.simpleml.tab2kg.profiles.rdf.model;

import de.l3s.simpleml.tab2kg.model.sparql.Prefix;

public class PrefixedInstance {
	private Prefix prefix;
	private String id;
	private String uri;

	public PrefixedInstance(Prefix prefix, String id, Prefix basePrefix) {
		super();
		this.prefix = prefix;
		this.id = id;

		if (this.prefix == basePrefix)
			this.uri = "<" + this.id + ">";
		else
			this.uri = prefix.getPrefix() + this.id;
	}

	public Prefix getPrefix() {
		return prefix;
	}

	public String getId() {
		return id;
	}

	public String getUri() {
		return uri;
	}

}
