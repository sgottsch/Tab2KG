package de.l3s.simpleml.tab2kg.ml.baseline;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.evaluation.EvaluationInstance;
import de.l3s.simpleml.tab2kg.util.Source;

public class DSLConfidencesLoader {

	private Map<String, Double> confidences = new HashMap<String, Double>();

	public void loadPairs(String pairsFileName, String dslOutputFolder, Source dataSet,
			List<EvaluationInstance> evaluationInstances, boolean useDomainGraphs) {

		if (!dslOutputFolder.endsWith("/"))
			dslOutputFolder = dslOutputFolder + "/";

		System.out.println("loadPairs: " + pairsFileName);
		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(pairsFileName), "UTF-8");

			String suffixPD = "p";
			if (useDomainGraphs)
				suffixPD = "d";

			while (it.hasNext()) {
				String line = it.nextLine();

				if (line.startsWith("#"))
					continue;

				System.out.println(line);

				String[] parts = line.split(" ");
				String id = dataSet.getName() + "_" + suffixPD + "_" + parts[0];

				String graphFile = parts[5];
				String tableFile = parts[6];

				String jsonFileName = dslOutputFolder + id + "_result.json";

				Map<String, Map<String, Double>> confidences = parseResultJSON(jsonFileName);

//				// map 2nd column name to attribute
//				RMLMappingReader rmr = new RMLMappingReader();
//				String suffix = ".rml";
//				if (dataSet == Source.SOCCER || dataSet == Source.WEAPONS)
//					suffix = ".csv.rml";
//
//				String folder = dataSet.getFolderName();
//				if (dataSet == Source.SEMTAB_EASY)
//					folder = Source.SEMTAB.getFolderName();
//
//				Map<String, String> columnToAttribute = new HashMap<String, String>();
//				List<ColumnLiteralMapping> columnMappings = rmr.getMappings(Config.getPath(FileLocation.BASE_FOLDER)
//						+ folder + SemTabTableCreator.FOLDER_NAME_MAPPINGS + "/" + graphFile + suffix);
//				for (ColumnLiteralMapping cc : columnMappings) {
//					columnToAttribute.put(cc.getColumnId().replace(" ", ""),
//							cc.getSubjectClass().getURI() + " " + cc.getProperty().getURI());
//				}

				for (String columnId : confidences.keySet()) {
					for (String attributeKey : confidences.get(columnId).keySet()) {
						String key = createKey(graphFile, tableFile, columnId, attributeKey.toLowerCase());// .get(attributeKey));
						double confidence = confidences.get(columnId).get(attributeKey);

						// System.out.println("Put " + key + " -> " + confidence);

						this.confidences.put(key, confidence);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (it != null)
				try {
					it.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

	public Double getConfidence(String fileName1, String fileName2, Attribute column, Attribute attribute) {

		String attributeKey = attribute.getSubjectClassURI()
				.substring(attribute.getSubjectClassURI().lastIndexOf("/") + 1) + " "
				+ attribute.getPredicateURI().substring(attribute.getPredicateURI().lastIndexOf("/") + 1);

//		System.out.println(fileName1 + " / " + fileName2);
//		System.out.println(" Column: " + column.getIdentifier());
//		System.out.println(" Attribute: " + attribute.getPredicateURI());

		String key = createKey(clearFileName(fileName1), clearFileName(fileName2),
				column.getIdentifier().replace(" ", ""), attributeKey);

//		System.out.println("Query key: " + key);
//		System.out.println("Confidence: " + this.confidences.get(key));

		return this.confidences.get(key);
	}

	private String createKey(String fileName1, String fileName2, String columnId, String attributeKey) {
		return (clearFileName(fileName1) + "@" + clearFileName(fileName2) + "@" + columnId + "@" + attributeKey)
				.toLowerCase();
	}

	private Map<String, Map<String, Double>> parseResultJSON(String jsonFileName) {

		Map<String, Map<String, Double>> confidences = new HashMap<String, Map<String, Double>>();

		JSONObject json;
		try {
			json = new JSONObject(FileUtils.readFileToString(new File(jsonFileName), "UTF-8"));
			for (String file : json.keySet()) {
				JSONObject jsonOfFile = json.getJSONObject(file);

				for (String columnName : jsonOfFile.keySet()) {

					confidences.put(columnName, new HashMap<String, Double>());

					JSONArray columnJSONArr = jsonOfFile.getJSONArray(columnName);

					for (int j = 0; j < columnJSONArr.length(); j++) {

						JSONArray arr = columnJSONArr.getJSONArray(j);

						double confidence = arr.getDouble(0);
						JSONArray attributesJSONArr = arr.getJSONArray(1);
						for (int i = 0; i < attributesJSONArr.length(); i++) {
							String attribute = attributesJSONArr.getString(i).replace("---", " ");

							confidences.get(columnName).put(attribute, confidence);
						}
					}
				}
			}
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}

		return confidences;
	}

	public static String clearFileName(String fileName) {

		fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		fileName = fileName.replace("split2_graph", "file.csv");
		fileName = fileName.replaceAll("file$", "file.csv");
		fileName = fileName.replaceAll("file.csv.ttl$", "file.csv");
		fileName = fileName.replaceAll(".ttl$", "");
		fileName = fileName.replaceAll(".csv$", "");

		return fileName;
	}

}
