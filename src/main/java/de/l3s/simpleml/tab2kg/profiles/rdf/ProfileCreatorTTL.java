package de.l3s.simpleml.tab2kg.profiles.rdf;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.DataSetFile;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.DataSet;
import de.l3s.simpleml.tab2kg.model.sparql.Language;
import de.l3s.simpleml.tab2kg.model.sparql.Prefix;
import de.l3s.simpleml.tab2kg.model.sparql.PropertyInstance;
import de.l3s.simpleml.tab2kg.profiles.features.FeatureContext;
import de.l3s.simpleml.tab2kg.profiles.features.NumericProfileFeature;
import de.l3s.simpleml.tab2kg.profiles.features.TemporalProfileFeature;
import de.l3s.simpleml.tab2kg.profiles.rdf.model.LanguageString;
import de.l3s.simpleml.tab2kg.profiles.rdf.model.PrefixedInstance;
import de.l3s.simpleml.tab2kg.util.IDGenerator;

public class ProfileCreatorTTL {

	public static final SimpleDateFormat RDF_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

	private PrefixedInstance dataSetInstance;
	private PrefixedInstance fileIdInstance;

	public static final Prefix BASE_PREFIX = Prefix.SML;

	private TTLLinesCollector lc;

	private DataSet dataSet;

	public ProfileCreatorTTL(DataSet dataSet) {
		this.dataSet = dataSet;
		this.dataSetInstance = new PrefixedInstance(BASE_PREFIX, dataSet.getId(), BASE_PREFIX);
		this.lc = new TTLLinesCollector(BASE_PREFIX);
	}

