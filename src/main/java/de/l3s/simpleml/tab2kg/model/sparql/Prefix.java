package de.l3s.simpleml.tab2kg.model.sparql;

public enum Prefix {

	RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"), SML("https://simple-ml.de/resource/", "sml"),
	ANON("https://example.org/resource/", "sml"), OWL("http://www.w3.org/2002/07/owl#", "owl"),
	DCTERMS("http://purl.org/dc/terms/", "dcterms"), DCELEMENTS("http://purl.org/dc/elements/1.1/", "dcelements"),
	SO("http://schema.org/", "so"), SIOC("http://rdfs.org/sioc/ns#", "sioc"),
	RDFS("http://www.w3.org/2000/01/rdf-schema#", "rdfs"), XSD("http://www.w3.org/2001/XMLSchema#", "xsd"),
	GEO("https://www.w3.org/2003/01/geo/wgs84_pos#", "geo"),
	GEO_OLD("http://www.w3.org/2003/01/geo/wgs84_pos#", "geo_old"), TIME("http://www.w3.org/2006/time#", "time"),
	FOAF("http://xmlns.com/foaf/0.1/", "foaf"), GEOM("http://geovocab.org/geometry#", "geom"),
	SPATIAL("http://geovocab.org/spatial#", "spatial"), DCAT("http://www.w3.org/ns/dcat#", "dcat"),
	VOID("http://rdfs.org/ns/void#", "void"), CSVW("http://www.w3.org/ns/csvw#", "csvw"),
	R2RML("http://www.w3.org/ns/r2rml#", "r2rml"), SKOS("http://www.w3.org/2004/02/skos/core#", "skos"),
	VCARD("http://www.w3.org/2006/vcard/ns#", "vcard"), SEAS("https://w3id.org/seas/", "seas"),
	MEX("http://mex.aksw.org/mex-algo#", "mex");

	public static Prefix getPrefixFromURL(String url) {
		for (Prefix prefix : values()) {
			if (prefix.getUrl().equals(url)) {
				return prefix;
			}
		}
		throw new IllegalArgumentException("Prefix with URL " + url + " does not exist.");
	}

	private String url;
	private String prefix;

	private Prefix(String url, String prefix) {
		this.url = url;
		this.prefix = prefix;
	}

	public String getUrl() {
		return url;
	}

	public String getPrefix() {
		return prefix + ":";
	}

}
