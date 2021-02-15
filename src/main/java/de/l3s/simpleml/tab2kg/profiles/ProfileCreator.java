package de.l3s.simpleml.tab2kg.profiles;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.locationtech.jts.geom.Geometry;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.AttributeValue;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatistics;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL2;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL3;
import de.l3s.simpleml.tab2kg.datareader.AttributeAnalyzer;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.util.RDFUtil;

/**
 * Creates a RDF profile given the header line of a CSV file.
 */
public class ProfileCreator {

	public static final SimpleDateFormat RDF_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");

	public static void identifyDataTypeAndAddStatistics(Attribute attribute) {

		DataTypeClass dataTypeClass = null;
		boolean isCategoric = false;

		if (attribute.getMappedProperty() == null) {
			AttributeAnalyzer columnAnalyzer = new AttributeAnalyzer();
			dataTypeClass = columnAnalyzer.detectDataType(attribute);
			isCategoric = columnAnalyzer.isCategorical();
		} else {
			dataTypeClass = RDFUtil.getDataTypeClass(attribute.getMappedProperty());
			if (dataTypeClass == DataTypeClass.XS_STRING || dataTypeClass == DataTypeClass.RDF_LANG_STRING) {
				isCategoric = detectCategoricString(attribute);
			}
		}

		setDataTypeAndSetStatistics(attribute, dataTypeClass);
		if (isCategoric)
			attribute.getStatistics().setIsCategorical(true);
	}

	public static boolean detectCategoricString(Attribute attribute) {

		int numberOfValues = 0;
		int numberOfDistinctValues = 0;
		Set<String> uniqueValues = new HashSet<String>();

		for (String value : attribute.getValues()) {
			if (value != null) {
				uniqueValues.add(value);
				numberOfValues += 1;
			}
		}
		numberOfDistinctValues = uniqueValues.size();

		System.out.println(numberOfDistinctValues + " | " + numberOfValues + " -> categ.:"
				+ AttributeAnalyzer.isCategorical(numberOfValues, numberOfDistinctValues));
		return AttributeAnalyzer.isCategorical(numberOfValues, numberOfDistinctValues);
	}

	public static void setDataTypeAndSetStatistics(Attribute attribute, DataTypeClass dataTypeClass) {
		attribute.setDataTypeClass(dataTypeClass);

		// see DataTableFromInputGraphCreator
		if (dataTypeClass == DataTypeClass.XS_LONG) {
			attribute.setStatistics(new AttributeStatistics<Long>(AttributeStatisticsTypeL2.LONG));
		} else if (dataTypeClass == DataTypeClass.XS_INTEGER || dataTypeClass == DataTypeClass.XS_INT
				|| dataTypeClass == DataTypeClass.XS_POSITIVE_INTEGER
				|| dataTypeClass == DataTypeClass.XS_NEGATIVE_INTEGER
				|| dataTypeClass == DataTypeClass.XS_NON_POSITIVE_INTEGER
				|| dataTypeClass == DataTypeClass.XS_NON_NEGATIVE_INTEGER) {
			attribute.setStatistics(new AttributeStatistics<Integer>(AttributeStatisticsTypeL2.INTEGER));
		} else if (dataTypeClass == DataTypeClass.XS_STRING || dataTypeClass == DataTypeClass.RDF_LANG_STRING) {
			attribute.setStatistics(new AttributeStatistics<String>(AttributeStatisticsTypeL2.STRING));
			attribute.getStatistics()
					.setWordLengthStatistics(new AttributeStatistics<Integer>(AttributeStatisticsTypeL2.INTEGER));
		} else if (dataTypeClass == DataTypeClass.XS_ANY_URI) {
			attribute.setStatistics(new AttributeStatistics<String>(AttributeStatisticsTypeL2.STRING));
			attribute.getStatistics()
					.setWordLengthStatistics(new AttributeStatistics<Integer>(AttributeStatisticsTypeL2.INTEGER));
		} else if (dataTypeClass == DataTypeClass.XS_DOUBLE || dataTypeClass == DataTypeClass.XS_DECIMAL
				|| dataTypeClass == DataTypeClass.XS_FLOAT) {
			attribute.setStatistics(new AttributeStatistics<Double>(AttributeStatisticsTypeL2.DOUBLE));
		} else if (dataTypeClass == DataTypeClass.XS_TIME) {
			attribute.setStatistics(new AttributeStatistics<Date>(AttributeStatisticsTypeL2.TIME));
			attribute.getStatistics()
					.setMilliSecondsStatistics(new AttributeStatistics<Long>(AttributeStatisticsTypeL2.LONG));
		} else if (dataTypeClass == DataTypeClass.XS_DATE_TIME) {
			attribute.setStatistics(new AttributeStatistics<Date>(AttributeStatisticsTypeL2.DATETIME));
			attribute.getStatistics()
					.setMilliSecondsStatistics(new AttributeStatistics<Long>(AttributeStatisticsTypeL2.LONG));
		} else if (dataTypeClass == DataTypeClass.XS_DATE) {
			attribute.setStatistics(new AttributeStatistics<Date>(AttributeStatisticsTypeL2.DATE));
			attribute.getStatistics()
					.setMilliSecondsStatistics(new AttributeStatistics<Long>(AttributeStatisticsTypeL2.LONG));
		} else if (dataTypeClass == DataTypeClass.XS_BOOLEAN) {
			attribute.setStatistics(new AttributeStatistics<Boolean>(AttributeStatisticsTypeL2.BOOLEAN));
		} else if (dataTypeClass == DataTypeClass.SML_WKB || dataTypeClass == DataTypeClass.SML_WKT) {
			attribute.setStatistics(new AttributeStatistics<Geometry>(AttributeStatisticsTypeL2.GEO));
			attribute.getStatistics().setIsWKB(dataTypeClass == DataTypeClass.SML_WKB);
			attribute.getStatistics()
					.setGeoDimensionStatistics(new AttributeStatistics<Double>(AttributeStatisticsTypeL2.DOUBLE));
		} else {
			System.out.println("Missing statistics type: " + dataTypeClass);
		}
	}