	public void createMetaFile(String catalogName, String profileOutputFileName) {

		List<String> allLines = new ArrayList<String>();

		allLines.add("### Dataset: " + this.dataSet.getId());
		allLines.addAll(createDatasetDescription(catalogName));
		allLines.add("");
		allLines.add("### File (" + this.dataSet.getId() + ")");
		allLines.add("");
		allLines.addAll(createFileDescription());
		allLines.add("");
		allLines.add("### Attributes (" + this.dataSet.getId() + ")");
		allLines.add("");
		allLines.addAll(createAttributesDescription());
//		allLines.add("### Semantic graph (" + this.dataSet.getId() + ")");
//		allLines.add("");
//		allLines.addAll(createSemanticGraph());

		allLines.add("");
		allLines.add("### Statistics (" + this.dataSet.getId() + ")");

		allLines.addAll(writeDataSetStatisticsForCatalog());
		for (Attribute attribute : dataSet.getAttributes()) {
			allLines.add("### Statistics for \"" + attribute.getIdentifier() + "\" (" + this.dataSet.getId() + ")");
			allLines.addAll(writeAttributeStatisticsForCatalog(attribute));
		}

		List<String> allLinesWithPrefixes = lc.createPrefixLines();
		allLinesWithPrefixes.add("");
		allLinesWithPrefixes.addAll(allLines);

		PrintWriter writer = null;
		try {
			writer = new PrintWriter(profileOutputFileName);
			for (String line : allLinesWithPrefixes)
				writer.println(line);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

//	private Collection<? extends String> createSemanticGraph() {
//		List<String> lines = new ArrayList<String>();
//
//		for (Statement statement : this.dataSet.getMappingStatements()) {
//			lines.add(lc.createLine(statement));
//		}
//
//		addLineEndings(lines);
//
//		return lines;
//	}

	private List<String> createDatasetDescription(String catalogName) {

		List<String> lines = new ArrayList<String>();

		PrefixedInstance catalog = new PrefixedInstance(Prefix.SML, catalogName, BASE_PREFIX);

		if (catalogName != null)
			lines.add(lc.createLine(catalog, Prefix.DCAT, "dataset", this.dataSetInstance));

		lines.add(lc.createTypeLine(this.dataSetInstance, Prefix.DCAT, "Dataset"));

		for (Language language : dataSet.getTitles().keySet())
			lines.add(lc.createLine(this.dataSetInstance, PropertyInstance.DCTERMS_TITLE,
					new LanguageString(language, dataSet.getTitles().get(language))));
		for (Language language : dataSet.getDescriptions().keySet())
			lines.add(lc.createLine(this.dataSetInstance, PropertyInstance.DCTERMS_DESCRIPTION,
					new LanguageString(language, dataSet.getDescriptions().get(language))));
		for (Language language : dataSet.getTopics().keySet()) {
			for (String subject : dataSet.getTopics().get(language)) {
				lines.add(lc.createLine(this.dataSetInstance, PropertyInstance.DCTERMS_SUBJECT,
						new LanguageString(language, subject)));
			}
		}

		if (dataSet.getCreatorId() != null)
			lines.add(lc.createLine(this.dataSetInstance, PropertyInstance.SML_CREATOR_ID,
					lc.createNonNegativeInteger(dataSet.getCreatorId())));
		if (dataSet.getNumberOfInstances() != null)
			lines.add(lc.createLine(this.dataSetInstance, PropertyInstance.SML_NUMBER_OF_INSTANCES,
					lc.createNonNegativeInteger(dataSet.getNumberOfInstances())));
		if (dataSet.latBeforeLon() != null)
			lines.add(lc.createLine(this.dataSetInstance, PropertyInstance.SML_LAT_BEFORE_LON,
					lc.createBoolean(dataSet.latBeforeLon())));

		addLineEndings(lines);

		return lines;
	}

	private List<String> createFileDescription() {
		List<String> lines = new ArrayList<String>();

		DataSetFile file = dataSet.getFile();

		if (file == null)
			return lines;

		this.fileIdInstance = new PrefixedInstance(BASE_PREFIX, IDGenerator.createURLString(dataSet.getId(), "File"),
				BASE_PREFIX);

		lines.add(lc.createLine(this.dataSetInstance, Prefix.SML, "hasFile", this.fileIdInstance));

		lines.add(lc.createTypeLine(this.fileIdInstance, Prefix.SML, "TextFile"));
		lines.add(lc.createLine(this.fileIdInstance, Prefix.SML, "fileLocation",
				file.getFileLocation().substring(file.getFileLocation().lastIndexOf("/") + 1)));
		if (file.getSeparator().equals("	") || file.getSeparator().equals("\t"))
			lines.add(lc.createLine(this.fileIdInstance, Prefix.DCTERMS, "format", "text/tab-separated-values"));
		else
			lines.add(lc.createLine(this.fileIdInstance, Prefix.DCTERMS, "format", "text/comma-separated-values"));
		lines.add(lc.createLine(this.fileIdInstance, Prefix.CSVW, "separator", file.getSeparator()));
		lines.add(lc.createLine(this.fileIdInstance, Prefix.CSVW, "header", lc.createBoolean(file.hasHeader())));

		if (file.getNullValue() != null)
			lines.add(lc.createLine(this.fileIdInstance, Prefix.CSVW, "null", file.getNullValue()));

		addLineEndings(lines);

		return lines;
	}

	private static void addLineEndings(List<String> lines) {

		// group lines by subject
		Map<String, List<String>> linesBySubject = new LinkedHashMap<String, List<String>>();

		for (String line : lines) {
			String subject = line.split(" ")[0];
			if (!linesBySubject.containsKey(subject))
				linesBySubject.put(subject, new ArrayList<String>());
			linesBySubject.get(subject).add(line);
		}

		lines.clear();
		for (String subject : linesBySubject.keySet()) {
			List<String> linesOfSubject = linesBySubject.get(subject);
			for (int i = 0; i < linesOfSubject.size(); i++) {
				String line = linesOfSubject.get(i);
				if (i != 0) {
					line = line.replaceFirst(subject, "");
				}
				if (i == linesOfSubject.size() - 1)
					line += " .";
				else
					line += " ;";
				lines.add(line);
			}
		}

	}

	private List<String> createAttributesDescription() {

		List<String> lines = new ArrayList<String>();

		for (Attribute attribute : dataSet.getAttributes()) {
			List<String> attributeLines = new ArrayList<String>();

			// String attributeId = "<" + dataSet.getId() + "Attribute" +
			// attribute.getColumnIndex() + ">";
			PrefixedInstance attributeInstance = new PrefixedInstance(Prefix.SML, attribute.getURI(), BASE_PREFIX);
			attribute.setPrefixedInstance(attributeInstance);
			attributeLines.add(lc.createLine(dataSetInstance, Prefix.SML, "hasAttribute", attributeInstance));
			attributeLines.add(lc.createTypeLine(attributeInstance, Prefix.SML, "Attribute"));
			attributeLines
					.add(lc.createLine(attributeInstance, Prefix.DCTERMS, "identifier", attribute.getIdentifier()));
			attributeLines.add(lc.createLine(attributeInstance, Prefix.SML, "columnIndex",
					lc.createInteger(attribute.getColumnIndex())));
			if (attribute.getLabel() != null)
				attributeLines.add(lc.createLine(attributeInstance, Prefix.RDFS, "label",
						new LanguageString(Language.EN, attribute.getLabel())));

			if (attribute.getMappedProperty() == null) {
//				attributeLines.add(lc.createLine(attributeInstance, Prefix.SML, "valueType",
//						attribute.getDataTypeClass().getPrefix(), attribute.getDataTypeClass().getName()));
			} else {
				PrefixedInstance valueTypeInstance = new PrefixedInstance(attribute.getDataTypeClass().getPrefix(),
						attribute.getDataTypeClass().getName(), BASE_PREFIX);

				attributeLines.add(lc.createLine(attributeInstance, Prefix.SML, "valueType", valueTypeInstance));
			}

			if (attribute.getMappedClass() != null) {

				PrefixedInstance subjectInstance = new PrefixedInstance(Prefix.SML,
						attribute.getMappedClass().getSubject().getLocalName(), BASE_PREFIX);

				PrefixedInstance propertyInstance = new PrefixedInstance(
						Prefix.getPrefixFromURL(attribute.getMappedProperty().getNameSpace()),
						attribute.getMappedProperty().getLocalName(), BASE_PREFIX);

				attributeLines.add(lc.createLine(attributeInstance, Prefix.SML, "mapsToDomain", subjectInstance));
				attributeLines.add(lc.createLine(attributeInstance, Prefix.SML, "mapsToProperty", propertyInstance));
			}

			addLineEndings(attributeLines);
			lines.addAll(attributeLines);
			lines.add("");
		}

		return lines;

	}

	public List<String> writeDataSetStatisticsForCatalog() {
		List<String> lines = new ArrayList<String>();
		lines.addAll(createNumberOfInstancesLine(dataSet, dataSet.getNumberOfInstances()));

		addLineEndings(lines);
		return lines;
	}

	public List<String> writeAttributeStatisticsForCatalog(Attribute attribute) {
		List<String> lines = new ArrayList<String>();

		if (attribute.getStatistics() == null)
			return lines;

		for (NumericProfileFeature<?> feature : attribute.getStatistics().getNumericProfileFeatures()) {
			PrefixedInstance object = null;
			if (feature.getRank() == null)
				object = new PrefixedInstance(Prefix.SML, attribute.getPrefixedInstance().getId()
						+ StringUtils.capitalize(feature.getProfileFeatureEnum().getShortName()), BASE_PREFIX);
			else
				object = new PrefixedInstance(Prefix.SML, attribute.getPrefixedInstance().getId()
						+ StringUtils.capitalize(feature.getProfileFeatureEnum().getShortName()) + feature.getRank(),
						BASE_PREFIX);

			if (feature.getProfileFeatureEnum().isEvaluation()) {
				PrefixedInstance type = new PrefixedInstance(feature.getProfileFeatureEnum().getPrefix(),
						feature.getProfileFeatureEnum().getName(), BASE_PREFIX);

				lines.add(lc.createLine(attribute.getPrefixedInstance(), PropertyInstance.SEAS_EVALUATION, object));
				lines.add(lc.createLine(object, PropertyInstance.RDF_TYPE, type));

				lines.add(lc.createLine(object, Prefix.SEAS, "evaluatedValue",
						lc.createNumberLiteral(feature.getDataTypeClass(), feature.getValue())));

				if (feature.getRank() != null)
					lines.add(lc.createLine(object, PropertyInstance.SEAS_RANK, lc.createInteger(feature.getRank())));

			} else {
				lines.add(lc.createLine(attribute.getPrefixedInstance(), feature.getProfileFeatureEnum().getPrefix(),
						feature.getProfileFeatureEnum().getName(),
						lc.createNumberLiteral(feature.getDataTypeClass(), feature.getValue())));
			}

			lines.addAll(getFeatureContextLines(object, feature.getFeatureContexts()));
		}

		for (TemporalProfileFeature feature : attribute.getStatistics().getTemporalProfileFeatures()) {

			PrefixedInstance object = null;
			if (feature.getRank() == null)
				object = new PrefixedInstance(Prefix.SML,
						attribute.getPrefixedInstance().getId() + feature.getProfileFeatureEnum().getShortName(),
						BASE_PREFIX);
			else
				object = new PrefixedInstance(Prefix.SML, attribute.getPrefixedInstance().getId()
						+ StringUtils.capitalize(feature.getProfileFeatureEnum().getShortName()) + feature.getRank(),
						BASE_PREFIX);

			if (feature.getProfileFeatureEnum().isEvaluation()) {
				PrefixedInstance type = new PrefixedInstance(feature.getProfileFeatureEnum().getPrefix(),
						StringUtils.capitalize(feature.getProfileFeatureEnum().getName()), BASE_PREFIX);

				lines.add(lc.createLine(attribute.getPrefixedInstance(), PropertyInstance.SEAS_EVALUATION, object));
				lines.add(lc.createLine(object, PropertyInstance.RDF_TYPE, type));

				lines.add(lc.createLine(object, Prefix.SEAS, "evaluatedValue", lc.createDateTime(feature.getValue())));

				if (feature.getRank() != null)
					lines.add(lc.createLine(object, PropertyInstance.SEAS_RANK, lc.createInteger(feature.getRank())));
			} else {
				lines.add(lc.createLine(attribute.getPrefixedInstance(), feature.getProfileFeatureEnum().getPrefix(),
						feature.getProfileFeatureEnum().getName(), lc.createDateTime(feature.getValue())));
			}

			lines.addAll(getFeatureContextLines(object, feature.getFeatureContexts()));
		}

		addLineEndings(lines);

		return lines;
	}

	private List<String> getFeatureContextLines(PrefixedInstance object, Set<FeatureContext<?>> featureContexts) {
		List<String> lines = new ArrayList<String>();
		for (FeatureContext<?> featureContext : featureContexts) {
			lines.add(lc.createLine(object, featureContext.getProfileFeatureContextEnum().getPrefix(),
					featureContext.getProfileFeatureContextEnum().getName(),
					lc.createLiteral(featureContext.getDataType(), featureContext.getValue())));
		}
		return lines;

	}

	private List<String> createNumberOfInstancesLine(DataSet dataSet, Integer numberOfInstances) {
		List<String> lines = new ArrayList<String>();

		if (numberOfInstances == null)
			return lines;

		lines.add(lc.createLine(dataSetInstance, Prefix.SML, "numberOfInstances", lc.createInteger(numberOfInstances)));

		return lines;
	}

}
