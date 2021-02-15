package de.l3s.simpleml.tab2kg.model.sparql;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;

public enum ClassInstance {

	SEAS_MEDIAN("DistributionMedianEvaluation", Prefix.SEAS), SEAS_MIN("DistributionMinimumEvaluation", Prefix.SEAS),
	SEAS_MAX("DistributionMaximumEvaluation", Prefix.SEAS), SEAS_SD("StandardDeviationEvaluation", Prefix.SEAS),
	SEAS_MEAN("DistributionMeanEvaluation", Prefix.SEAS);

	private String name;
	private Prefix prefix;

	private ClassInstance(String name, Prefix prefix) {
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

	public OntClass getOntClass(OntModel model) {
		return model.getOntClass(getURI());
	}

	public String getPrefixedName(Prefix basePrefix) {
		if (this.prefix == basePrefix)
			return "<" + this.name + ">";
		else
			return this.prefix.getPrefix() + this.name;
	}

}
