package de.l3s.simpleml.tab2kg.rml.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Resource;

public class RMLSubjectMap {

	private String template;

	private Resource rdfClass;

	private List<RMLLiteralPredicateObjectMap> literalMaps = new ArrayList<RMLLiteralPredicateObjectMap>();
	private List<RMLPredicateObjectMap> relationMaps = new ArrayList<RMLPredicateObjectMap>();

	private String identifier;

	public RMLSubjectMap(Resource resource) {
		super();
		this.rdfClass = resource;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public Resource getRdfClass() {
		return rdfClass;
	}

	public void setRdfClass(Resource rdfClass) {
		this.rdfClass = rdfClass;
	}

	public List<RMLLiteralPredicateObjectMap> getLiteralMaps() {
		return literalMaps;
	}

	public void addLiteralMap(RMLLiteralPredicateObjectMap literalMap) {
		this.literalMaps.add(literalMap);
	}

	public List<RMLPredicateObjectMap> getRelationMaps() {
		return relationMaps;
	}

	public void addRelationMap(RMLPredicateObjectMap relationMap) {
		this.relationMaps.add(relationMap);
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

}
