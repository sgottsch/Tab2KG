package de.l3s.simpleml.tab2kg.model.rdf;

import org.apache.jena.rdf.model.Resource;

/**
 * An instance of an RDF class in a query graph. That means, if a class appears
 * multiple times, they are represent by different instances of this class.
 */
public class RDFClass extends RDFNode {

	private Resource resource;

	private int instance;

	// private Set<RDFClassLiteralTriple> literalTriples = new
	// HashSet<RDFClassLiteralTriple>();

	public RDFClass(Resource resource) {
		super(null);
		this.resource = resource;
	}

	public RDFClass(Resource resource, String placeHolder, int instance) {
		super(placeHolder);
		this.resource = resource;
		this.instance = instance;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public int getInstance() {
		return instance;
	}

	public void setInstance(int instance) {
		this.instance = instance;
	}

	// public Set<RDFClassLiteralTriple> getLiteralTriples() {
	// return literalTriples;
	// }
	//
	// public void setLiteralTriples(Set<RDFClassLiteralTriple> literalTriples)
	// {
	// this.literalTriples = literalTriples;
	// }
	//
	// public void addLiteralTriple(RDFClassLiteralTriple literalTriple) {
	// this.literalTriples.add(literalTriple);
	// }

}
