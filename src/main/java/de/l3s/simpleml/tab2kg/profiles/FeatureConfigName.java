package de.l3s.simpleml.tab2kg.profiles;

public enum FeatureConfigName {

	// ALL("all_with_embeddings", true, true, true),
	ALL("all", false, true, true, true, true), NO_DISTRIBUTIONS("no_distributions", false, false, true, true, true),
	NO_BASIC_STATISTICS("no_basic_statistics", false, true, false, true, true),
	NO_COMPLETENESS("no_completeness", false, true, true, false, true),
	NO_DATATYPES("no_datatypes", false, true, true, true, false);

	FeatureConfigName(String name, boolean useEmbeddings, boolean useDistributions, boolean useBasicStatistics,
			boolean useCompleteness, boolean useDatatypes) {
		this.name = name;
		this.useEmbeddings = useEmbeddings;
		this.useDistributions = useDistributions;
		this.useBasicStatistics = useBasicStatistics;
		this.useCompleteness = useCompleteness;
		this.useDatatypes = useDatatypes;
	}

	private String name;
	private boolean useEmbeddings;
	private boolean useDistributions;
	private boolean useBasicStatistics;
	private boolean useCompleteness;
	private boolean useDatatypes;

	public String getName() {
		return name;
	}

	public boolean useEmbeddings() {
		return this.useEmbeddings;
	}

	public boolean useDistributions() {
		return this.useDistributions;
	}

	public boolean useBasicStatistics() {
		return this.useBasicStatistics;
	}

	public boolean useCompleteness() {
		return useCompleteness;
	}

	public boolean useDatatypes() {
		return useDatatypes;
	}

}