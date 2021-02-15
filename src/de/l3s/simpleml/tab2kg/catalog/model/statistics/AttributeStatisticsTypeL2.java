package de.l3s.simpleml.tab2kg.catalog.model.statistics;

public enum AttributeStatisticsTypeL2 {

	INTEGER(AttributeStatisticsTypeL1.NUMERIC), DOUBLE(AttributeStatisticsTypeL1.NUMERIC),
	TIME(AttributeStatisticsTypeL1.TEMPORAL), DATE(AttributeStatisticsTypeL1.TEMPORAL),
	DATETIME(AttributeStatisticsTypeL1.TEMPORAL), BOOLEAN(AttributeStatisticsTypeL1.BOOLEAN),
	STRING(AttributeStatisticsTypeL1.TEXTUAL), LONG(AttributeStatisticsTypeL1.NUMERIC),
	GEO(AttributeStatisticsTypeL1.SPATIAL);

	private AttributeStatisticsTypeL1 typeL1;

	private AttributeStatisticsTypeL2(AttributeStatisticsTypeL1 typeL1) {
		this.typeL1 = typeL1;
	}

	public AttributeStatisticsTypeL1 getTypeL1() {
		return typeL1;
	}

}