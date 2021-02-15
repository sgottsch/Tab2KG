package de.l3s.simpleml.tab2kg.model.rdf;

public abstract class RDFNode {

	private String placeHolder;

	public RDFNode(String placeHolder) {
		super();
		this.placeHolder = placeHolder;
	}

	public String getPlaceHolder() {
		return placeHolder;
	}

	public void setPlaceHolder(String placeHolder) {
		this.placeHolder = placeHolder;
	}

}
