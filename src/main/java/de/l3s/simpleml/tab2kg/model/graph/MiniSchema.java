package de.l3s.simpleml.tab2kg.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;

public class MiniSchema {

	private List<RDFNodeLiteralTriple> literalTriples = new ArrayList<RDFNodeLiteralTriple>();
	private List<RDFNodeTriple> classTriples = new ArrayList<RDFNodeTriple>();

	private Map<Resource, List<RDFNodeLiteralTriple>> literalTriplesBySubject = new HashMap<Resource, List<RDFNodeLiteralTriple>>();

	private Map<String, Set<String>> neighouredClasses = new HashMap<String, Set<String>>();

	public MiniSchema() {
		super();
	}

	public List<RDFNodeLiteralTriple> getLiteralTriples() {
		return literalTriples;
	}

	public List<RDFNodeLiteralTriple> getLiteralTriplesWithSubject(Resource subject) {
		return literalTriples;
	}

	public void addLiteralTriple(RDFNodeLiteralTriple literalTriple) {
		this.literalTriples.add(literalTriple);
		if (!this.literalTriplesBySubject.containsKey(literalTriple.getSubject()))
			this.literalTriplesBySubject.put(literalTriple.getSubject(), new ArrayList<RDFNodeLiteralTriple>());
		this.literalTriplesBySubject.get(literalTriple.getSubject()).add(literalTriple);
	}

	public List<RDFNodeLiteralTriple> getLiteralTriplesBySubject(Resource subject) {
		if (!this.literalTriplesBySubject.containsKey(subject))
			return new ArrayList<RDFNodeLiteralTriple>();
		return literalTriplesBySubject.get(subject);
	}

	public List<RDFNodeTriple> getClassTriples() {
		return classTriples;
	}

	public void setClassTriples(List<RDFNodeTriple> classTriples) {
		this.classTriples = classTriples;
	}

	public void addClassTriple(RDFNodeTriple classTriple) {
		this.classTriples.add(classTriple);

		addNeighouredClass(classTriple.getSubject(), classTriple.getObject());
	}

	public Map<String, Set<String>> getNeighouredClasses() {
		return neighouredClasses;
	}

	public void addNeighouredClass(Resource class1, Resource class2) {
		if (!this.neighouredClasses.containsKey(class1.getURI()))
			this.neighouredClasses.put(class1.getURI(), new HashSet<String>());
		this.neighouredClasses.get(class1.getURI()).add(class2.getURI());

		if (!this.neighouredClasses.containsKey(class2.getURI()))
			this.neighouredClasses.put(class2.getURI(), new HashSet<String>());
		this.neighouredClasses.get(class2.getURI()).add(class1.getURI());
	}

}
