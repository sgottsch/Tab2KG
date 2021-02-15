package de.l3s.simpleml.tab2kg.model.template;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.l3s.simpleml.tab2kg.model.rdf.QueryGraph;
import de.l3s.simpleml.tab2kg.model.rdf.QueryTemplate;

public class TemplateNode {

	private int id;

	private TemplateNode parent;

	private List<TemplateNode> children = new ArrayList<TemplateNode>();

	private List<TemplateNode> allChildren = new ArrayList<TemplateNode>();

	public Set<TemplateNode> dependentTrees = new HashSet<TemplateNode>();
	public TemplateNode dependsOnTree;

	private QueryGraph queryGraph;
	private QueryTemplate queryTemplate;

	public TemplateNode(int id) {
		super();
		this.id = id;
		this.allChildren.add(this);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public List<TemplateNode> getChildren() {
		return children;
	}

	public void addChild(TemplateNode node) {
		this.children.add(node);
		node.setParent(this);

		addToAllChildren(node);
	}

	private void addToAllChildren(TemplateNode node) {
		this.allChildren.add(node);
		if (getParent() != null) {
			getParent().addToAllChildren(node);
		}
	}

	public void setChildren(List<TemplateNode> children) {
		this.children = children;
	}

	public List<TemplateNode> getAllNodes() {
		return allChildren;
	}

	public void print() {
		print("");
	}

	public void print(String indent) {

		System.out.println(indent + this.id);
		for (TemplateNode child : this.children)
			child.print(indent + " ");
	}

	public TemplateNode getParent() {
		return parent;
	}

	public void setParent(TemplateNode parent) {
		this.parent = parent;
	}

	public List<TemplateNode> getAllChildren() {
		return allChildren;
	}

	@Override
	public String toString() {
		return String.valueOf(this.id);
	}

	public void setDependsOnTree(TemplateNode dependsOnTree) {
		this.dependsOnTree = dependsOnTree;
	}

	public void addDependentTree(TemplateNode tree) {
		this.dependentTrees.add(tree);
		tree.setDependsOnTree(this);

		if (this.dependsOnTree != null)
			this.dependsOnTree.addDependentTree(tree);
	}

	public Set<TemplateNode> getDependentTrees() {
		return dependentTrees;
	}

	public QueryGraph getQueryGraph() {
		return queryGraph;
	}

	public void setQueryGraph(QueryGraph queryGraph) {
		this.queryGraph = queryGraph;
	}

	public QueryTemplate getQueryTemplate() {
		return queryTemplate;
	}

	public void setQueryTemplate(QueryTemplate queryTemplate) {
		this.queryTemplate = queryTemplate;
	}

}
