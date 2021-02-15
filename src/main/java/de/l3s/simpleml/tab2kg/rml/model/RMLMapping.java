package de.l3s.simpleml.tab2kg.rml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RMLMapping {

	private String mappingString;

	private String sourceFileName;

	private List<RMLSubjectMap> types = new ArrayList<RMLSubjectMap>();

	private Map<String, String> prefixes;

	private String delimiter;

	public String getMappingString() {
		return mappingString;
	}

	public void setMappingString(String mappingString) {
		this.mappingString = mappingString;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

	public List<RMLSubjectMap> getTypes() {
		return types;
	}

	public void addType(RMLSubjectMap type) {
		this.types.add(type);
	}

	public Map<String, String> getPrefixes() {
		return prefixes;
	}

	public void setPrefixes(Map<String, String> prefixes) {
		this.prefixes = prefixes;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

}
