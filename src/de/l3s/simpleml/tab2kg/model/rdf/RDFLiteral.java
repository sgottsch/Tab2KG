package de.l3s.simpleml.tab2kg.model.rdf;

import org.apache.jena.datatypes.RDFDatatype;

public class RDFLiteral extends RDFNode {

	private RDFDatatype dataType;

	public RDFLiteral(RDFDatatype dataType) {
		super(null);
		this.dataType = dataType;
	}

	public RDFLiteral(String placeHolder) {
		super(placeHolder);
	}

	public RDFDatatype getDataType() {
		return dataType;
	}

	public void setDataType(RDFDatatype dataType) {
		this.dataType = dataType;
	}

}
