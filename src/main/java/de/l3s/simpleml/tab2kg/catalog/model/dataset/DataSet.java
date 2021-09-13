package de.l3s.simpleml.tab2kg.catalog.model.dataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Statement;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.DataSetFile;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL1;
import de.l3s.simpleml.tab2kg.model.sparql.Language;

public class DataSet {

	private String uri;
	private String id;

	private Date created;
	private Integer creatorId;
	private Date modified;
	private String label;
	private String title;
	private String description;
	private String publisher;
	private Boolean spatial;
	private Boolean temporal;
	private Integer numberOfInstances;
	private Integer numberOfAttributes;

	private Map<Language, String> descriptions = new HashMap<Language, String>();
	private Map<Language, String> titles = new HashMap<Language, String>();

	private DataSetFile file;

	private List<Attribute> attributes = new ArrayList<Attribute>();
	private List<Attribute> allAttributes = new ArrayList<Attribute>();
	private List<Attribute> idAttributes = new ArrayList<Attribute>();

	private Map<Language, Set<String>> topics = new HashMap<Language, Set<String>>();

	private List<Statement> mappingStatements = new ArrayList<Statement>();

	/**
	 * true, if the latitude of spatial attributes comes before the longitude. false
	 * otherwise.
	 */
	private Boolean latBeforeLon = null;

	private Boolean valid;

	public DataSet(String id) {
		super();
		this.id = id;
	}

