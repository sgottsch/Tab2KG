package de.l3s.simpleml.tab2kg.model.rdf;

import org.apache.jena.rdf.model.Resource;

public class RDFResource extends RDFNode {

	private Resource resource;

	public RDFResource(String placeHolder) {
		super(placeHolder);
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

}
