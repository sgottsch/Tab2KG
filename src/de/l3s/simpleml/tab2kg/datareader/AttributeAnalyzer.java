package de.l3s.simpleml.tab2kg.datareader;

import java.text.SimpleDateFormat;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.SMLWKB;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.SMLWKT;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSBoolean;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSDataType;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSDate;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSDateTime;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSDouble;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSInteger;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSLong;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSNegativeInteger;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSNonNegativeInteger;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSNonPositiveInteger;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSString;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.XSTime;
import de.l3s.simpleml.tab2kg.util.time.dateparser.DateParserUtils;
import de.l3s.simpleml.tab2kg.util.time.dateparser.MyDateTime;

public class AttributeAnalyzer {

	// https://www.data2type.de/xml-xslt-xslfo/xml-schema/datentypen-referenz/

	public static final int MAX_VALUES_IF_CATEGORICAL = 20;

	private List<XSDataType> xsDataTypes = new ArrayList<XSDataType>();

	private double threshold = 0.9d;

	private boolean isCategorical;
	private boolean isIdentifier;

	private String attributeIdentifier;

	public static void main(String[] args) {

		Date date1 = DateParserUtils.parseDate("1984-5-5");
		Date date2 = DateParserUtils.parseDate("12:33:35");
		Date date3 = DateParserUtils.parseDate("1984-5-5 12:33:35");

		System.out.println(new SimpleDateFormat("yyyy.dd.MM").format(date2));

		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy.dd.MM");
		boolean hasTime1 = !"12:00:00".equals(dateFormat.format(date1));
		boolean hasDate1 = !"0001.01.01".equals(timeFormat.format(date1));
		System.out.println(hasTime1 + " " + hasDate1);
		boolean hasTime2 = !"12:00:00".equals(dateFormat.format(date2));
		boolean hasDate2 = !"0001.01.01".equals(timeFormat.format(date2));
		System.out.println(hasTime2 + " " + hasDate2);
		boolean hasTime3 = !"12:00:00".equals(dateFormat.format(date3));
		boolean hasDate3 = !"0001.01.01".equals(timeFormat.format(date3));
		System.out.println(hasTime3 + " " + hasDate3);

		testCase1();

		testCase2();

		testCase3();

		testCase4();

		testCase5();

		testCase6();

		testCase7();

		testCase8();

		testCase9();
	}

	private static void testCase9() {
		AttributeAnalyzer ca = new AttributeAnalyzer();
		ca.init();

		Attribute attribute = new Attribute();
		attribute.addValue("12345678910");

		System.out.println(ca.detectDataType(attribute));
	}

	private static void testCase7() {
		Attribute attribute = new Attribute();
		attribute.setActive(true);

//		attribute.addValue("POINT(4 6)");
//		attribute.addValue("POINT(2 7)");
		attribute.addValue("POINT(3 4)");
		attribute.addValue("LINESTRING(4 6,7 10))");
		attribute.addValue("LINESTRING(4 5,7 11))");
		attribute.addValue("LINESTRING(4 8,7 9))");

		DataTable table = new DataTable();
		table.addAttribute(attribute);
		DataTableProfilesCreator.createColumnProfiles(table, Arrays.asList(4), Arrays.asList(10));

		System.out.println(attribute.getStatistics().getAttributeStatisticsTypeL3());
	}

	private static void testCase8() {
		System.out.println("=== testCase8 ===");

		Attribute attribute = new Attribute();
		attribute.setActive(true);

		attribute.addValue("1965-12-12");

		DataTable table = new DataTable();
		table.addAttribute(attribute);
		DataTableProfilesCreator.createColumnProfiles(table, Arrays.asList(4), Arrays.asList(10));

		System.out.println(attribute.getStatistics().getAttributeStatisticsTypeL3());
		System.out.println(attribute.getStatistics().getAttributeStatisticsType());
	}

	public AttributeAnalyzer() {
		super();
		init();
	}

