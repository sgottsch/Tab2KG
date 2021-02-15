package de.l3s.simpleml.tab2kg.model.sparql;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Property;

public enum PropertyInstance {

	DCTERMS_TITLE("title", Prefix.DCTERMS), DCTERMS_SUBJECT("subject", Prefix.DCTERMS),
	DCTERMS_DESCRIPTION("description", Prefix.DCTERMS), SML_SUBJECT("subject", Prefix.DCTERMS),
	RDF_TYPE("type", Prefix.RDF), RDFS_LABEL("label", Prefix.RDFS), SML_CLASS_INSTANCE("classInstance", Prefix.ANON),
	SML_MAPS_TO("mapsTo", Prefix.ANON), SML_LAT_BEFORE_LON("latBeforeLon", Prefix.ANON),
	SML_CREATOR_ID("creatorId", Prefix.ANON), SML_HAS_FILE("hasFile", Prefix.ANON),
	SML_FILE_LOCATION("fileLocation", Prefix.ANON), DCTERMS_FORMAT("format", Prefix.DCTERMS),
	DCTERMS_IDENTIFIER("identifier", Prefix.DCTERMS), CSVW_SEPARATOR("separator", Prefix.CSVW),
	CSVW_HEADER("header", Prefix.CSVW), CSVW_NULL("null", Prefix.CSVW), SML_HAS_ATTRIBUTE("hasAttribute", Prefix.ANON),
	SML_COLUMN_INDEX("columnIndex", Prefix.ANON), SML_VALUE_TYPE("valueType", Prefix.ANON),
	SML_MAPS_TO_DOMAIN("mapsToDomain", Prefix.ANON), SML_MAPS_TO_PROPERTY("mapsToProperty", Prefix.ANON),
	SML_NUMBER_OF_VALUES("numberOfValues", Prefix.ANON), SML_NUMBER_OF_VALID_VALUES("numberOfValidValues", Prefix.ANON),
	SML_NUMBER_OF_VALID_NON_NULL_VALUES("numberOfValidNonNullValues", Prefix.ANON),
	SML_NUMBER_OF_NULL_VALUES("numberOfNullValues", Prefix.ANON),
	SML_NUMBER_OF_DISTINCT_VALUES("numberOfDistinctValues", Prefix.ANON),
	SML_NUMBER_OF_INVALID_VALUES("numberOfInvalidValues", Prefix.ANON), SEAS_EVALUATION("evaluation", Prefix.SEAS),
	SEAS_EVALUATED_VALUE("evaluatedValue", Prefix.SEAS), SEAS_RANK("rank", Prefix.SEAS),
	SML_HAS_VALUE_DISTRIBUTION("hasValueDistribution", Prefix.ANON),
	SML_INSTANCES_OF_VALUE("instancesOfValue", Prefix.ANON),
	SML_NUMBER_OF_INSTANCES_OF_VALUE("numberOfInstancesOfValue", Prefix.ANON),
	SML_VALUE_DISTRIBUTION_VALUE("valueDistributionValue", Prefix.ANON),
	SML_HAS_SPATIAL_DISTRIBUTION("hasSpatialDistribution", Prefix.ANON),
	SML_SPATIAL_DISTRIBUTION_VALUE("spatialDistributionValue", Prefix.ANON),
	SML_INSTANCES_OF_REGION("instancesOfRegion", Prefix.ANON),
	SML_NUMBER_OF_INSTANCES_OF_REGION("numberOfInstancesInRegion", Prefix.ANON), SML_AS_WKT("asWKT", Prefix.ANON),
	SML_COMMENT("comment", Prefix.ANON), SML_HAS_SAMPLE("hasSample", Prefix.ANON), SML_HAS_LINE("hasLine", Prefix.ANON),
	SML_HAS_CONTENT("hasContent", Prefix.ANON), SML_HAS_HEADER("hasHeader", Prefix.ANON),
	SML_HAS_HEADER_ATTRIBUTES("hasHeaderAttributes", Prefix.ANON), SML_RANK("rank", Prefix.ANON),
	SML_NUMBER_OF_INSTANCES("numberOfInstances", Prefix.ANON);

	private String name;
	private Prefix prefix;

	private PropertyInstance(String name, Prefix prefix) {
		this.name = name;
		this.prefix = prefix;
	}

	public String getName() {
		return name;
	}

	public Prefix getPrefix() {
		return prefix;
	}

	public String getURI() {
		return prefix.getUrl() + name;
	}

	public Property getProperty(OntModel model) {
		return model.getProperty(getURI());
	}

	public String getPrefixedName(Prefix basePrefix) {
		if (this.prefix == basePrefix)
			return "<" + this.name + ">";
		else
			return this.prefix.getPrefix() + this.name;
	}

}
