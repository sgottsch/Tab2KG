package de.l3s.simpleml.tab2kg.evaluation.baselines.t2kmatch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.Row;
import de.l3s.simpleml.tab2kg.data.PairsLoader;
import de.l3s.simpleml.tab2kg.datareader.DataTableReader;
import de.l3s.simpleml.tab2kg.evaluation.EvaluationInstance;
import de.l3s.simpleml.tab2kg.rml.ColumnLiteralMapping;
import de.l3s.simpleml.tab2kg.rml.RMLMappingReader;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Source;

public class T2KMatchInputDataTransformer {

	public static void main(String[] args) {

		Source source = Source.SEMTAB_EASY;

		String outputFolder = Config.getPath(FileLocation.BASE_FOLDER) + Source.SEMTAB_EASY.getFolderName();
		String outputTablesFolder = Config.getPath(FileLocation.BASE_FOLDER) + Source.SEMTAB_EASY.getFolderName()+"tables/";

		String gsClassFile = outputFolder + "gs_class.csv";
		String gsPropertyFile = outputFolder + "gs_property.csv";

		PrintWriter writerGSInstance = null;
		PrintWriter writerGSClass = null;
		PrintWriter writerGSProperty = null;

		try {
			// writerGSInstance = new PrintWriter(gsInstanceFile);
			writerGSClass = new PrintWriter(gsClassFile);
			writerGSProperty = new PrintWriter(gsPropertyFile);
			List<EvaluationInstance> pairs = PairsLoader.loadPairs(source);
			Set<String> doneTableFiles = new HashSet<String>();
			for (EvaluationInstance pair : pairs) {
				if (!doneTableFiles.contains(pair.getTableFileName())) {
					processPair(pair, writerGSInstance, writerGSClass, writerGSProperty, outputTablesFolder);
					doneTableFiles.add(pair.getTableFileName());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			// writerGSInstance.close();
			writerGSClass.close();
			writerGSProperty.close();
		}

	}

	private static void processPair(EvaluationInstance pair, PrintWriter writerGSInstance, PrintWriter writerGSClass,
			PrintWriter writerGSProperty, String dataFolder) {

		String tableFilePath = pair.getTableFileName();
		String tableFileName = tableFilePath.substring(tableFilePath.lastIndexOf("/") + 1);
		String outputTableFileName = dataFolder + tableFileName;

		convertTableFile(tableFilePath, outputTableFileName);
		addToGroundTruthFiles(tableFileName, pair.getMappingFileName(), writerGSClass, writerGSProperty);
	}

	private static void convertTableFile(String tableFileName, String outputTableFileName) {

		DataTable table = DataTableReader.readDataTable(tableFileName, ",", true, false);

		PrintWriter writer = null;
		System.out.println("outputTableFileName: " + outputTableFileName);

		try {
			writer = new PrintWriter(outputTableFileName);
			int rank = 1;

			List<String> headerValues = new ArrayList<String>();
			headerValues.add("\"rank\"");
			for (String value : table.getHeaderRow().getValues()) {
				headerValues.add("\"" + value + "\"");
			}
			String headerLine = StringUtils.join(headerValues, ",");
			writer.println(headerLine);

			for (Row row : table.getRows()) {
				List<String> values = new ArrayList<String>();
				values.add("\"" + String.valueOf(rank) + "\"");
				for (String value : row.getValues())
					values.add("\"" + value.replace("\"", "") + "\"");
				String line = StringUtils.join(values, ",");
				writer.println(line);
				rank += 1;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}

	private static void addToGroundTruthFiles(String tableFilaName, String mappingFile, PrintWriter writerGSClass,
			PrintWriter writerGSProperty) {

		List<ColumnLiteralMapping> mappings1;
		try {
			RMLMappingReader rmr = new RMLMappingReader();
			mappings1 = rmr.getMappings(mappingFile);

			for (ColumnLiteralMapping clm : mappings1) {
				String targetClass = clm.getSubjectClass().getURI().replace("http://dbpedia.org/ontology/", "");
				writerGSClass.println(tableFilaName + "," + targetClass + ",true");
				break;
			}

			for (ColumnLiteralMapping clm : mappings1) {
				String column = clm.getColumnId().replace("col", "Col");
				writerGSProperty.println(tableFilaName + "~" + column + "," + clm.getProperty().getURI() + ",true");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
