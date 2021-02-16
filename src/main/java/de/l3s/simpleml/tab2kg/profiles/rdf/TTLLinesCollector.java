package de.l3s.simpleml.tab2kg.profiles.rdf;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.jena.rdf.model.Statement;

import de.l3s.simpleml.tab2kg.model.sparql.Prefix;
import de.l3s.simpleml.tab2kg.model.sparql.PropertyInstance;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.profiles.rdf.model.LanguageString;
import de.l3s.simpleml.tab2kg.profiles.rdf.model.PrefixedInstance;
import de.l3s.simpleml.tab2kg.profiles.rdf.model.TypedLiteral;

public class TTLLinesCollector {

	private List<Prefix> usedPrefixes = new ArrayList<Prefix>();
	private Prefix basePrefix;

	public TTLLinesCollector(Prefix basePrefix) {
		super();
		this.basePrefix = basePrefix;
	}

	public String createLine(PrefixedInstance subject, PrefixedInstance predicate, PrefixedInstance object) {
		if (!this.usedPrefixes.contains(subject.getPrefix()))
			this.usedPrefixes.add(subject.getPrefix());
		if (!this.usedPrefixes.contains(predicate.getPrefix()))
			this.usedPrefixes.add(predicate.getPrefix());
		if (!this.usedPrefixes.contains(object.getPrefix()))
			this.usedPrefixes.add(object.getPrefix());
		return subject.getUri() + " " + predicate.getUri() + " " + object.getUri();
	}

	public String createLine(PrefixedInstance subject, PropertyInstance predicate, TypedLiteral object) {
		PrefixedInstance predicateTmp = new PrefixedInstance(predicate.getPrefix(), predicate.getName(), basePrefix);
		return createLine(subject, predicateTmp, object);
	}

	public String createLine(PrefixedInstance subject, PrefixedInstance predicate, TypedLiteral object) {
		if (!this.usedPrefixes.contains(subject.getPrefix()))
			this.usedPrefixes.add(subject.getPrefix());
		if (!this.usedPrefixes.contains(predicate.getPrefix()))
			this.usedPrefixes.add(predicate.getPrefix());
		if (!this.usedPrefixes.contains(object.getDataTypePrefix()))
			this.usedPrefixes.add(object.getDataTypePrefix());

		return subject.getUri() + " " + predicate.getUri() + " " + object.getString();
	}

	public String createLine(PrefixedInstance subject, PrefixedInstance predicate, LanguageString object) {
		if (!this.usedPrefixes.contains(subject.getPrefix()))
			this.usedPrefixes.add(subject.getPrefix());
		if (!this.usedPrefixes.contains(predicate.getPrefix()))
			this.usedPrefixes.add(predicate.getPrefix());
		return subject.getUri() + " " + predicate.getUri() + " " + object.getString();
	}

	public String createLine(PrefixedInstance subject, PropertyInstance predicate, LanguageString object) {
		if (!this.usedPrefixes.contains(subject.getPrefix()))
			this.usedPrefixes.add(subject.getPrefix());
		if (!this.usedPrefixes.contains(predicate.getPrefix()))
			this.usedPrefixes.add(predicate.getPrefix());
		PrefixedInstance predicateTmp = new PrefixedInstance(predicate.getPrefix(), predicate.getName(), basePrefix);

		return createLine(subject, predicateTmp, object);
	}

	public String createLine(PrefixedInstance subject, PrefixedInstance predicate, String object) {
		if (!this.usedPrefixes.contains(subject.getPrefix()))
			this.usedPrefixes.add(subject.getPrefix());
		if (!this.usedPrefixes.contains(predicate.getPrefix()))
			this.usedPrefixes.add(predicate.getPrefix());
		return subject.getUri() + " " + predicate.getUri() + " " + "\"" + object + "\"";
	}

	public String createTypeLine(PrefixedInstance subject, PrefixedInstance object) {
		if (!this.usedPrefixes.contains(subject.getPrefix()))
			this.usedPrefixes.add(subject.getPrefix());
		if (!this.usedPrefixes.contains(object.getPrefix()))
			this.usedPrefixes.add(object.getPrefix());
		return subject.getUri() + " a " + object.getUri();
	}

	// ~~ Predicate two-fold

	public String createLine(PrefixedInstance subject, Prefix predicatePrefix, String predicateID,
			PrefixedInstance object) {
		PrefixedInstance predicate = new PrefixedInstance(predicatePrefix, predicateID, basePrefix);
		return createLine(subject, predicate, object);
	}

	public String createLine(PrefixedInstance subject, Prefix predicatePrefix, String predicateID,
			TypedLiteral object) {
		PrefixedInstance predicate = new PrefixedInstance(predicatePrefix, predicateID, basePrefix);
		return createLine(subject, predicate, object);
	}

