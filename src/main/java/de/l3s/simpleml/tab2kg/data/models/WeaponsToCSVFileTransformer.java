package de.l3s.simpleml.tab2kg.data.models;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import de.l3s.simpleml.tab2kg.graph.DataTableFromInputGraphCreator;

public class WeaponsToCSVFileTransformer {

	public static void transform(File file, String outputFileName, HashSet<String> usedColumnNames) {
		// collect attributes / column names

		Set<Integer> relevantColumns = new HashSet<Integer>();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(outputFileName);
			CSVPrinter csvPrinter = null;
			try {

				List<String> lines = FileUtils.readLines(file, "UTF-8");

				List<String> columnTitles = new ArrayList<String>();
				columnTitles.add(DataTableFromInputGraphCreator.ROW_NUMBER);

				JSONObject jsonFirstLine = new JSONObject(lines.get(0));
				int i = 0;
				for (Iterator<String> it = jsonFirstLine.keys(); it.hasNext();) {
					String columnTitle = it.next();
					if (usedColumnNames.contains(columnTitle)) {
						columnTitles.add(columnTitle);
						relevantColumns.add(i);
					}
					i += 1;
				}

				String[] header = new String[columnTitles.size()];
				i = 0;
				for (String columnTitle : columnTitles) {
					header[i] = columnTitle;
					i += 1;
				}

				csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));

				int rowNumber = 0;
				for (String line : FileUtils.readLines(file, "UTF-8")) {
					List<String> lineValues = new ArrayList<String>();
					lineValues.add(String.valueOf(rowNumber));
					JSONObject jsonLine = new JSONObject(line);
					i = 0;
					for (Iterator<String> it = jsonLine.keys(); it.hasNext();) {
						String key = it.next();
						if (relevantColumns.contains(i))
							lineValues.add(jsonLine.getString(key));
						i += 1;
					}
					csvPrinter.printRecord(lineValues);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					csvPrinter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}

}
