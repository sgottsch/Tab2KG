package de.l3s.simpleml.tab2kg.graphcandidates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraph;
import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraphEdge;
import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraphNode;

public class SubGraphsFinder2 {

	Map<String, DummyGraph> allGraphs = new HashMap<String, DummyGraph>();
	Map<String, DummyGraph> doneGraphs = new HashMap<String, DummyGraph>();
	Set<DummyGraph> resultGraphs = new HashSet<DummyGraph>();
	Map<String, DummyGraph> undoneGraphs = new HashMap<String, DummyGraph>();

	private List<DummyGraphNode> targetNodesList;
	private HashMap<DummyGraphNode, DummyGraphNode> oldToNewNodes;

	public static final int MAX_GRAPH_SIZE = 4;

	public static void main(String[] args) {

		// DummyGraphNode nodeA = new DummyGraphNode("A");
		// DummyGraphNode nodeB = new DummyGraphNode("B");
		// DummyGraphNode nodeC = new DummyGraphNode("C");
		// DummyGraphNode nodeD = new DummyGraphNode("D");
		//
		// nodeA.addNeighbour(nodeB);
		// nodeA.addNeighbour(nodeC);
		// nodeC.addNeighbour(nodeD);

		// DummyGraphNode nodeA = new DummyGraphNode("A");
		// DummyGraphNode nodeB = new DummyGraphNode("B");
		// DummyGraphNode nodeC = new DummyGraphNode("C");
		// DummyGraphNode nodeD = new DummyGraphNode("D");
		// DummyGraphNode nodeE = new DummyGraphNode("E");
		// DummyGraphNode nodeF = new DummyGraphNode("F");
		// DummyGraphNode nodeG = new DummyGraphNode("G");
		// DummyGraphNode nodeH = new DummyGraphNode("H");
		//
		// Set<DummyGraphEdge> edges = new HashSet<DummyGraphEdge>();
		// dummyGraph.addEdge(new DummyGraphEdge(nodeA, nodeC));
		// dummyGraph.addEdge(new DummyGraphEdge(nodeA, nodeF));
		// dummyGraph.addEdge(new DummyGraphEdge(nodeC, nodeD));
		// dummyGraph.addEdge(new DummyGraphEdge(nodeF, nodeD));
		// dummyGraph.addEdge(new DummyGraphEdge(nodeF, nodeG));
		// dummyGraph.addEdge(new DummyGraphEdge(nodeF, nodeH));
		// dummyGraph.addEdge(new DummyGraphEdge(nodeD, nodeE));
		// dummyGraph.addEdge(new DummyGraphEdge(nodeE, nodeB));
		// dummyGraph.addEdge(new DummyGraphEdge(nodeH, nodeB));

		DummyGraph dummyGraph = new DummyGraph();

		DummyGraphNode nodeA = new DummyGraphNode("A");
		DummyGraphNode nodeB = new DummyGraphNode("B");
		DummyGraphNode nodeC = new DummyGraphNode("C");
		DummyGraphNode nodeD = new DummyGraphNode("D");
		DummyGraphNode nodeE = new DummyGraphNode("E");
		DummyGraphNode nodeF = new DummyGraphNode("F");
		DummyGraphNode nodeG = new DummyGraphNode("G");
		DummyGraphNode nodeH = new DummyGraphNode("H");
		DummyGraphNode nodeI = new DummyGraphNode("I");

		dummyGraph.addNode(nodeA);
		dummyGraph.addNode(nodeB);
		dummyGraph.addNode(nodeC);
		dummyGraph.addNode(nodeD);
		dummyGraph.addNode(nodeE);
		dummyGraph.addNode(nodeF);
		dummyGraph.addNode(nodeG);
		dummyGraph.addNode(nodeH);
		dummyGraph.addNode(nodeI);

		dummyGraph.addEdge(new DummyGraphEdge(nodeA, nodeB));
		dummyGraph.addEdge(new DummyGraphEdge(nodeA, nodeC));
		dummyGraph.addEdge(new DummyGraphEdge(nodeA, nodeD));
		dummyGraph.addEdge(new DummyGraphEdge(nodeA, nodeE));
		dummyGraph.addEdge(new DummyGraphEdge(nodeA, nodeH));
		dummyGraph.addEdge(new DummyGraphEdge(nodeA, nodeI));
		dummyGraph.addEdge(new DummyGraphEdge(nodeB, nodeC));
		dummyGraph.addEdge(new DummyGraphEdge(nodeC, nodeD));
		dummyGraph.addEdge(new DummyGraphEdge(nodeC, nodeE));
		dummyGraph.addEdge(new DummyGraphEdge(nodeD, nodeG));
		dummyGraph.addEdge(new DummyGraphEdge(nodeD, nodeH));
		dummyGraph.addEdge(new DummyGraphEdge(nodeE, nodeF));
		dummyGraph.addEdge(new DummyGraphEdge(nodeE, nodeG));
		dummyGraph.addEdge(new DummyGraphEdge(nodeE, nodeH));
		dummyGraph.addEdge(new DummyGraphEdge(nodeF, nodeI));
		dummyGraph.addEdge(new DummyGraphEdge(nodeG, nodeH));

		Set<DummyGraphNode> targetNodes = new HashSet<DummyGraphNode>();
		targetNodes.add(nodeA);
		targetNodes.add(nodeB);
		targetNodes.add(nodeD);
		targetNodes.add(nodeH);

		SubGraphsFinder2 sf = new SubGraphsFinder2();
		sf.findGraphs(dummyGraph, targetNodes);

	}

