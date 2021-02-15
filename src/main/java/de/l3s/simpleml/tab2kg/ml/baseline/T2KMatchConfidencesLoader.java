package de.l3s.simpleml.tab2kg.ml.baseline;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.util.Source;

public class T2KMatchConfidencesLoader {

	private Map<String, Double> confidences = new HashMap<String, Double>();

	public void loadPairs(String classesFileName, String propertiesFileName, Source dataSet) {
		Map<String, List<String>> predictedClasses = new HashMap<String, List<String>>();

		Reader in = null;
		try {
			in = new FileReader(classesFileName.replace("\"", ""));

			CSVFormat format = CSVFormat.RFC4180.withIgnoreEmptyLines();

			Iterable<CSVRecord> records = format.parse(in);

			for (CSVRecord record : records) {
				String fileName = record.get(1);
				List<String> classes = new ArrayList<String>();
				for (String goalClass : record.get(2).split(","))
					classes.add(goalClass.trim());
				predictedClasses.put(fileName, classes);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Reader in2 = null;
		try {
			in2 = new FileReader(propertiesFileName.replace("\"", ""));

			CSVFormat format = CSVFormat.RFC4180.withIgnoreEmptyLines();

			Iterable<CSVRecord> records = format.parse(in2);

			for (CSVRecord record : records) {
				String fileName = record.get(0).substring(0, record.get(0).lastIndexOf("~"));
				int columnNumber = Integer
						.valueOf(record.get(0).substring(record.get(0).lastIndexOf("~") + 1).replace("Col", "")) - 1;
				double confidence = Double.valueOf(record.get(2));
				List<String> classes = predictedClasses.get(fileName);

				for (String predictedClass : classes) {

					confidences.put(createKey(fileName, columnNumber, predictedClass + " " + record.get(1)),
							confidence);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Double getConfidence(String fileName1, Attribute column, Attribute attribute) {

		String attributeKey = attribute.getSubjectClassURI()
				.substring(attribute.getSubjectClassURI().lastIndexOf("/") + 1);
		attributeKey += " " + attribute.getPredicateURI();

		String key = createKey(clearFileName(fileName1), column.getColumnIndex(), attributeKey);

		return this.confidences.get(key);
	}

	private String createKey(String fileName1, int columnNumber, String attributeKey) {
		return fileName1 + " " + columnNumber + " " + attributeKey;
	}

	public static String clearFileName(String fileName) {

		fileName = fileName.substring(fileName.lastIndexOf("/") + 1);

		fileName = fileName.replace("split2_graph", "file.csv");
		fileName = fileName.replaceAll("file$", "file.csv");
		fileName = fileName.replaceAll("file.csv.ttl$", "file.csv");

		// fileName = fileName.substring(fileName.lastIndexOf("/") + 1);

//		if (fileName.endsWith(".csv.model.json"))
//			return fileName.replaceAll(".csv.model.json$", "");
//		if (fileName.endsWith(".csv.ttl"))
//			return fileName.replaceAll(".csv.ttl$", "");
//		if (fileName.endsWith(".ttl"))
//			return fileName.replaceAll(".ttl$", "");
//		if (fileName.endsWith(".csv"))
//			return fileName.replaceAll(".csv$", "");
//		else
		return fileName;

	}

}
