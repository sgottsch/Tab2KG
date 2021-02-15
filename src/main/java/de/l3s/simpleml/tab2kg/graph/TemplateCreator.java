package de.l3s.simpleml.tab2kg.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.l3s.simpleml.tab2kg.model.rdf.QueryTemplate;
import de.l3s.simpleml.tab2kg.model.template.TemplateNode;
import de.l3s.simpleml.tab2kg.model.template.TemplateTriple;

public class TemplateCreator {

	Set<TemplateNode> tress = new HashSet<TemplateNode>();

	private Map<Integer, List<TemplateNode>> treesPerSize = new HashMap<Integer, List<TemplateNode>>();

	public static void main(String[] args) {
		TemplateCreator tc = new TemplateCreator();
		Map<Integer, Set<QueryTemplate>> templates = tc.createQueryTemplates(3);
		for (int size : templates.keySet()) {
			for (QueryTemplate template : templates.get(size)) {
				template.getTree().print();
				System.out.println("");
			}
		}
	}

	public Map<Integer, Set<QueryTemplate>> createQueryTemplates(int maxNumberOfNodes) {

		Map<Integer, Set<QueryTemplate>> queryTemplates = new HashMap<Integer, Set<QueryTemplate>>();

		TemplateCreator tc = new TemplateCreator();

		TemplateNode oneTree = new TemplateNode(0);

		tc.treesPerSize.put(1, new ArrayList<TemplateNode>());
		tc.treesPerSize.get(1).add(oneTree);

		for (int size = 2; size <= maxNumberOfNodes; size++) {
			queryTemplates.put(size, new HashSet<QueryTemplate>());

			tc.treesPerSize.put(size, new ArrayList<TemplateNode>());

			Set<TemplateNode> roots = new HashSet<TemplateNode>();
			roots.addAll(tc.treesPerSize.get(size - 1));
			for (TemplateNode root : roots) {
				tc.addNode(root);
			}

			// clean trees
			Map<String, TemplateNode> uniqueTrees = new HashMap<String, TemplateNode>();
			for (TemplateNode root : tc.treesPerSize.get(size)) {

				Set<TemplateNode> allChildren = new HashSet<TemplateNode>();
				allChildren.addAll(root.getAllChildren());
				for (TemplateNode child : allChildren)
					updateAllChildren(child);

				Map<Integer, Set<TemplateNode>> nodesPerLevel = new HashMap<Integer, Set<TemplateNode>>();
				createNodesPerLevel(nodesPerLevel, root, 0);

				String label = createTreeLabel(nodesPerLevel);

				uniqueTrees.put(label, root);
			}

			tc.treesPerSize.get(size).clear();
			tc.treesPerSize.get(size).addAll(uniqueTrees.values());
		}

		int templateId = 1;
		for (int i : tc.treesPerSize.keySet()) {
			queryTemplates.put(i, new HashSet<QueryTemplate>());

			for (TemplateNode root : tc.treesPerSize.get(i)) {
				queryTemplates.get(i).add(tc.transformTreeIntoQueryTemplate(root, templateId));
				templateId += 1;
			}
		}

		for (Set<QueryTemplate> queryTemplateTmp : queryTemplates.values()) {
			for (QueryTemplate queryTemplate : queryTemplateTmp) {
				for (TemplateNode node : queryTemplate.getTree().getDependentTrees()) {
					queryTemplate.getDependentQueryTemplates().add(node.getQueryTemplate());
				}
			}
		}

		return queryTemplates;
	}

	private void createNodesPerLevel(Map<Integer, Set<TemplateNode>> nodesPerLevel, TemplateNode root, int level) {

		if (!nodesPerLevel.containsKey(level))
			nodesPerLevel.put(level, new HashSet<TemplateNode>());

		nodesPerLevel.get(level).add(root);

		for (TemplateNode child : root.getChildren()) {
			createNodesPerLevel(nodesPerLevel, child, level + 1);
		}
	}

	private String createTreeLabel(Map<Integer, Set<TemplateNode>> nodesPerLevel) {

		String label = "";

		for (int level : nodesPerLevel.keySet()) {
			List<Integer> sizes = new ArrayList<Integer>();
			for (TemplateNode node : nodesPerLevel.get(level))
				sizes.add(node.getAllChildren().size());
			Collections.sort(sizes);
			label += StringUtils.join(sizes, "-") + "_";
		}

		return label.substring(0, label.length() - 1);
	}

	public void addNode(TemplateNode root) {

		for (int i = 0; i < root.getAllNodes().size(); i++) {
			TemplateNode tree = copyTree(root);
			updateAllChildren(tree);

			tree.getAllNodes().get(i).addChild(new TemplateNode(tree.getAllNodes().size()));
			this.treesPerSize.get(tree.getAllNodes().size()).add(tree);

			root.addDependentTree(tree);
		}

	}

