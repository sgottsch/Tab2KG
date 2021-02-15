package de.l3s.simpleml.tab2kg.model.rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.l3s.simpleml.tab2kg.model.template.TemplateNode;
import de.l3s.simpleml.tab2kg.model.template.TemplateTriple;

public class QueryTemplate {

	private int id;

	private String query;

	private TemplateNode tree;

	private String countVariableName;

	private List<String> nodePlaceHolders = new ArrayList<String>();

	private List<String> typePlaceHolders = new ArrayList<String>();

	private List<String> propertyPlaceHolders = new ArrayList<String>();

	private List<TemplateTriple> templateTriples = new ArrayList<TemplateTriple>();

	private Set<QueryTemplate> dependentQueryTemplates = new HashSet<QueryTemplate>();

	public QueryTemplate(int id) {
		super();
		this.id = id;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public TemplateNode getTree() {
		return tree;
	}

	public void setTree(TemplateNode tree) {
		this.tree = tree;
	}

	public String getCountVariableName() {
		return countVariableName;
	}

	public void setCountVariableName(String countVariableName) {
		this.countVariableName = countVariableName;
	}

	public List<String> getNodePlaceHolders() {
		return nodePlaceHolders;
	}

	public void setNodePlaceHolders(List<String> nodePlaceHolders) {
		this.nodePlaceHolders = nodePlaceHolders;
	}

	public void addNodePlaceHolder(String nodePlaceHolder) {
		this.nodePlaceHolders.add(nodePlaceHolder);
	}

	public List<String> getTypePlaceHolders() {
		return typePlaceHolders;
	}

	public void setTypePlaceHolders(List<String> typePlaceHolders) {
		this.typePlaceHolders = typePlaceHolders;
	}

	public void addTypePlaceHolder(String typePlaceHolder) {
		this.typePlaceHolders.add(typePlaceHolder);
	}

	public List<String> getPropertyPlaceHolders() {
		return propertyPlaceHolders;
	}

	public void setPropertyPlaceHolders(List<String> propertyPlaceHolders) {
		this.propertyPlaceHolders = propertyPlaceHolders;
	}

	public void addPropertyPlaceHolder(String propertyPlaceHolder) {
		this.propertyPlaceHolders.add(propertyPlaceHolder);
	}

	public List<TemplateTriple> getTemplateTriples() {
		return templateTriples;
	}

	public void setTemplateTriples(List<TemplateTriple> templateTriples) {
		this.templateTriples = templateTriples;
	}

	public void addTemplateTriple(TemplateTriple templateTriple) {
		this.templateTriples.add(templateTriple);
	}

	public Set<QueryTemplate> getDependentQueryTemplates() {
		return dependentQueryTemplates;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
