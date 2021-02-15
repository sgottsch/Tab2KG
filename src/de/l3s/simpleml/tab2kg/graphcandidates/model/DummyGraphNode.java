package de.l3s.simpleml.tab2kg.graphcandidates.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.graphcandidates.ColumnCandidate;

public class DummyGraphNode {

	private Resource resource;

	private String id;
	private int number;

	private List<DummyGraphNode> neighbours = new ArrayList<DummyGraphNode>();
	private List<DummyGraphEdge> neighbourEdges = new ArrayList<DummyGraphEdge>();

	private Set<ColumnCandidate> columnCandidate = new HashSet<ColumnCandidate>();

	public DummyGraphNode(Resource resource) {
		super();
		this.resource = resource;
	}

	public DummyGraphNode(Set<ColumnCandidate> columnCandidate) {
		super();
		this.columnCandidate = columnCandidate;
	}

	public DummyGraphNode(String id) {
		super();
		this.id = id;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public List<DummyGraphNode> getNeighbours() {
		return neighbours;
	}

	public void addNeighbour(DummyGraphNode neighbour) {
		this.neighbours.add(neighbour);
		if (!neighbour.getNeighbours().contains(this))
			neighbour.addNeighbour(this);
	}

	public Set<ColumnCandidate> getColumnCandidate() {
		return columnCandidate;
	}

	public String getId() {
		return id;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public List<DummyGraphEdge> getNeighbourEdges() {
		return neighbourEdges;
	}

	public void addNeighbourEdge(DummyGraphEdge edge) {
		if(!this.neighbourEdges.contains(edge))
		this.neighbourEdges.add(edge);
	}

}
