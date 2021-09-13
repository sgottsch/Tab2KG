package de.l3s.simpleml.tab2kg.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.AttributeValue;
import de.l3s.simpleml.tab2kg.graph.simple.SimpleGraphProfilesCreator;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;

public class DomainGraphToTableConverter {

	public static void main(String[] args) {
		run(args[0], args[1], args[2]);
	}

	public static void run(String inputGraphFile, String outputFileName, String outputFileNameMapping) {

		File directory1 = new File(String.valueOf(outputFileName.substring(0, outputFileName.lastIndexOf("/"))));
		if (!directory1.exists())
			directory1.mkdir();

		File directory2 = new File(String.valueOf(outputFileNameMapping.substring(0, outputFileNameMapping.lastIndexOf("/"))));
		if (!directory2.exists())
			directory2.mkdir();

		SimpleGraph graph = new SimpleGraph(inputGraphFile);

		List<Integer> numbersOfQuantiles = Arrays.asList(4, 10);
		List<Integer> numbersOfIntervals = Arrays.asList(10);

		SimpleGraphProfilesCreator.createAttributeProfiles(graph, numbersOfQuantiles, numbersOfIntervals, false);

		List<List<String>> values = new ArrayList<List<String>>();

		JSONObject mappingJson = new JSONObject();
		mappingJson.put("name", outputFileName.substring(outputFileName.lastIndexOf("/") + 1));
		mappingJson.put("id", outputFileName.substring(outputFileName.lastIndexOf("/") + 1));

		mappingJson.put("graph", new JSONObject());
		JSONArray mappingArr = new JSONArray();
		mappingJson.getJSONObject("graph").put("nodes", mappingArr);

		List<String> columnTitles = new ArrayList<String>();

		for (Attribute attribute : graph.getAttributes()) {
			JSONObject mapJSON = new JSONObject();

			mappingArr.put(mapJSON);

			String columnName = getColumnName(attribute);
			mapJSON.put("columnName", columnName);
			columnTitles.add(columnName);

			JSONArray userArr = new JSONArray();
			JSONObject j = new JSONObject();
			userArr.put(j);

			mapJSON.put("userSemanticTypes", userArr);

			JSONObject domainJSON = new JSONObject();
			JSONObject typeJSON = new JSONObject();

			domainJSON.put("uri", attribute.getSubjectClassURI());

			typeJSON.put("uri", attribute.getPredicateURI());
			j.put("domain", domainJSON);
			j.put("type", typeJSON);
		}

		int columnNumber = 0;
		for (Attribute attribute : graph.getAttributes()) {
			System.out.println("Attribute: " + attribute.getPredicateURI());
			int rowNumber = 0;
			for (AttributeValue<?> value : attribute.getStatistics().getAttributeValues()) {

				if (values.size() < attribute.getStatistics().getAttributeValues().size()) {
					List<String> row = new ArrayList<String>();
					for (int i = 0; i < graph.getAttributes().size(); i++)
						row.add("");
					values.add(row);
				}

				if (value.isValidNonNull()) {
					String v = value.getOriginalValue();
					if (v.contains(","))
						v = "\"" + value.getOriginalValue() + "\"";
					values.get(rowNumber).set(columnNumber, v);
				}
				rowNumber += 1;
			}
			columnNumber += 1;
		}

		PrintWriter writer = null;
		OutputStream os = null;
		PrintWriter writerMapping = null;
		OutputStream osMapping = null;
		try {
			os = new FileOutputStream(outputFileName);
			writer = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
			writer.println(StringUtils.join(columnTitles, ","));

			for (List<String> row : values) {
				writer.println(StringUtils.join(row, ","));
			}

			osMapping = new FileOutputStream(outputFileNameMapping);
			writerMapping = new PrintWriter(new OutputStreamWriter(osMapping, "UTF-8"));
			writerMapping.println(mappingJson.toString());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			writer.close();
			writerMapping.close();
			try {
				os.close();
				osMapping.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static String getColumnName(Attribute attribute) {
		String columnName = attribute.getSubjectClassURI() + "---" + attribute.getPredicateURI();
		columnName = columnName.replace("http://", "");
		columnName = columnName.replace("https://", "");
		columnName = columnName.replace(",", "_");
		return columnName;
	}

}