	private static TemplateNode copyTree(TemplateNode root) {

		TemplateNode node = new TemplateNode(root.getId());

		for (TemplateNode child : root.getChildren()) {
			TemplateNode subTree = copyTree(child);
			node.addChild(subTree);
		}

		return node;
	}

	private static void updateAllChildren(TemplateNode root) {
		root.getAllChildren().clear();
		root.getAllChildren().add(root);
		for (TemplateNode child : root.getChildren()) {
			updateAllChildren(root, child);
		}
	}

	private static void updateAllChildren(TemplateNode root, TemplateNode child) {
		root.getAllChildren().add(child);
		for (TemplateNode child2 : child.getChildren()) {
			updateAllChildren(root, child2);
		}
	}

	private QueryTemplate transformTreeIntoQueryTemplate(TemplateNode root, int templateId) {

		QueryTemplate queryTemplate = new QueryTemplate(templateId);
		root.setQueryTemplate(queryTemplate);
		queryTemplate.setTree(root);
		List<String> connections = new ArrayList<String>();

		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";

		queryString += "SELECT DISTINCT ";

		String groupByString = "";

		for (TemplateNode node : root.getAllChildren()) {
			String nodeId = "?x" + node.getId();
			queryTemplate.addNodePlaceHolder(nodeId);

			String typeId = "?t" + node.getId();
			queryTemplate.addTypePlaceHolder(typeId);

			queryString += typeId + " ";
			groupByString += typeId + " ";
		}
		for (int i = 0; i < root.getAllChildren().size() - 1; i++) {
			String propertyId = "?p" + i;
			queryTemplate.addPropertyPlaceHolder(propertyId);

			queryString += propertyId + " ";
			groupByString += propertyId + " ";
		}

		queryTemplate.setCountVariableName("?count");
		queryString += "(COUNT(*) AS " + queryTemplate.getCountVariableName() + ") ";

		queryString += "WHERE {\n";

		addConnections(root, connections, queryTemplate);

		for (String connection : connections) {
			queryString += connection + "\n";
		}

		for (TemplateNode node : root.getAllChildren()) {
			queryString += "?x" + node.getId() + " rdf:type ?t" + node.getId() + " .\n";
		}

		// assumption: in one graph, there is always just one instance per type!
		for (TemplateNode node1 : root.getAllChildren()) {
			for (TemplateNode node2 : root.getAllChildren()) {
				if (node1 != node2)
					queryString += "FILTER(?t" + node1.getId() + " != ?t" + node2.getId() + ") .\n";
			}
		}

		for (TemplateNode node : root.getAllChildren()) {
			queryString += "FILTER(!STRSTARTS(STR(?t" + node.getId() + "),\"http://www.w3.org/2002/07/owl#\")) .\n";
			queryString += "FILTER(!STRSTARTS(STR(?t" + node.getId() + "),\"http://rdfs.org/ns/void#\")) .\n";
		}

		List<String> filterConditions = new ArrayList<String>();
		for (TemplateNode node1 : root.getAllChildren()) {
			for (TemplateNode node2 : root.getAllChildren()) {
				if (node1 != node2)
					filterConditions.add("?x" + node1.getId() + " != ?x" + node2.getId());
				// queryString += "FILTER(?x" + node1.getId() + " != ?x" +
				// node2.getId() + ") .\n";
			}
		}

		if (!filterConditions.isEmpty()) {
			queryString += "FILTER(" + StringUtils.join(filterConditions, " && ") + ") .\n";
		}

		queryString += "} GROUP BY " + groupByString.trim();

		queryTemplate.setQuery(queryString);

		return queryTemplate;
	}

	private void addConnections(TemplateNode root, List<String> connections, QueryTemplate queryTemplate) {

		for (TemplateNode child : root.getChildren()) {
			String subjectVariableName = "?x" + root.getId();
			String propertyVariableName = "?p" + connections.size();
			String objectVariableName = "?x" + child.getId();
			String subjectTypeVariableName = "?t" + root.getId();
			String objectTypeVariableName = "?t" + child.getId();

			connections.add(subjectVariableName + " " + propertyVariableName + " " + objectVariableName
					+ " .\nFILTER(?p" + connections.size() + " != rdf:type) .");
			addConnections(child, connections, queryTemplate);
			queryTemplate.addTemplateTriple(
					new TemplateTriple(subjectTypeVariableName, propertyVariableName, objectTypeVariableName));
		}

	}

}
