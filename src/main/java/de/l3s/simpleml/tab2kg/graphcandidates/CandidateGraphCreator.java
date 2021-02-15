package de.l3s.simpleml.tab2kg.graphcandidates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraph;
import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraphEdge;
import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraphNode;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;

public class CandidateGraphCreator {

	public static DummyGraph createDummyGraph(SimpleGraph simpleGraph) {
		// System.out.println("createDummyGraph");
		DummyGraph dummyGraph = new DummyGraph();

		Map<String, DummyGraphNode> nodesByResourceURI = new HashMap<String, DummyGraphNode>();
		for (RDFNodeTriple classTriple : simpleGraph.getMiniSchema().getClassTriples()) {

			String subjectURI = classTriple.getSubject().getURI();
			DummyGraphNode subjectNode = nodesByResourceURI.get(subjectURI);
			if (subjectNode == null) {
				subjectNode = new DummyGraphNode(classTriple.getSubject());
				nodesByResourceURI.put(subjectURI, subjectNode);
				dummyGraph.addNode(subjectNode);
			}

			String objectURI = classTriple.getObject().getURI();
			DummyGraphNode objectNode = nodesByResourceURI.get(objectURI);
			if (objectNode == null) {
				objectNode = new DummyGraphNode(classTriple.getObject());
				nodesByResourceURI.put(objectURI, objectNode);
				dummyGraph.addNode(objectNode);
			}

			if (!dummyGraph.containsEdge(subjectNode, objectNode) && subjectNode != objectNode) {
				dummyGraph.addEdge(new DummyGraphEdge(subjectNode, objectNode));
			}
		}

		for (RDFNodeLiteralTriple literalTriple : simpleGraph.getMiniSchema().getLiteralTriples()) {
			String subjectURI = literalTriple.getSubject().getURI();
			DummyGraphNode subjectNode = nodesByResourceURI.get(subjectURI);
			if (subjectNode == null) {
				subjectNode = new DummyGraphNode(literalTriple.getSubject());
				nodesByResourceURI.put(subjectURI, subjectNode);
				dummyGraph.addNode(subjectNode);
			}
		}

		return dummyGraph;
	}

	/**
	 * 
	 * Given multiple sets, creates a new set which contain one element of each
	 * input set each.
	 * 
	 * For example, the three groups "a,b,c", "x" and "y,z" result in the following
	 * six combinations: axy, axz, bxy, bxz, cxy, cxz.
	 * 
	 */
	public static <T> List<Set<T>> combineGroups(Set<Set<T>> objects) {
		List<Set<T>> newGroups = new ArrayList<Set<T>>();

		int size = 1;
		for (Set<T> candidates : objects) {
			size *= candidates.size();
		}

		for (int i = 0; i < size; i++) {
			newGroups.add(new HashSet<T>());
		}

		Map<Set<T>, Integer> repitions = new HashMap<Set<T>, Integer>();
		int rep = 1;

		for (Set<T> candidates : objects) {
			repitions.put(candidates, rep);
			rep *= candidates.size();
		}

		for (Set<T> candidates : objects) {
			int step = repitions.get(candidates);
			int i = 0;
			while (i < size) {
				for (T candidate : candidates) {
					for (int j = 0; j < step; j++) {
						newGroups.get(i + j).add(candidate);
					}
					i += step;
				}
			}

		}

		return newGroups;
	}

}
