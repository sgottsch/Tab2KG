package de.l3s.simpleml.tab2kg.data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import de.l3s.simpleml.tab2kg.rml.ColumnLiteralMapping;
import de.l3s.simpleml.tab2kg.rml.RMLMappingReader;

public class ModelFilesCreator {

	public static boolean createModelFile(String tableFileName, String mappingFileName, String outputFileName) {

		JSONObject json = new JSONObject();
		json.put("id", tableFileName);
		json.put("name", tableFileName);

		// JSONArray columnsJSON = new JSONArray();
		// json.put("sourceColumns", columnsJSON);

		JSONObject graphJSON = new JSONObject();
		json.put("graph", graphJSON);

		JSONArray nodesJSON = new JSONArray();
		graphJSON.put("nodes", nodesJSON);

		// JSONArray linksJSON = new JSONArray();
		// graphJSON.put("links", linksJSON);

		try {
			RMLMappingReader rmr = new RMLMappingReader();
			List<ColumnLiteralMapping> mappings = rmr.getMappings(mappingFileName);
			if (mappings == null)
				return false;

			for (ColumnLiteralMapping mapping : mappings) {
				JSONObject nodeJSON = new JSONObject();
				nodesJSON.put(nodeJSON);
				nodeJSON.put("columnName", mapping.getColumnId());

				JSONArray semTypeJSONArr = new JSONArray();
				JSONObject semTypeJSON = new JSONObject();

				nodeJSON.put("userSemanticTypes", semTypeJSONArr);
				semTypeJSONArr.put(semTypeJSON);

				JSONObject domainJSON = new JSONObject();
				semTypeJSON.put("domain", domainJSON);
				domainJSON.put("uri", mapping.getSubjectClass().getURI());

				JSONObject typeJSON = new JSONObject();
				semTypeJSON.put("type", typeJSON);
				typeJSON.put("uri", mapping.getProperty().getURI());
			}

			RMLMappingReader rmr2 = new RMLMappingReader();
			mappings = rmr2.getMappings(mappingFileName);
		} catch (IOException e1) {
			System.out.println("Could not create mapping: " + tableFileName + ": " + e1.getMessage());
			return false;
		}

		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonParser jp = new JsonParser();
			JsonElement je = jp.parse(json.toString());
			String prettyJsonString = gson.toJson(je);

			FileUtils.write(new File(outputFileName), prettyJsonString, Charset.forName("UTF-8"));
		} catch (IOException e) {
			System.out.println("Could not create JSON for mapping: " + tableFileName + ": " + e.getMessage());
			return false;
		}

		return true;
	}

}
