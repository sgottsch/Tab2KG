package de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.rdf.model.Statement;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.DataSetFile;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.DataSet;
import de.l3s.simpleml.tab2kg.model.sparql.Language;

public class DataTable extends DataSet {

	public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("hh:mm:ss");

	private Row headerRow;
	private List<Row> rows = new ArrayList<Row>();

	public DataTable() {
		super();
	}

	public DataTable(String delimiter) {
		super();
		setFile(new DataSetFile());
		getFile().setDelimiter(delimiter);
	}

	public DataTable(String fileName, String delimiter, boolean hasHeader) {
		super();

		setFile(new DataSetFile());
		getFile().setFileLocation(fileName);
		getFile().setDelimiter(delimiter);
		getFile().setHasHeader(hasHeader);
	}

	public DataTable(String fileName, String delimiter) {
		super();

		setFile(new DataSetFile());
		getFile().setFileLocation(fileName);
		getFile().setDelimiter(delimiter);
	}

	public String getFileName() {
		return getFile().getFileLocation();
	}

	public void setFileName(String fileName) {
		if (getFile() == null)
			setFile(new DataSetFile());

		getFile().setFileLocation(fileName);
	}

	public String getDelimiter() {
		return getFile().getDelimiter();
	}

	public void setDelimiter(String delimiter) {
		if (getFile() == null)
			setFile(new DataSetFile());

		getFile().setDelimiter(delimiter);
	}

	public Row getHeaderRow() {
		return headerRow;
	}

	public void setHeaderRow(Row headerRow) {
		this.headerRow = headerRow;
	}

	public List<Row> getRows() {
		return rows;
	}

	public void addRow(Row row) {
		this.rows.add(row);
	}

	public void printTableContent() {
		System.out.println(StringUtils.join(headerRow.getValues(), "\t"));
		System.out.println("-----");
		for (Row row : rows)
			System.out.println(StringUtils.join(row.getValues(), "\t"));
	}

	public String getNullValue() {
		return getFile().getNullValue();
	}

	public void setNullValue(String nullValue) {
		if (getFile() == null)
			setFile(new DataSetFile());

		getFile().setNullValue(nullValue);
	}

	public boolean hasHeader() {
		return getFile().hasHeader();
	}

	public void setHasHeader(boolean hasHeader) {
		if (getFile() == null)
			setFile(new DataSetFile());

		getFile().setHasHeader(hasHeader);
	}

	public boolean hasRowNumberColumn() {
		return getFile().hasRowNumberColumn();
	}

	public void setHasRowNumberColumn(boolean hasRowNumberColumn) {
		if (getFile() == null)
			setFile(new DataSetFile());

		getFile().setHasRowNumberColumn(hasRowNumberColumn);
	}

	public boolean hasColumnTitles() {
		return getFile().hasColumnTitles();
	}

	public void setHasColumnTitles(boolean hasColumnTitles) {
		if (getFile() == null)
			setFile(new DataSetFile());

		getFile().setHasColumnTitles(hasColumnTitles);
	}

	public DataTable(DataSet dataSet) {

		this.setTitle(dataSet.getTitle());
		this.setLabel(dataSet.getLabel());
		this.setCreatorId(dataSet.getCreatorId());
		this.setId(dataSet.getId());
		this.setUri(dataSet.getUri());
		this.setNumberOfAttributes(dataSet.getNumberOfAttributes());
		this.setNumberOfInstances(dataSet.getNumberOfInstances());
		this.setLatBeforeLon(dataSet.latBeforeLon());

		for (Language language : dataSet.getDescriptions().keySet())
			this.addDescription(language, dataSet.getDescriptions().get(language));

		for (Language language : dataSet.getTitles().keySet())
			this.addTitle(language, dataSet.getTitles().get(language));

		for (Language language : dataSet.getTopics().keySet()) {
			this.getTopics().put(language, new HashSet<String>());
			for (String subject : dataSet.getTopics().get(language)) {
				this.getTopics().get(language).add(subject);
			}
		}

		// if (geoStatistics != null)
		// this.setGeoStatistics(geoStatistics.copy());
		// if (timeStatistics != null)
		// this.setTimeStatistics(timeStatistics.copy());

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(dataSet.getAllAttributes());

		Map<String, Attribute> oldAttributeIDsToNewAttributes = new HashMap<String, Attribute>();

		for (Attribute attribute : attributes) {
			Attribute attributeCopied = attribute.copy();

			this.addAttribute(attributeCopied);
			attributeCopied.setDataSet(this);

			oldAttributeIDsToNewAttributes.put(attribute.getURI(), attributeCopied);
		}

		// file
		if (dataSet.getFile() != null)
			this.setFile(dataSet.getFile().copy());

		for (Statement st : dataSet.getMappingStatements()) {
			this.addMappingStatement(st);
		}

	}

	public Attribute getAttributeByIdentifier(String attributeId) {

		for (Attribute attribute : this.getAttributes()) {
			if (attribute.getIdentifier().equals(attributeId))
				return attribute;
		}

		return null;
	}
}
