package de.l3s.simpleml.tab2kg.catalog.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.rdf.model.Resource;
import org.locationtech.jts.geom.Geometry;

import de.l3s.simpleml.tab2kg.catalog.model.dataset.DataSet;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatistics;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL1;
import de.l3s.simpleml.tab2kg.model.sparql.MappedClass;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.profiles.rdf.model.PrefixedInstance;
import de.l3s.simpleml.tab2kg.util.GeoUtil;
import de.l3s.simpleml.tab2kg.util.Tab2KGConfiguration;
import de.l3s.simpleml.tab2kg.util.time.DateUtil;

public class Attribute {

	private String uri;

	private Integer columnIndex;
	// if we derive multiple attributes from the same attribute (using
	// transformation rules), keep track of their order
	private int subColumnIndex = 0;

	private boolean isActive;

	// many-to-one attributes
	private List<Integer> columnIndexes;

	private String identifier;
	private String description;
	private String label;
	private String timeFormat;
	private Integer timeGroup;
	private Integer locationGroup;
	private String valueType;

	private DataSet dataSet;

	private boolean isMetaAttribute = false;

	private AttributeStatistics<?> statistics;

	private boolean isGeoAttribute = false;
	private boolean isTimeAttribute = false;

	private String predicateURI;
	private String subjectClassURI;

	private Resource subjectClass;

	private DataTypeClass dataTypeClass;

	private List<String> values = new ArrayList<String>();

	private DatatypeProperty mappedProperty;
	private MappedClass mappedClass;

	private Attribute representedAttribute;
	private Attribute predictedAttribute;

	private List<Double> features;

	private Map<Attribute, Double> similarities = new HashMap<Attribute, Double>();
	private Map<Attribute, Double> correlations = new HashMap<Attribute, Double>();

	private PrefixedInstance prefixedInstance;

	public Attribute() {
	}

	public Attribute(String label) {
		super();
		this.label = label;
	}

	public Attribute(int columnIndex) {
		super();
		this.columnIndex = columnIndex;
	}

	public Attribute(int columnIndex, String identifier) {
		super();
		this.columnIndex = columnIndex;
		this.identifier = identifier;
	}

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public List<Integer> getColumnIndexes() {
		return columnIndexes;
	}

	public void setColumnIndexes(List<Integer> columnIndexes) {
		this.columnIndexes = columnIndexes;
	}

	public Integer getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(Integer columnIndex) {
		this.columnIndex = columnIndex;
	}

	public String getValueType() {
		return valueType;
	}