	private static void testCase1() {
		AttributeAnalyzer ca = new AttributeAnalyzer();
		ca.init();

		System.out.println("Values 1");

		Attribute attribute = new Attribute();
		attribute.addValue("2019.4");
		attribute.addValue("123");
		attribute.addValue("0");
		attribute.addValue("abc");
		attribute.addValue("-123");
		attribute.addValue("4");
		attribute.addValue("true");
		attribute.addValue("false");
		attribute.addValue("fals");
		attribute.addValue("0.1");
		attribute.addValue("-0.2");
		attribute.addValue("0.0346");
		attribute.addValue("12.948");
		attribute.addValue("12.948-");
		attribute.addValue("000000000140000000000000004010000000000000");
		attribute.addValue("130382fa034647f18837399faa9acd5c");
		attribute.addValue("40be67a4127a9314c9687c83a7091a34d32c0d1ffd7bfc150ad0302bf6e06845");
		System.out.println(ca.detectDataType(attribute));
	}

	private static void testCase2() {
		AttributeAnalyzer ca = new AttributeAnalyzer();
		ca.init();

		System.out.println("Values 2");

		Attribute attribute = new Attribute();
		attribute.addValue("-5");
		attribute.addValue("0");
		attribute.addValue("2");

		System.out.println(ca.detectDataType(attribute));
	}

	private static void testCase3() {
		AttributeAnalyzer ca = new AttributeAnalyzer();
		ca.init();

		System.out.println("Values 3");

		Attribute attribute = new Attribute();
		// attribute.addValue("2001-10-26T21:32:52");
		attribute.addValue("2001-10-26");

		// values.add("2001-10-26+02:00");
		// values.add("2001-10-26Z");
		// values.add("2001-10-26+00:00");
		// values.add("-2001-10-26");
		// values.add("-20000-04-01");
		System.out.println(ca.detectDataType(attribute));
	}

	private static void testCase4() {
		AttributeAnalyzer ca = new AttributeAnalyzer();
		ca.init();

		System.out.println("Values 4");

		Attribute attribute = new Attribute();
		attribute.addValue("2019.4");
		attribute.addValue("3.0E-8");
		attribute.addValue("3e-8");
		System.out.println(ca.detectDataType(attribute));
	}

	private static void testCase5() {
		AttributeAnalyzer ca = new AttributeAnalyzer();
		ca.init();

		Attribute attribute = new Attribute();
		for (int i = 0; i < 50; i++) {
			if (i == 15 || i == 38)
				attribute.addValue("xx");
			else
				attribute.addValue(String.valueOf(i));
		}
		System.out.println(ca.detectDataType(attribute));
	}

	private static void testCase6() {
		AttributeAnalyzer ca = new AttributeAnalyzer();
		ca.init();

		Attribute attribute = new Attribute();
		for (int i = 0; i < 50; i++) {
			if (i <= 10)
				attribute.addValue(" ");
			else
				attribute.addValue(String.valueOf(i));
		}
		System.out.println(ca.detectDataType(attribute));
	}

	public String getAttributeIdentifier() {
		return attributeIdentifier;
	}

	public void setAttributeIdentifier(String attributeIdentifier) {
		this.attributeIdentifier = attributeIdentifier;
	}

