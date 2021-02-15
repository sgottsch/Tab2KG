package de.l3s.simpleml.tab2kg.graphcandidates.model;

import java.util.ArrayList;
import java.util.List;

public class DummyGraphPath {

	private List<DummyGraphNode> nodes = new ArrayList<DummyGraphNode>();

	public DummyGraphPath() {
	}

	public DummyGraphPath(DummyGraphNode node) {
		this.nodes.add(node);
	}

	public DummyGraphNode getStartNode() {
		return this.nodes.get(0);
	}

	public DummyGraphNode getLastNode() {
		return this.nodes.get(this.nodes.size() - 1);
	}

	public List<DummyGraphNode> getNodes() {
		return nodes;
	}

	public void addNode(DummyGraphNode node) {
		this.nodes.add(node);
	}

	public boolean containsNode(DummyGraphNode node) {
		return this.nodes.contains(node);
	}

	public DummyGraphPath copy() {
		DummyGraphPath copy = new DummyGraphPath();

		for (DummyGraphNode node : this.getNodes())
			copy.addNode(node);

		return copy;
	}

	public String printPath() {
		String ids = "";
		for (DummyGraphNode node : this.nodes)
			ids += node.getId() + " ";
		return ids.trim();
	}

}