	public String createLine(PrefixedInstance subject, Prefix predicatePrefix, String predicateID,
			LanguageString object) {
		PrefixedInstance predicate = new PrefixedInstance(predicatePrefix, predicateID, basePrefix);
		return createLine(subject, predicate, object);
	}

	public String createLine(PrefixedInstance subject, Prefix predicatePrefix, String predicateID, String object) {
		PrefixedInstance predicate = new PrefixedInstance(predicatePrefix, predicateID, basePrefix);
		return createLine(subject, predicate, object);
	}

	public String createLine(PrefixedInstance subject, PropertyInstance predicate, String object) {
		PrefixedInstance predicateTmp = new PrefixedInstance(predicate.getPrefix(), predicate.getName(), basePrefix);
		return createLine(subject, predicateTmp, object);
	}

	// ~~ Predicate and object two-fold

	public String createLine(PrefixedInstance subject, Prefix predicatePrefix, String predicateID, Prefix objectPrefix,
			String objectID) {
		PrefixedInstance predicate = new PrefixedInstance(predicatePrefix, predicateID, basePrefix);
		PrefixedInstance object = new PrefixedInstance(objectPrefix, objectID, basePrefix);
		return createLine(subject, predicate, object);
	}

	public String createLine(PrefixedInstance subject, PropertyInstance predicate, Prefix objectPrefix,
			String objectID) {
		PrefixedInstance predicateTmp = new PrefixedInstance(predicate.getPrefix(), predicate.getName(), basePrefix);
		PrefixedInstance object = new PrefixedInstance(objectPrefix, objectID, basePrefix);

		return createLine(subject, predicateTmp, object);
	}

	public String createLine(PrefixedInstance subject, PropertyInstance predicate, PrefixedInstance object) {
		PrefixedInstance predicateTmp = new PrefixedInstance(predicate.getPrefix(), predicate.getName(), basePrefix);

		return createLine(subject, predicateTmp, object);
	}

	public String createTypeLine(PrefixedInstance subject, Prefix objectPrefix, String objectID) {
		PrefixedInstance object = new PrefixedInstance(objectPrefix, objectID, basePrefix);
		return createTypeLine(subject, object);
	}

	public TypedLiteral createDateTime(Date date) {
		return new TypedLiteral(Prefix.XSD, "dateTime", ProfileCreatorTTL.RDF_DATE_TIME_FORMAT.format(date));
	}

	public TypedLiteral createBoolean(boolean value) {
		return new TypedLiteral(Prefix.XSD, "boolean", String.valueOf(value));
	}

	public TypedLiteral createDouble(Double value) {
		return new TypedLiteral(Prefix.XSD, "double", String.valueOf(value));
	}

	public TypedLiteral createNumberLiteral(DataTypeClass dataType, Number value) {
		return new TypedLiteral(dataType.getPrefix(), dataType.getName(), String.valueOf(value));
	}

	public TypedLiteral createLiteral(DataTypeClass dataType, Object value) {
		String valueStr = String.valueOf(value);
		if (dataType == DataTypeClass.XS_DATE_TIME || dataType == DataTypeClass.XS_TIME)
			valueStr = ProfileCreatorTTL.RDF_DATE_TIME_FORMAT.format((Date) value);
		return new TypedLiteral(dataType.getPrefix(), dataType.getName(), valueStr);
	}

	public TypedLiteral createInteger(Integer value) {
		return new TypedLiteral(Prefix.XSD, "integer", String.valueOf(value));
	}

	public TypedLiteral createLong(Long value) {
		return new TypedLiteral(Prefix.XSD, "long", String.valueOf(value));
	}

	public TypedLiteral createNonNegativeInteger(Integer value) {
		return new TypedLiteral(Prefix.XSD, "nonNegativeInteger", String.valueOf(value));
	}

	public List<String> createPrefixLines() {

		List<String> lines = new ArrayList<String>();

		lines.add("@base <" + basePrefix.getUrl() + "> .");
		lines.add("");

		for (Prefix prefix : this.usedPrefixes) {
			lines.add("@prefix " + prefix.getPrefix() + " <" + prefix.getUrl() + "> .");
		}

		return lines;
	}

	public String createLine(Statement statement) {
		PrefixedInstance subject = new PrefixedInstance(Prefix.getPrefixFromURL(statement.getSubject().getNameSpace()),
				statement.getSubject().getLocalName(), basePrefix);
		PrefixedInstance property = new PrefixedInstance(
				Prefix.getPrefixFromURL(statement.getPredicate().getNameSpace()),
				statement.getPredicate().getLocalName(), basePrefix);

		if (statement.getObject().isLiteral()) {
			return createLine(subject, property, createInteger(statement.getObject().asLiteral().getInt()));
		} else {
			PrefixedInstance object = new PrefixedInstance(
					Prefix.getPrefixFromURL(statement.getObject().asResource().getNameSpace()),
					statement.getObject().asResource().getLocalName(), basePrefix);
			return createLine(subject, property, object);
		}

	}

}
