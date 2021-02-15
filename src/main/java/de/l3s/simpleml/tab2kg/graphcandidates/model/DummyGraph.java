package de.l3s.simpleml.tab2kg.graphcandidates.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class DummyGraph {

	private Set<DummyGraphEdge> edges = new HashSet<DummyGraphEdge>();
	private List<DummyGraphNode> nodes = new ArrayList<DummyGraphNode>();

	private int number = 0;

	private String id = null;
	private Set<String> edgeIds = new HashSet<String>();

	public Set<DummyGraphEdge> getEdges() {
		return edges;
	}

	public void setEdges(Set<DummyGraphEdge> edges) {
		this.edges = edges;
	}

	public List<DummyGraphNode> getNodes() {
		return nodes;
	}

	public void addEdge(DummyGraphEdge edge) {

		if (this.edgeIds.contains(edge.getId()))
			return;

		this.edges.add(edge);
		edge.getNode1().addNeighbour(edge.getNode2());
		edge.getNode2().addNeighbour(edge.getNode1());
		edge.getNode1().addNeighbourEdge(edge);
		edge.getNode2().addNeighbourEdge(edge);
		this.edgeIds.add(edge.getId());
	}

	public void setNodes(List<DummyGraphNode> nodes) {
		this.nodes = nodes;
	}

	public void addNode(DummyGraphNode node) {
		if (this.nodes.contains(node))
			return;
		this.nodes.add(node);
		node.setNumber(number);
		number += 1;
	}

	public boolean containsEdge(DummyGraphNode node1, DummyGraphNode node2) {

		for (DummyGraphEdge edge : this.edges) {
			if (edge.contains(node1) && edge.contains(node2))
				return true;
		}

		return false;
	}

	public void print() {
		for (DummyGraphNode n : this.nodes)
			System.out.println(n.getResource().getURI());
		for (DummyGraphEdge n : this.edges)
			System.out.println(n.getNode1().getResource().getURI() + "--" + n.getNode2().getResource().getURI());

//		for (DummyGraphNode n : this.nodes)
//			System.out.println(n.getId());
//		for (DummyGraphEdge n : this.edges)
//			System.out.println(n.getNode1().getId() + "->" + n.getNode2().getId());

	}

	public String getId() {

		if (this.id == null) {
			this.id = "";
			List<Integer> nodeIds = new ArrayList<Integer>();
			for (DummyGraphNode node : this.nodes)
				nodeIds.add(node.getNumber());
			Collections.sort(nodeIds);
			this.id = StringUtils.join(nodeIds, "-") + "_";

			List<String> edgeIds = new ArrayList<String>();
			for (DummyGraphEdge edge : this.edges) {
				edgeIds.add(edge.getId());
			}
			this.id += StringUtils.join(edgeIds, "-");
		}

		return this.id;
	}

}