	public DataTypeClass detectDataType(Attribute attribute) {

		Map<DataTypeClass, Integer> dataTypeCounts = new HashMap<DataTypeClass, Integer>();
		for (XSDataType dataType : this.xsDataTypes)
			dataTypeCounts.put(dataType.getRDFClass(), 0);

		Set<String> uniqueValues = new HashSet<String>();
		int numberOfNonNullValues = 0;
		for (String value : attribute.getValues()) {

			if (value == null)
				continue;

			numberOfNonNullValues += 1;

			uniqueValues.add(value);
			for (DataTypeClass rdfClass : getGenericDataTypes(value)) {
				// if (!dataTypeCounts.containsKey(rdfClass))
				// dataTypeCounts.put(rdfClass, 1);
				// else
				dataTypeCounts.put(rdfClass, dataTypeCounts.get(rdfClass) + 1);
			}
		}

//		System.out.println("\n\n");
//		System.out.println("Values: " + uniqueValues);
//		for (DataTypeClass rdfClass : dataTypeCounts.keySet()) {
//			System.out.println(rdfClass.getName() + " -> " + dataTypeCounts.get(rdfClass));
//		}

		int numberOfValues = attribute.getValues().size();
		int numberOfUniqueValues = uniqueValues.size();

		// find highest coverage
		Integer highestCoverage = null;
		for (DataTypeClass rdfClass : dataTypeCounts.keySet()) {
			if (highestCoverage == null || dataTypeCounts.get(rdfClass) > highestCoverage)
				highestCoverage = dataTypeCounts.get(rdfClass);
		}

		DataTypeClass targetDataType = null;
		for (XSDataType dataType : this.xsDataTypes) {
			int coverage = dataTypeCounts.get(dataType.getRDFClass());
			if (coverage < highestCoverage)
				continue;
			double relativeCoverage = (double) dataTypeCounts.get(dataType.getRDFClass()) / numberOfNonNullValues;
			if (relativeCoverage >= this.threshold) {
				targetDataType = dataType.getRDFClass();
				break;
			}
		}

		if (targetDataType == null)
			targetDataType = new XSString().getRDFClass();

		if (isCategorical(numberOfValues, numberOfUniqueValues))
			this.isCategorical = true;

		attribute.setDataTypeClass(targetDataType);

		return targetDataType;
	}

	private void init() {

		// get less restrict in order

		this.xsDataTypes.add(new SMLWKT());
		this.xsDataTypes.add(new SMLWKB());

		this.xsDataTypes.add(new XSDateTime());
		this.xsDataTypes.add(new XSDate());
		this.xsDataTypes.add(new XSTime());

		this.xsDataTypes.add(new XSBoolean());

		this.xsDataTypes.add(new XSNonPositiveInteger());
		this.xsDataTypes.add(new XSNegativeInteger());
		this.xsDataTypes.add(new XSNonNegativeInteger());

		this.xsDataTypes.add(new XSInteger());
		this.xsDataTypes.add(new XSLong());

		this.xsDataTypes.add(new XSDouble());

		// this.xsDataTypes.add(new XSString());
	}

	private Set<DataTypeClass> getGenericDataTypes(String value) {

		Set<DataTypeClass> dataTypes = new HashSet<DataTypeClass>();

		for (XSDataType dataType : this.xsDataTypes) {
			if (dataType.isTemporal()) {
				// treat temporal data types differently, so we only need to parse them once
				continue;
			}
			if (dataType.matches(value)) {
				dataTypes.add(dataType.getRDFClass());
			}
		}

		try {
			MyDateTime dateTime = DateParserUtils.parseMyDateTime(value);
			if (dateTime.hasDate() && dateTime.hasTime())
				dataTypes.add((new XSDateTime()).getRDFClass());
			else if (dateTime.hasDate() && !dateTime.hasTime())
				dataTypes.add((new XSDate()).getRDFClass());
			else if (!dateTime.hasDate() && dateTime.hasTime())
				dataTypes.add((new XSTime()).getRDFClass());
		} catch (DateTimeParseException e) {
		}

		return dataTypes;
	}

	public boolean isCategorical() {
		return isCategorical;
	}

	public void setCategorical(boolean isCategorical) {
		this.isCategorical = isCategorical;
	}

	public boolean isIdentifier() {
		return isIdentifier;
	}

	public static boolean isCategorical(int numberOfValues, int numberOfDistinctValues) {
		return numberOfDistinctValues <= AttributeAnalyzer.MAX_VALUES_IF_CATEGORICAL
				&& Math.sqrt(numberOfValues) > numberOfDistinctValues && numberOfDistinctValues > 1;
	}

}
