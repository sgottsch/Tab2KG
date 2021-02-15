package de.l3s.simpleml.tab2kg.graphcandidates.model;

public class DummyGraphEdge {

	private DummyGraphNode node1;

	private DummyGraphNode node2;

	private String id;

	public DummyGraphEdge(DummyGraphNode node1, DummyGraphNode node2) {
		super();
		this.node1 = node1;
		this.node2 = node2;

		node1.getNeighbours().add(node2);
		node2.getNeighbours().add(node1);
	}

	public boolean contains(DummyGraphNode node) {
		return this.node1 == node || this.node2 == node;
	}

	public DummyGraphNode getNode1() {
		return node1;
	}

	public DummyGraphNode getNode2() {
		return node2;
	}

	public boolean hasNeighbour(DummyGraphEdge edge2) {
		return this.node1 == edge2.node1 || this.node1 == edge2.node2 || this.node2 == edge2.node1
				|| this.node2 == edge2.node2;
	}

	public String getId() {
		if (this.id == null) {
			if (this.node1.getNumber() < this.node2.getNumber())
				this.id = node1.getNumber() + "," + node2.getNumber();
			else
				this.id = node2.getNumber() + "," + node1.getNumber();
		}
		return this.id;
	}

}
