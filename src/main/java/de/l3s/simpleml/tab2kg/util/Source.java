package de.l3s.simpleml.tab2kg.util;

public enum Source {

	GITHUB, SOCCER, WEAPONS, SEMTAB, SEMTAB_EASY;

	public String getFolderName() {
		return toString().toLowerCase() + "/";
	}

	public String getName() {
		return toString().toLowerCase();
	}
}
