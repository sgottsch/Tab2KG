package de.l3s.simpleml.tab2kg.util;

public enum FileLocation {

	TMP_MAPPING("tmp/mapping.rml"), TMP_GRAPH("tmp/graph.ttl"), BASE_FOLDER("data/"),
	FUNCTIONS_TTL("data/functions.ttl"), SIMPLE_GRAPHS_FOLDER("data/simple_graphs/$tt$/"),
	SIMPLE_GRAPHS_ALL_FOLDER("data/simple_graphs/"), COLUMN_MATCHING_MODEL_FOLDER("column_matching/model/"),COLUMN_MATCHING_FOLDER("column_matching/$tt$/"),
	GRAPH_MATCHING_STATS_FOLDER("data/graph_matching/"), TABLE_MATCHING_FOLDER("data/table_matching/$tt$/"),
	GRAPH_MATCHING_FOLDER("data/graph_matching/$tt$/"), OUTPUT_GRAPH_PAIRS_FOLDER("data/graph_pairs/"),
	SUB_AND_FILE_GRAPHS_FOLDER("data/sub_and_file_graphs/$tt$/"),
	PROCESSED_FILES_FILE("data/github/processed_files_$time$.tsv"), CSV_OUTPUT_FILE("data/input.csv"),
	CSV_OUTPUT_LABELS_FILE("data/labels.csv"), CSV_OUTPUT_TEST_FILE("data/input_test.csv"),
	CSV_OUTPUT_LABELS_TEST_FILE("data/labels_test.csv"), JSON_OUTPUT_FOLDER("data/jsons/"),
	OUTPUT_FOLDER("data/created_files/"), DOWNLOADED_FILES("data/downloaded_files/"),
	GITHUB_RAW_FILES_FOLDER("data/github/raw_files/"), GITHUB_FILES_FOLDER("data/github/files/"),
	GITHUB_FOLDER("data/github/"), GITHUB_API_FOLDER("data/github/api/"),
	GITHUB_RAW_DESCRIPTIONS_FOLDER("data/github/raw_descriptions/"),
	GITHUB_DESCRIPTIONS_FOLDER("data/github/descriptions/"), ODP_FILES_FOLDER("data/odp/files/"),
	DISTRIBUTIONS_FOLDER("data/odp/distributions/"), DISTRIBUTIONS_FILE("data/odp/distributions.tsv"),
	LABELS_FOLDER("labels/"), SELECTED_FILES("data/downloaded_files/selection/"),
	WORD2VEC("data/GoogleNews-vectors-negative300.bin.gz"), RESULTS("evaluation/results/"),
	GS_CLASSES("data/gold_standard_classes.csv");

	private String serverPath;

	private FileLocation(String serverPath) {
		this.serverPath = serverPath;
	}

	public String getServerPath() {
		return serverPath;
	}

	public void setServerPath(String serverPath) {
		this.serverPath = serverPath;
	}

}