	public void setValueType(String valueType) {
		this.valueType = valueType;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getTimeFormat() {
		return timeFormat;
	}

	public void setTimeFormat(String timeFormat) {
		this.timeFormat = timeFormat;
	}

	public Integer getTimeGroup() {
		return timeGroup;
	}

	public void setTimeGroup(Integer timeGroup) {
		this.timeGroup = timeGroup;
	}

	public Integer getLocationGroup() {
		return locationGroup;
	}

	public void setLocationGroup(Integer locationGroup) {
		this.locationGroup = locationGroup;
	}

	public Attribute copy() {

		Attribute attributeCopy = new Attribute();
		attributeCopy.setLabel(this.label);
		attributeCopy.setIdentifier(this.identifier);
		attributeCopy.setTimeFormat(this.timeFormat);
		attributeCopy.setTimeGroup(this.timeGroup);
		attributeCopy.setValueType(this.valueType);
		attributeCopy.setColumnIndex(this.columnIndex);
		attributeCopy.setTimeFormat(this.timeFormat);
		attributeCopy.setTimeGroup(this.timeGroup);
		attributeCopy.setLocationGroup(this.locationGroup);
		attributeCopy.setMetaAttribute(isMetaAttribute);
		attributeCopy.setURI(this.uri);
		attributeCopy.setDataSet(dataSet);
		attributeCopy.setGeoAttribute(this.isGeoAttribute);
		attributeCopy.setTimeAttribute(this.isTimeAttribute);
		attributeCopy.setActive(this.isActive);

		attributeCopy.setDataTypeClass(this.dataTypeClass);

		attributeCopy.setMappedProperty(this.mappedProperty);
		attributeCopy.setMappedClass(this.mappedClass.copy());

		if (this.columnIndexes != null) {
			attributeCopy.setColumnIndexes(new ArrayList<Integer>());
			for (int columnIndex : this.columnIndexes)
				attributeCopy.getColumnIndexes().add(columnIndex);
		}

		if (statistics != null)
			attributeCopy.setStatistics(statistics.copy());

		return attributeCopy;
	}

	public boolean isMetaAttribute() {
		return this.isMetaAttribute;
	}

	public void setMetaAttribute(boolean isMetaAttribute) {
		this.isMetaAttribute = isMetaAttribute;
	}

	public AttributeStatistics<?> getStatistics() {
		return statistics;
	}

	public AttributeStatistics<Number> getNumberStatistics() {
		return statistics.getNumberStatistics();
	}

	@SuppressWarnings("unchecked")
	public <T> AttributeStatistics<T> getStatistics(Class<T> class1) {
		return (AttributeStatistics<T>) getStatistics();
	}

	public void setStatistics(AttributeStatistics<?> statistics) {
		this.statistics = statistics;
	}

	public DataSet getDataSet() {
		return dataSet;
	}

	public void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	public int getSubColumnIndex() {
		return subColumnIndex;
	}

	public void setSubColumnIndex(int subColumnIndex) {
		this.subColumnIndex = subColumnIndex;
	}

	public boolean isGeoAttribute() {
		return isGeoAttribute;
	}

	public void setGeoAttribute(boolean isGeoAttribute) {
		this.isGeoAttribute = isGeoAttribute;
	}

	public boolean isTimeAttribute() {
		return isTimeAttribute;
	}

	public void setTimeAttribute(boolean isTimeAttribute) {
		this.isTimeAttribute = isTimeAttribute;
	}

	public String getPredicateURI() {
		return predicateURI;
	}

	public void setPredicateURI(String predicateURI) {
		this.predicateURI = predicateURI;
	}

	public String getSubjectClassURI() {
		return subjectClassURI;
	}

	public void setSubjectClass(Resource subjectClass) {
		this.subjectClass = subjectClass;
		this.subjectClassURI = subjectClass.getURI();
	}

	public Resource getSubjectClass() {
		return subjectClass;
	}

	public DataTypeClass getDataTypeClass() {
		return dataTypeClass;
	}

	public void setDataTypeClass(DataTypeClass dataTypeClass) {
		this.dataTypeClass = dataTypeClass;
	}

	public List<String> getValues() {
		return values;
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public void addValue(String value) {
		this.values.add(value);
	}

	public DatatypeProperty getMappedProperty() {
		return mappedProperty;
	}

	public void setMappedProperty(DatatypeProperty propertyX) {
		this.mappedProperty = propertyX;
	}

	public MappedClass getMappedClass() {
		return mappedClass;
	}

	public void setMappedClass(MappedClass mappedClass) {
		this.mappedClass = mappedClass;
	}

	public String getStringValue(int rowNumber, Tab2KGConfiguration config) {

		Object value = this.getStatistics().getAttributeValues().get(rowNumber).getValue();

		if (value == null) {
			return Tab2KGConfiguration.NULL_VALUE;
		} else if (this.getStatistics().getAttributeStatisticsType()
				.getTypeL1() == AttributeStatisticsTypeL1.TEMPORAL) {
			return DateUtil.formatDate((Date) value, this.getStatistics().getAttributeStatisticsType(), config);
		} else if (this.getStatistics().getAttributeStatisticsType().getTypeL1() == AttributeStatisticsTypeL1.SPATIAL) {
			return GeoUtil.createCoordinateArrayString((Geometry) value);
		} else {
			return value.toString();
		}
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Attribute getRepresentedAttribute() {
		return representedAttribute;
	}

	public void setRepresentedAttribute(Attribute representedAttribute) {
		this.representedAttribute = representedAttribute;
	}

	public Attribute getPredictedAttribute() {
		return predictedAttribute;
	}

	public void setPredictedAttribute(Attribute predictedAttribute) {
		this.predictedAttribute = predictedAttribute;
	}

	public List<Double> getFeatures() {
		return features;
	}

	public void setFeatures(List<Double> features) {
		this.features = features;
	}

	public Double getSimilarity(Attribute attribute) {
		return similarities.get(attribute);
	}

	public void addSimilarity(Attribute attribute, Double similarity) {
		this.similarities.put(attribute, similarity);
	}

	public Double getCorrelation(Attribute attribute) {
		return correlations.get(attribute);
	}

	public void addCorrelation(Attribute attribute, Double correlation) {
		this.correlations.put(attribute, correlation);
	}

	public PrefixedInstance getPrefixedInstance() {
		return prefixedInstance;
	}

	public void setPrefixedInstance(PrefixedInstance prefixedInstance) {
		this.prefixedInstance = prefixedInstance;
	}

}