	public static void identifyTypeL3(Attribute attribute) {

		switch (attribute.getStatistics().getAttributeStatisticsType().getTypeL1()) {
		case BOOLEAN:
			// covered in L1/L2
			break;
		case SPATIAL:
			setGeoTypeL3(attribute);
			break;
		case TEMPORAL:
			// covered in L1/L2
			break;
		case NUMERIC:
			setNumericTypeL3(attribute);
			break;
		case TEXTUAL:
			setTextualTypeL3(attribute);
			break;
		default:
			break;
		}
	}

	private static void setTextualTypeL3(Attribute attribute) {

		boolean hasEmails = true;
		boolean hasURLs = true;
		boolean oneValueAtLeast = false;

		for (AttributeValue<?> value : attribute.getStatistics().getAttributeValues()) {
			if (value.isNull())
				continue;

			oneValueAtLeast = true;
			String text = (String) value.getValue();

			if (!EmailValidator.getInstance(true, true).isValid(text))
				hasEmails = false;

			if (!UrlValidator.getInstance().isValid(text))
				hasURLs = false;

			if (!hasEmails && !hasURLs)
				break;
		}

		if (oneValueAtLeast && hasEmails)
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.EMAIL);
		else if (oneValueAtLeast && hasURLs)
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.URL);
		else if (attribute.getStatistics().isCategorical())
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.CATEGORICAL_TEXT);
		else
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.GENERIC_TEXT);
	}

	private static void setNumericTypeL3(Attribute attribute) {

		if (attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.INTEGER
				|| attribute.getStatistics().getAttributeStatisticsType() == AttributeStatisticsTypeL2.LONG) {

			List<Long> values = new ArrayList<Long>();
			for (AttributeValue<?> value : attribute.getStatistics().getAttributeValues()) {
				if (value.isNull() || value.isInvalid())
					continue;
				values.add(((Number) value.getValue()).longValue());
			}

			NumericTypeDetector ntd = new NumericTypeDetector(values);

			attribute.getStatistics().setAttributeStatisticsTypeL3(ntd.detectType());
		} else
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.ANY_NUMBER);

	}

	private static void setGeoTypeL3(Attribute attribute) {

		int dimensions[] = new int[] { 0, 0, 0 };
		for (AttributeValue<?> value : attribute.getStatistics().getAttributeValues()) {
			if (value.isNull() || value.isInvalid())
				continue;
			Geometry geometry = (Geometry) value.getValue();

			dimensions[geometry.getDimension()] = dimensions[geometry.getDimension()] + 1;
		}

		if (dimensions[0] > 0 && dimensions[1] == 0 && dimensions[2] == 0)
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.POINT);
		else if (dimensions[0] == 0 && dimensions[1] > 0 && dimensions[2] == 0)
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.LINESTRING);
		else if (dimensions[0] == 0 && dimensions[1] == 0 && dimensions[2] > 0)
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.POLYGON);
		else {
			// if more than two dimensions occur more than zero times: mixed geometries
			attribute.getStatistics().setAttributeStatisticsTypeL3(AttributeStatisticsTypeL3.MIXED_GEOMETRIES);
		}

	}

}