	public DataSet() {
		this.attributes = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public boolean isSpatial() {
		return spatial;
	}

	public void setSpatial(boolean spatial) {
		this.spatial = spatial;
	}

	public boolean isTemporal() {
		return temporal;
	}

	public void setTemporal(boolean temporal) {
		this.temporal = temporal;
	}

	public Integer getNumberOfInstances() {
		return numberOfInstances;
	}

	public void setNumberOfInstances(Integer numberOfEntries) {
		this.numberOfInstances = numberOfEntries;
	}

	public Integer getNumberOfAttributes() {
		return numberOfAttributes;
	}

	public void setNumberOfAttributes(Integer numberOfAttributes) {
		this.numberOfAttributes = numberOfAttributes;
	}

	public DataSetFile getFile() {
		return file;
	}

	public void setFile(DataSetFile file) {
		this.file = file;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public Attribute getAttributeByColumnIndex(int columnIndex) {
		for (Attribute attribute : this.attributes)
			if (attribute.getColumnIndex() == columnIndex)
				return attribute;
		return null;
	}

	public void addAttribute(Attribute attribute) {

		if (attribute.isActive())
			attributes.add(attribute);

		allAttributes.add(attribute);
	}

	public void reOrderAttributesByColumnIndexes() {
		List<Attribute> orderedAttributes = new ArrayList<Attribute>(attributes.size());
		for (int columnIndex = 0; columnIndex < attributes.size(); columnIndex++) {
			for (Attribute attribute : attributes) {
				if (attribute.getColumnIndex() == columnIndex)
					orderedAttributes.add(attribute);
			}
		}
		this.attributes = orderedAttributes;

		List<Attribute> orderedIDAttributes = new ArrayList<Attribute>(idAttributes.size());
		for (int columnIndex = 0; columnIndex < idAttributes.size(); columnIndex++) {
			for (Attribute attribute : idAttributes) {
				if (attribute.getColumnIndex() == columnIndex)
					orderedIDAttributes.add(attribute);
			}
		}
		this.idAttributes = orderedIDAttributes;

		List<Attribute> orderedAllAttributes = new ArrayList<Attribute>(allAttributes.size());
		for (int columnIndex = 0; columnIndex < allAttributes.size(); columnIndex++) {
			for (Attribute attribute : allAttributes) {
				if (attribute.getColumnIndex() == columnIndex)
					orderedAllAttributes.add(attribute);
			}
		}
		this.allAttributes = orderedAllAttributes;
	}

	public DataSet copy() {
		DataSet copiedDataSet = new DataSet();

		copiedDataSet.setTitle(getTitle());
		copiedDataSet.setLabel(getLabel());
		copiedDataSet.setId(getId());
		copiedDataSet.setUri(getUri());
		copiedDataSet.setNumberOfAttributes(getNumberOfAttributes());
		copiedDataSet.setNumberOfInstances(getNumberOfInstances());
		copiedDataSet.setLatBeforeLon(latBeforeLon());

		for (Language language : getDescriptions().keySet())
			copiedDataSet.addDescription(language, getDescriptions().get(language));

		for (Language language : getTitles().keySet())
			copiedDataSet.addTitle(language, getTitles().get(language));

		for (Language language : getTopics().keySet()) {
			copiedDataSet.getTopics().put(language, new HashSet<String>());
			for (String subject : getTopics().get(language)) {
				copiedDataSet.getTopics().get(language).add(subject);
			}
		}

		// if (geoStatistics != null)
		// copiedDataSet.setGeoStatistics(geoStatistics.copy());
		// if (timeStatistics != null)
		// copiedDataSet.setTimeStatistics(timeStatistics.copy());

		List<Attribute> attributes = new ArrayList<Attribute>();
		attributes.addAll(this.getAllAttributes());

		Map<String, Attribute> oldAttributeIDsToNewAttributes = new HashMap<String, Attribute>();

		for (Attribute attribute : attributes) {

			Attribute attributeCopied = attribute.copy();

			copiedDataSet.addAttribute(attributeCopied);
			attributeCopied.setDataSet(copiedDataSet);

			oldAttributeIDsToNewAttributes.put(attribute.getURI(), attributeCopied);
		}

		// file
		if (this.getFile() != null)
			copiedDataSet.setFile(this.getFile().copy());

		for (Statement st : getMappingStatements()) {
			copiedDataSet.addMappingStatement(st);
		}

		return copiedDataSet;
	}

	public String getDescription(Language language) {
		return this.descriptions.get(language);
	}

	public void addDescription(Language language, String description) {
		this.descriptions.put(language, description);
	}

	public void addTopic(Language language, String subject) {
		if (!this.topics.containsKey(language))
			this.topics.put(language, new HashSet<String>());
		this.topics.get(language).add(subject);
	}

	public String getTitle(Language language) {
		return this.titles.get(language);
	}

	public String addTitle(Language language, String title) {
		return this.titles.put(language, title);
	}

	public Map<Language, Set<String>> getTopics() {
		return topics;
	}

	public Set<String> getTopics(Language language) {
		return topics.get(language);
	}

	public void setTopics(Map<Language, Set<String>> subjects) {
		this.topics = subjects;
	}

	public Map<Language, String> getDescriptions() {
		return descriptions;
	}

	public Map<Language, String> getTitles() {
		return titles;
	}

	public void setDescriptions(Map<Language, String> descriptions) {
		this.descriptions = descriptions;
	}

	public void setTitles(Map<Language, String> titles) {
		this.titles = titles;
	}

	public List<Attribute> getIdAttributes() {
		return idAttributes;
	}

	public void setIdAttributes(ArrayList<Attribute> idAttributes) {
		this.idAttributes = idAttributes;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Attribute getAttributeByURI(String attributeURI) {
		for (Attribute attribute : this.attributes) {
			if (attribute.getURI().equals(attributeURI))
				return attribute;
		}
		return null;
	}

	public Attribute getAttributeByID(String attributeID) {
		for (Attribute attribute : this.allAttributes) {
			if (attribute.getIdentifier().equals(attributeID))
				return attribute;
		}
		return null;
	}

	public List<Statement> getMappingStatements() {
		return mappingStatements;
	}

	public void addMappingStatement(Statement mappingStatement) {
		this.mappingStatements.add(mappingStatement);
	}

	public Integer getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(int creatorId) {
		this.creatorId = creatorId;
	}

	public Boolean latBeforeLon() {
		return latBeforeLon;
	}

	public void setLatBeforeLon(Boolean latBeforeLon) {
		this.latBeforeLon = latBeforeLon;
	}

	public boolean hasAttributeOfType(AttributeStatisticsTypeL1 attributeType) {

		for (Attribute attribute : this.attributes) {
			if (attribute.getStatistics().getAttributeStatisticsType().getTypeL1() == attributeType) {
				return true;
			}
		}

		return false;
	}

	public List<Attribute> getAllAttributes() {
		return allAttributes;
	}

	public void setAllAttributes(ArrayList<Attribute> allAttributes) {
		this.allAttributes = allAttributes;
	}

	public List<Attribute> getAllAttributes(AttributeStatisticsTypeL1 type) {

		List<Attribute> filteredAttributes = new ArrayList<Attribute>();

		for (Attribute attribute : this.allAttributes) {
			if (attribute.getStatistics().getAttributeStatisticsType().getTypeL1() == type)
				filteredAttributes.add(attribute);
		}

		return filteredAttributes;
	}

	public Boolean isValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

}
