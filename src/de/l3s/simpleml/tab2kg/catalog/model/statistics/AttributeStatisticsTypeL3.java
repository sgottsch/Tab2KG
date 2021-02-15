package de.l3s.simpleml.tab2kg.catalog.model.statistics;

public enum AttributeStatisticsTypeL3 {

	GENERIC_TEXT(AttributeStatisticsTypeL1.TEXTUAL), CATEGORICAL_TEXT(AttributeStatisticsTypeL1.TEXTUAL),
	EMAIL(AttributeStatisticsTypeL1.TEXTUAL), URL(AttributeStatisticsTypeL1.TEXTUAL),
	SEQUENTIAL(AttributeStatisticsTypeL1.NUMERIC), CATEGORICAL_NUMBER(AttributeStatisticsTypeL1.NUMERIC),
	ANY_NUMBER(AttributeStatisticsTypeL1.NUMERIC), POINT(AttributeStatisticsTypeL1.SPATIAL),
	LINESTRING(AttributeStatisticsTypeL1.SPATIAL), POLYGON(AttributeStatisticsTypeL1.SPATIAL),
	MIXED_GEOMETRIES(AttributeStatisticsTypeL1.SPATIAL);

	private AttributeStatisticsTypeL1 typeL1;

	private AttributeStatisticsTypeL3(AttributeStatisticsTypeL1 typeL1) {
		this.typeL1 = typeL1;
	}

	public AttributeStatisticsTypeL1 getTypeL1() {
		return typeL1;
	}

}