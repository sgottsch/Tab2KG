package de.l3s.simpleml.tab2kg.model.rdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.model.template.TemplateNode;

public class QueryGraph {

	private String query;

	private List<RDFClass> types = new ArrayList<RDFClass>();

	private List<RDFClassLiteralTriple> literalTriples = new ArrayList<RDFClassLiteralTriple>();

	private List<RDFClassTriple> classTriples = new ArrayList<RDFClassTriple>();

	private String countVariableName;
	private int frequency;

	private TemplateNode tree;
	private Set<QueryGraph> dependentQueryGraphs = new HashSet<QueryGraph>();
	private QueryTemplate queryTemplate;

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getCountVariableName() {
		return countVariableName;
	}

	public Set<QueryGraph> getDependentQueryGraphs() {
		return dependentQueryGraphs;
	}

	public void setDependentQueryGraphs(Set<QueryGraph> dependentQueryGraphs) {
		this.dependentQueryGraphs = dependentQueryGraphs;
	}

	public void setCountVariableName(String count) {
		this.countVariableName = count;
	}

	public TemplateNode getTree() {
		return tree;
	}

	public void setTree(TemplateNode tree) {
		this.tree = tree;
	}

	public QueryGraph copy() {

		QueryGraph copy = new QueryGraph();
		copy.setQueryTemplate(this.queryTemplate);
		copy.setQuery(query);
		copy.setCountVariableName(countVariableName);
		copy.setFrequency(frequency);

		List<RDFClass> types = new ArrayList<RDFClass>();
		types.addAll(this.types);
		copy.setTypes(types);

		List<RDFClassLiteralTriple> literalTriples = new ArrayList<RDFClassLiteralTriple>();
		literalTriples.addAll(this.literalTriples);
		copy.setLiteralTriples(literalTriples);

		List<RDFClassTriple> classTriples = new ArrayList<RDFClassTriple>();
		classTriples.addAll(this.classTriples);
		copy.setClassTriples(classTriples);

		return copy;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public QueryTemplate getQueryTemplate() {
		return queryTemplate;
	}

	public void setQueryTemplate(QueryTemplate queryTemplate) {
		this.queryTemplate = queryTemplate;
	}

	public List<RDFClass> getTypes() {
		return types;
	}

	public void setTypes(List<RDFClass> types) {
		this.types = types;
	}

	public void addType(RDFClass type) {
		this.types.add(type);
	}

	public List<RDFClassLiteralTriple> getLiteralTriples() {
		return literalTriples;
	}

	public void setLiteralTriples(List<RDFClassLiteralTriple> literalTriples) {
		this.literalTriples = literalTriples;
	}

	public void addLiteralTriple(RDFClassLiteralTriple literalTriple) {
		this.literalTriples.add(literalTriple);
	}

	public List<RDFClassTriple> getClassTriples() {
		return classTriples;
	}

	public void setClassTriples(List<RDFClassTriple> classTriples) {
		this.classTriples = classTriples;
	}

	public void addClassTriple(RDFClassTriple classTriple) {
		this.classTriples.add(classTriple);
	}

	public String getStringRepresentation() {

		Map<Resource, Map<RDFClass, Integer>> instances = new HashMap<Resource, Map<RDFClass, Integer>>();

		String res = "Graph with frequency: " + this.frequency + "\n";

		if (getClassTriples().isEmpty()) {
			if (getTypes().isEmpty())
				res += " ---\n";
			else
				res += " " + getTypes().get(0).getResource().getLocalName() + "\n";
		} else {
			for (RDFClassTriple triple : getClassTriples()) {
				int instanceSubject = getInstance(instances, triple.getSubject());
				int instanceObject = getInstance(instances, triple.getObject());

				List<String> values = new ArrayList<String>();
				values.add(triple.getSubject().getResource().getLocalName() + " (" + instanceSubject + ")");
				values.add(triple.getProperty().getLocalName());
				values.add(triple.getObject().getResource().getLocalName() + " (" + instanceObject + ")");
				res += " " + StringUtils.join(values, ", ") + "\n";
			}
		}

		// for (RDFClass type : this.types) {
		for (RDFClassLiteralTriple literalRelation : getLiteralTriples()) {
			int instanceSubject = getInstance(instances, literalRelation.getSubject());
			res += " " + literalRelation.getSubject().getResource().getLocalName() + " (" + instanceSubject + ") "
					+ literalRelation.getProperty().getLocalName() + "\n";
		}
		// }

		res = StringUtils.strip(res, "\n");

		return res;
	}

	public String getHashRepresentation() {

		Map<Resource, Map<RDFClass, Integer>> instances = new HashMap<Resource, Map<RDFClass, Integer>>();

		List<String> lines = new ArrayList<String>();

		if (getClassTriples().isEmpty()) {
			lines.add("C:" + getTypes().get(0).getResource().getLocalName());
		} else {
			for (RDFClassTriple triple : getClassTriples()) {
				int instanceSubject = getInstance(instances, triple.getSubject());
				int instanceObject = getInstance(instances, triple.getObject());

				List<String> values = new ArrayList<String>();
				values.add(triple.getSubject().getResource().getLocalName() + "_" + instanceSubject);
				values.add(triple.getProperty().getLocalName());
				values.add(triple.getObject().getResource().getLocalName() + "_" + instanceObject);
				lines.add("C:" + StringUtils.join(values, "-"));
			}
		}

		// for (RDFClass type : this.types) {
		for (RDFClassLiteralTriple literalRelation : getLiteralTriples()) {
			int instanceSubject = getInstance(instances, literalRelation.getSubject());
			lines.add("L:" + literalRelation.getSubject().getResource().getLocalName() + "_" + instanceSubject + "-"
					+ literalRelation.getProperty().getLocalName());
		}
		// }

		Collections.sort(lines);

		String res = StringUtils.join(lines, "|");
		res = res.trim();
		res = StringUtils.strip(res, "|");

		return res;
	}

	private int getInstance(Map<Resource, Map<RDFClass, Integer>> instances, RDFClass rdfClass) {
		int instanceSubject = 0;
		if (!instances.containsKey(rdfClass.getResource())) {
			instances.put(rdfClass.getResource(), new HashMap<RDFClass, Integer>());
			instances.get(rdfClass.getResource()).put(rdfClass, 0);
			instanceSubject = 0;
		} else if (!instances.get(rdfClass.getResource()).containsKey(rdfClass)) {
			instances.get(rdfClass.getResource()).put(rdfClass, instances.get(rdfClass.getResource()).size());
			instanceSubject = instances.get(rdfClass.getResource()).get(rdfClass);
		} else {
			instanceSubject = instances.get(rdfClass.getResource()).get(rdfClass);
		}
		return instanceSubject;
	}

	public List<RDFClassLiteralTriple> getLiteralTriples(RDFClass type) {

		List<RDFClassLiteralTriple> triples = new ArrayList<RDFClassLiteralTriple>();

		for (RDFClassLiteralTriple t : this.literalTriples) {
			if (t.getSubject() == type)
				triples.add(t);
		}

		return triples;
	}

}
