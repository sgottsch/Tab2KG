package de.l3s.simpleml.tab2kg.datareader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.AttributeValue;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL1;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.profiles.features.NumericProfileFeature;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeatureEnum;

public class EmbeddingsAdder {

	private static final String NULL_STRING = "-";
	static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);

	private static final String API_PORT = "5013";

	public static void extractValueFromTable(DataTable table) {

		JSONObject tableJSON = new JSONObject();
		JSONArray arr = new JSONArray();
		tableJSON.put("columns", arr);
		for (Attribute column : table.getAttributes()) {
			arr.put(extractValuesAsJSONArray(column.getIdentifier(), column.getStatistics().getAttributeValues(),
					column.getStatistics().getAttributeStatisticsType().getTypeL1(), null));
		}

		getResultViaPythonAPI(transformJSON(tableJSON), table.getAttributes());
	}

	public static void extractValueFromGraph(SimpleGraph graph) {
		
		JSONObject tableJSON = new JSONObject();
		JSONArray arr = new JSONArray();
		tableJSON.put("columns", arr);

		int maxLength = 0;
		for (Attribute attribute : graph.getAttributes()) {
			maxLength = Math.max(maxLength, attribute.getStatistics().getAttributeValues().size());
		}

		for (Attribute attribute : graph.getAttributes()) {

			String label1 = attribute.getSubjectClassURI().substring(attribute.getSubjectClassURI().lastIndexOf("/"))
					.replace("/", "");
			if (label1.contains("#"))
				label1 = label1.substring(label1.indexOf("#")).replaceFirst("#", "");

			String label2 = attribute.getPredicateURI().substring(attribute.getPredicateURI().lastIndexOf("/"))
					.replace("/", "");
			if (label2.contains("#"))
				label2 = label2.substring(label2.indexOf("#")).replaceFirst("#", "");

			String label = tokenise(label1 + " " + label2);

			arr.put(extractValuesAsJSONArray(label, attribute.getStatistics().getAttributeValues(),
					attribute.getStatistics().getAttributeStatisticsType().getTypeL1(), maxLength));
		}

		getResultViaPythonAPI(transformJSON(tableJSON), graph.getAttributes());
	}

	private static JSONObject transformJSON(JSONObject tableJSONOld) {

		JSONObject tableJSON = new JSONObject();
		JSONArray columnsArr = new JSONArray();
		tableJSON.put("columns", columnsArr);

		JSONArray rowsArr = new JSONArray();
		List<JSONArray> rows = new ArrayList<JSONArray>();
		tableJSON.put("rows", rowsArr);

		boolean firstColumn = true;
		for (int i = 0; i < tableJSONOld.getJSONArray("columns").length(); i++) {
			JSONObject column = new JSONObject();

			JSONObject oldColumn = tableJSONOld.getJSONArray("columns").getJSONObject(i);
			JSONArray valuesArr = oldColumn.getJSONArray("values");
			String sampleValue = null;
			for (int rowIndex = 0; rowIndex < valuesArr.length(); rowIndex++) {

				JSONArray rowArr = null;
				if (firstColumn) {
					rowArr = new JSONArray();
					rows.add(rowArr);
					rowsArr.put(rowArr);
				} else
					rowArr = rows.get(rowIndex);

				String value = valuesArr.getString(rowIndex);
				rowArr.put(value);

				if (sampleValue == null) {
					if (!valuesArr.getString(rowIndex).equals(NULL_STRING)) {
						sampleValue = value;
					}
				}
			}

			firstColumn = false;

			column.put("name", tokenise(oldColumn.getString("name")));
			column.put("type", oldColumn.getString("type"));
			column.put("sample_value", sampleValue);

			columnsArr.put(column);
		}

		return tableJSON;
	}

	private static String tokenise(String text) {
		return StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(text.replace("_", " ")), " ").trim()
				.replaceAll(" +", " ");
	}

	private static <T> JSONObject extractValuesAsJSONArray(String name, List<AttributeValue<T>> values,
			AttributeStatisticsTypeL1 type, Integer maxLength) {

		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();

		obj.put("values", arr);

		if (name != null && !name.isEmpty())
			obj.put("name", name);
		else
			obj.put("name", NULL_STRING);

		switch (type) {
		case NUMERIC:
			obj.put("type", "real");
			for (AttributeValue<T> value : values) {
				if (value.isNullOrInvalid())
					arr.put(NULL_STRING);
				else
					arr.put(String.valueOf(value.getValue()));
			}
			break;
		case TEXTUAL:
			obj.put("type", "text");
			for (AttributeValue<T> value : values) {
				if (value.isNullOrInvalid() || value.getStringValue() == null || value.getStringValue().isEmpty())
					arr.put(NULL_STRING);
				else
					arr.put(tokenise(value.getStringValue()));
			}
			break;
		case SPATIAL:
			obj.put("type", "text");
			for (AttributeValue<T> value : values) {
				if (value.isNullOrInvalid() || value.getStringValue() == null || value.getStringValue().isEmpty())
					arr.put(NULL_STRING);
				else
					arr.put(value.getStringValue());
			}
			break;
		case TEMPORAL:
			obj.put("type", "text");
			for (AttributeValue<T> value : values) {
				if (value.isNullOrInvalid() || value.getStringValue() == null || value.getStringValue().isEmpty())
					arr.put(NULL_STRING);
				else
					arr.put(simpleDateFormat.format((Date) value.getValue()));
			}
			break;
		case BOOLEAN:
			obj.put("type", "text");
			for (AttributeValue<T> value : values) {
				if (value.isNullOrInvalid() || value.getStringValue() == null || value.getStringValue().isEmpty())
					arr.put(NULL_STRING);
				else if ((Boolean) value.getValue())
					arr.put("true");
				else
					arr.put("false");
			}
			break;

		default:
			break;
		}

		if (maxLength != null) {
			while (arr.length() < maxLength) {
				int randomIndex = getRandomNumber(0, arr.length() - 1);
				arr.put(arr.get(randomIndex));
			}
		}
		
		System.out.println(name + " => "+arr);

		return obj;
	}

	private static int getRandomNumber(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	private static void getResultViaPythonAPI(JSONObject inputJson, List<Attribute> attributes) {

		CloseableHttpClient httpclient = HttpClients.createDefault();

		// List<List<Double>> values = new ArrayList<List<Double>>();

		HttpPost httpPost = new HttpPost("http://127.0.0.1:" + API_PORT + "/embed");
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("input", inputJson.toString()));
		CloseableHttpResponse response2 = null;
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
			response2 = httpclient.execute(httpPost);

			HttpEntity entity2 = response2.getEntity();
			// do something useful with the response body
			// and ensure it is fully consumed
			// EntityUtils.consume(entity2);

			BufferedReader in = new BufferedReader(new InputStreamReader(entity2.getContent()));
			JSONObject res = new JSONObject(in.readLine());

			JSONArray arr = new JSONArray(res.getString("result"));

			for (int i = 0; i < arr.length(); i++) {
				
				
				JSONArray columnJson = arr.getJSONArray(i);
				// List<Double> columnValues = new ArrayList<Double>();
				// values.add(columnValues);

				for (int j = 0; j < columnJson.length(); j++) {
				// columnValues.add(columnJson.getDouble(j));
					attributes.get(i).getStatistics().addNumericProfileFeature(new NumericProfileFeature<Double>(
							ProfileFeatureEnum.EMBEDDING, DataTypeClass.XS_DOUBLE, columnJson.getDouble(j), j));
				}
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				response2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