	public Set<DummyGraph> findGraphs(DummyGraph dummyGraphOri, Set<DummyGraphNode> targetNodesOri) {

		reduceDummyGraph(dummyGraphOri, targetNodesOri, false);

		this.targetNodesList = new ArrayList<DummyGraphNode>();
		List<DummyGraphNode> targetNodesList2 = new ArrayList<DummyGraphNode>();
		for (DummyGraphNode nodeOri : targetNodesOri) {
			this.targetNodesList.add(this.oldToNewNodes.get(nodeOri));
			targetNodesList2.add(this.oldToNewNodes.get(nodeOri));
		}

		DummyGraphNode startNode = targetNodesList.remove(0);
		DummyGraph startGraph = new DummyGraph();
		startGraph.addNode(startNode);

		extend(startGraph);

		for (Iterator<DummyGraph> it = this.resultGraphs.iterator(); it.hasNext();) {
			DummyGraph resultGraph = it.next();
			if (!isMinimal(resultGraph, targetNodesList2))
				it.remove();
		}

		return this.resultGraphs;
	}

	private boolean isMinimal(DummyGraph graph, List<DummyGraphNode> targetNodes) {

		// find leaf nodes
		// all nodes that only appear in one edge are leaf nodes
		Set<DummyGraphNode> leafNodes = new HashSet<DummyGraphNode>();
		leafNodes.addAll(graph.getNodes());

		Set<DummyGraphNode> nodesWithAnEdge = new HashSet<DummyGraphNode>();
		for (DummyGraphEdge edge : graph.getEdges()) {
			if (nodesWithAnEdge.contains(edge.getNode1()))
				leafNodes.remove(edge.getNode1());

			if (nodesWithAnEdge.contains(edge.getNode2()))
				leafNodes.remove(edge.getNode2());

			nodesWithAnEdge.add(edge.getNode1());
			nodesWithAnEdge.add(edge.getNode2());
		}

		for (DummyGraphNode leafNode : leafNodes) {
			if (!targetNodes.contains(leafNode))
				return false;
		}

		return true;
	}

	private void extend(DummyGraph startGraph) {

		if (this.doneGraphs.containsKey(startGraph.getId()))
			return;
		this.doneGraphs.put(startGraph.getId(), startGraph);

		boolean containsAll = true;
		for (DummyGraphNode node : this.targetNodesList) {
			if (!startGraph.getNodes().contains(node)) {
				containsAll = false;
			}
		}
		if (containsAll) {
			this.resultGraphs.add(startGraph);
			return;
		}

		for (DummyGraphNode node : startGraph.getNodes()) {

			List<DummyGraphEdge> neighbourEdges = new ArrayList<DummyGraphEdge>();
			neighbourEdges.addAll(node.getNeighbourEdges());

			for (DummyGraphEdge neighbourEdge : neighbourEdges) {

				DummyGraphNode neighbour = neighbourEdge.getNode1();
				if (neighbour == node)
					neighbour = neighbourEdge.getNode2();

				if (startGraph.getNodes().contains(neighbour))
					continue;

				DummyGraph graph = new DummyGraph();
				for (DummyGraphNode newNode : startGraph.getNodes()) {
					graph.addNode(newNode);
				}
				for (DummyGraphEdge newEdge : startGraph.getEdges()) {
					graph.addEdge(newEdge);
				}

				graph.addEdge(neighbourEdge);
				graph.addNode(neighbour);

				extend(graph);
			}

		}

		// this.doneGraphs.put(startGraph.getId(), startGraph);
	}

	public DummyGraph reduceDummyGraph(DummyGraph dummyGraph, Set<DummyGraphNode> targetNodes,
			boolean onlyEdgesThatContainBothTargets) {

		this.oldToNewNodes = new HashMap<DummyGraphNode, DummyGraphNode>();

		DummyGraph reducedDummyGraph = new DummyGraph();
		for (DummyGraphEdge edge : dummyGraph.getEdges()) {
			if (onlyEdgesThatContainBothTargets && targetNodes.contains(edge.getNode1())
					&& targetNodes.contains(edge.getNode2())
					|| !onlyEdgesThatContainBothTargets
							&& (targetNodes.contains(edge.getNode1()) || targetNodes.contains(edge.getNode2()))) {

				DummyGraphNode newNode1 = oldToNewNodes.get(edge.getNode1());
				if (newNode1 == null) {
					newNode1 = new DummyGraphNode(edge.getNode1().getId());
					newNode1.setResource(edge.getNode1().getResource());
					oldToNewNodes.put(edge.getNode1(), newNode1);
					reducedDummyGraph.addNode(newNode1);
				}

				DummyGraphNode newNode2 = oldToNewNodes.get(edge.getNode2());
				if (newNode2 == null) {
					newNode2 = new DummyGraphNode(edge.getNode2().getId());
					newNode2.setResource(edge.getNode2().getResource());
					oldToNewNodes.put(edge.getNode2(), newNode2);
					reducedDummyGraph.addNode(newNode2);
				}

				DummyGraphEdge newEdge = new DummyGraphEdge(newNode1, newNode2);
				reducedDummyGraph.addEdge(newEdge);
				reducedDummyGraph.addNode(newNode1);
				reducedDummyGraph.addNode(newNode2);
			}
		}

		List<DummyGraphNode> nodes = new ArrayList<DummyGraphNode>();
		nodes.addAll(dummyGraph.getNodes());

		for (DummyGraphNode node : nodes) {
			if (targetNodes.contains(node)) {
				DummyGraphNode newNode = oldToNewNodes.get(node);
				if (newNode == null) {
					newNode = new DummyGraphNode(node.getId());
					newNode.setResource(node.getResource());
					oldToNewNodes.put(node, newNode);
					reducedDummyGraph.addNode(newNode);
				}
			}
		}

		return reducedDummyGraph;
	}

	public static void cleanNodes(DummyGraph dummyGraph) {

	}

}
