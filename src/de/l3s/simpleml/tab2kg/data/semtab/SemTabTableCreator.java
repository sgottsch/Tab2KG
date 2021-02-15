package de.l3s.simpleml.tab2kg.data.semtab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.Row;
import de.l3s.simpleml.tab2kg.data.ModelFilesCreator;
import de.l3s.simpleml.tab2kg.datareader.DataTableReader;
import de.l3s.simpleml.tab2kg.graph.DataTableFromInputGraphCreator;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClass;
import de.l3s.simpleml.tab2kg.rml.RMLMappingCreator;
import de.l3s.simpleml.tab2kg.rml.RMLMappingExecutor;
import de.l3s.simpleml.tab2kg.rml.model.RMLLiteralPredicateObjectMap;
import de.l3s.simpleml.tab2kg.rml.model.RMLMapping;
import de.l3s.simpleml.tab2kg.rml.model.RMLPredicateObjectMap;
import de.l3s.simpleml.tab2kg.rml.model.RMLSubjectMap;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Source;

public class SemTabTableCreator {

	public static final String FOLDER_NAME_TABLES = "tables";
	public static final String FOLDER_NAME_GRAPHS = "graphs";
	public static final String FOLDER_NAME_MAPPINGS = "mappings";
	public static final String FOLDER_NAME_MODELS = "models";
	public static final String FILE_NAME_PAIRS = "pairs.tsv";
	public static final String FILE_NAME_PAIRS_TRAINING = "pairs_training.tsv";
	public static final String FILE_NAME_PAIRS_TEST = "pairs_test.tsv";

	private String tablesFolder;
	private String cleanedTablesFolder;
	private String ctaGroundTruthFile;
	private String cpaGroundTruthFile;

	private Map<String, DataTable> dataTables = new HashMap<String, DataTable>();
	private Map<DataTable, Set<Integer>> relevantColumnNumbers = new HashMap<DataTable, Set<Integer>>();
	private Map<DataTable, Set<CSVRecord>> ctaRecords = new HashMap<DataTable, Set<CSVRecord>>();
	private Map<DataTable, Set<CSVRecord>> cpaRecords = new HashMap<DataTable, Set<CSVRecord>>();
	private Map<DataTable, Map<Integer, String>> allColumnTitles = new HashMap<DataTable, Map<Integer, String>>();
	private String datasetFolder;

	public static void main(String[] args) {

		String sourceFolder = args[0];
		if (!sourceFolder.endsWith("/"))
			sourceFolder = sourceFolder + "/";

		String semtabFolder = Config.getPath(FileLocation.BASE_FOLDER) + Source.SEMTAB.getFolderName();

		try {
			Files.createDirectories(Paths.get(semtabFolder + FOLDER_NAME_TABLES));
			Files.createDirectories(Paths.get(semtabFolder + FOLDER_NAME_GRAPHS));
			Files.createDirectories(Paths.get(semtabFolder + FOLDER_NAME_MAPPINGS));
			Files.createDirectories(Paths.get(semtabFolder + FOLDER_NAME_MODELS));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int round = 1; round <= 4; round++) {

			System.out.println("=== ROUND " + round + " ===");

			String cleanedTablesFolder = semtabFolder + FOLDER_NAME_TABLES + "/";
			String tablesFolder = sourceFolder + "Round " + round + "/tables/";

			String ctaGroundTruthFile = sourceFolder + "Round " + round + "/gt/CTA_Round" + round + "_gt.csv";
			String cpaGroundTruthFile = sourceFolder + "Round " + round + "/gt/CPA_Round" + round + "_gt.csv";

			SemTabTableCreator semTabTableCreator = new SemTabTableCreator(semtabFolder, tablesFolder,
					ctaGroundTruthFile, cpaGroundTruthFile, cleanedTablesFolder);
			semTabTableCreator.loadRelevantColumns();
			semTabTableCreator.createCleanedTables(round);
			semTabTableCreator.createRMLMappings(round);
		}
	}

	private void deleteCleanedTables(DataTable table) {
		(new File(table.getFileName())).delete();
	}

	private void createRMLMappings(int round) {

		for (String tableId : this.dataTables.keySet()) {

			DataTable table = this.dataTables.get(tableId);

			if (table == null)
				continue;

			String tableFileName = "../" + FOLDER_NAME_TABLES + "/"
					+ table.getFileName().substring(table.getFileName().lastIndexOf("/") + 1);
			System.out.println("---" + tableFileName + " ---");

			RMLMapping rmlMapping = createRMLMapping(table);

			if (rmlMapping == null) {
				deleteCleanedTables(table);
				continue;
			}

			rmlMapping.setSourceFileName(tableFileName);
			rmlMapping.setDelimiter(",");

			String mappingFileName = this.datasetFolder + FOLDER_NAME_MAPPINGS + "/" + round + "_" + tableId + ".rml";
			RMLMappingCreator.createMappingString(rmlMapping, mappingFileName);

			RMLMappingExecutor exec = new RMLMappingExecutor(mappingFileName);

			String graphFileName = this.datasetFolder + FOLDER_NAME_GRAPHS + "/" + round + "_" + tableId + ".ttl";

			exec.run(graphFileName, null);

			String modelFileName = this.datasetFolder + FOLDER_NAME_MODELS + "/" + round + "_" + tableId
					+ ".csv.model.json";

			if (!ModelFilesCreator.createModelFile(round + "_" + table.getFileName(), mappingFileName, modelFileName)) {
				// RML file creation failed. Delete files
				System.out.println("Delete " + table.getFileName());
				try {
					Files.delete(Paths.get(table.getFileName()));
					Files.delete(Paths.get(mappingFileName));
					Files.delete(Paths.get(graphFileName));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public SemTabTableCreator(String datasetFolder, String tablesFolder, String ctaGroundTruthFile,
			String cpaGroundTruthFile, String cleanedTablesFolder) {
		super();
		this.datasetFolder = datasetFolder;
		this.tablesFolder = tablesFolder;
		this.ctaGroundTruthFile = ctaGroundTruthFile;
		this.cpaGroundTruthFile = cpaGroundTruthFile;
		this.cleanedTablesFolder = cleanedTablesFolder;
	}

	private void loadRelevantColumns() {

		Set<String> invalidTables = new HashSet<String>();
		Map<DataTable, Set<String>> classesPerTable = new HashMap<DataTable, Set<String>>();

		Reader inCTA = null;
		try {
			inCTA = new FileReader(this.ctaGroundTruthFile);

			Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(inCTA);
			for (CSVRecord record : records) {
				String tableId = record.get(0);

				if (invalidTables.contains(tableId))
					continue;

				DataTable table = this.dataTables.get(tableId);

				if (table == null) {
					table = loadTable(tableId);
					if (table != null)
						classesPerTable.put(table, new HashSet<String>());
				}

				if (table == null) {
					System.out.println("Skip. Can't find/parse table: " + tableId);
					// Skip table because they are missing or not parseable
					invalidTables.add(tableId);
					continue;
				}

				table.setId(tableId);

				String columnClass = record.get(2);

				if (classesPerTable.get(table).contains(columnClass)) {
					System.out.println("SKip. Table contains two columns of same type: " + table.getFileName());
					invalidTables.add(tableId);
					continue;
				}

				classesPerTable.get(table).add(columnClass);

				this.ctaRecords.get(table).add(record);

				int relevantColumnNumber = Integer.valueOf(record.get(1));
				relevantColumnNumbers.get(table).add(relevantColumnNumber);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inCTA.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		Map<String, Set<String>> subjectTypePropertyLinesPerTable = new HashMap<String, Set<String>>();

		Reader inCPA = null;
		try {
			inCPA = new FileReader(this.cpaGroundTruthFile);

			Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(inCPA);
			for (CSVRecord record : records) {
				String tableId = record.get(0);

				if (invalidTables.contains(tableId))
					continue;

				DataTable table = this.dataTables.get(tableId);
				if (table == null) {
					table = loadTable(tableId);
				}

				if (table == null) {
					// Skip table because they are not missing or not parseable
					invalidTables.add(tableId);
					continue;
				}

				if (!subjectTypePropertyLinesPerTable.containsKey(tableId))
					subjectTypePropertyLinesPerTable.put(tableId, new HashSet<String>());

				String subjectTypePropertyLine = record.get(1) + "-" + record.get(3);
				if (subjectTypePropertyLinesPerTable.get(tableId).contains(subjectTypePropertyLine)) {
					System.out.println("Skip. Table " + tableId + " with duplicate domain/property triples: "
							+ subjectTypePropertyLine);
					invalidTables.add(tableId);
					continue;
				}

				subjectTypePropertyLinesPerTable.get(tableId).add(subjectTypePropertyLine);

				table.setId(tableId);

				this.cpaRecords.get(table).add(record);

				int relevantColumnNumber1 = Integer.valueOf(record.get(1));
				relevantColumnNumbers.get(table).add(relevantColumnNumber1);

				int relevantColumnNumber2 = Integer.valueOf(record.get(2));
				relevantColumnNumbers.get(table).add(relevantColumnNumber2);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inCPA.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.dataTables.keySet().removeAll(invalidTables);

		for (DataTable table : this.dataTables.values()) {
			if (invalidTables.contains(table.getId()))
				continue;
			if (hasColumnsWithIdenticalValues(table, relevantColumnNumbers.get(table))) {
				System.out.println("Skip. Has columns with identical values: " + table.getId() + ".");
				invalidTables.add(table.getId());
			}
		}

		this.dataTables.keySet().removeAll(invalidTables);

		// Constraint: each table has at least two relevant columns
		for (DataTable table : this.dataTables.values()) {
			if (this.relevantColumnNumbers.get(table).size() <= 1) {
				System.out.println("Skip. Not enough colums: " + table.getFileName());
				invalidTables.add(table.getId());
			}
		}

		this.dataTables.keySet().removeAll(invalidTables);

		// Constraint: each table has at least two classes
//		for (DataTable table : this.dataTables.values()) {
//			if (this.ctaRecords.get(table).size() <= 1) {
//				System.out.println("Not enough classes: " + table.getFileName());
//				invalidTables.add(table.getId());
//			}
//		}		

		this.dataTables.keySet().removeAll(invalidTables);

	}

	private boolean hasColumnsWithIdenticalValues(DataTable table, Set<Integer> relevantColumnNumbers) {

		for (int columnNumber1 : relevantColumnNumbers) {
			col2Loop: for (int columnNumber2 : relevantColumnNumbers) {
				if (columnNumber1 == columnNumber2)
					continue;

				for (Row row : table.getRows()) {
					String val1 = row.getValues().get(columnNumber1);
					String val2 = row.getValues().get(columnNumber2);

					if (val1 == null || val2 == null)
						continue;

					if (!val1.equals(val2)) {
						continue col2Loop;
					}
				}

				return true;
			}
		}

		return false;

	}

	private void createCleanedTables(int round) {

		for (DataTable table : this.dataTables.values()) {

			if (table == null)
				continue;

			System.out.println("Clean " + table.getFileName());

			Map<Integer, String> columnTitles = new HashMap<Integer, String>();
			this.allColumnTitles.put(table, columnTitles);
			String newTableFileName = cleanedTablesFolder + round + "_"
					+ table.getFileName().substring(table.getFileName().lastIndexOf("/") + 1);

			PrintWriter writer = null;
			try {
				writer = new PrintWriter(newTableFileName);
				CSVPrinter csvPrinter = null;

				List<Integer> relevantColumnNumbers = new ArrayList<Integer>();
				relevantColumnNumbers.addAll(this.relevantColumnNumbers.get(table));
				Collections.sort(relevantColumnNumbers);

				Reader inCPA = null;
				try {

					inCPA = new FileReader(table.getFileName());

					Iterable<CSVRecord> records = CSVFormat.RFC4180.withIgnoreEmptyLines().parse(inCPA);

					for (CSVRecord record : records) {
						// create mapping from column numbers before cleaning to
						// header titles
						for (int i = 0; i < record.size(); i++) {
							String columnTitle = record.get(i);
							columnTitles.put(i, columnTitle);
						}
						break;
					}

					String[] header = new String[relevantColumnNumbers.size()];
					int j = 0;
					for (int i = 0; i < columnTitles.size(); i++) {
						if (relevantColumnNumbers.contains(i)) {
							header[j] = columnTitles.get(i);
							j += 1;
						}
					}

					csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));

					int rowNumber = 0;
					for (CSVRecord record : records) {
						if (rowNumber != 0) {

							List<String> lineValues = new ArrayList<String>();

							for (int relevantColumnNumber : relevantColumnNumbers) {
								lineValues.add(record.get(relevantColumnNumber));
							}

							csvPrinter.printRecord(lineValues);
						}

						rowNumber += 1;
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						inCPA.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} finally {
				writer.close();
			}

			table.setFileName(newTableFileName);
		}

	}

	private DataTable loadTable(String tableId) {

		try {

			String tableFileName = this.tablesFolder + tableId + ".csv";

			if (!(new File(tableFileName)).exists())
				return null;

			DataTable table = DataTableReader.readDataTable(tableFileName, ",", true, false,
					DataTableFromInputGraphCreator.NULL_VALUE);

			this.dataTables.put(tableId, table);
			this.ctaRecords.put(table, new HashSet<CSVRecord>());
			this.cpaRecords.put(table, new HashSet<CSVRecord>());

			if (!relevantColumnNumbers.containsKey(table))
				relevantColumnNumbers.put(table, new HashSet<Integer>());

			return table;
		} catch (IllegalStateException e) {
			return null;
		}
	}

	private RMLMapping createRMLMapping(DataTable table) {

		Map<Integer, String> columnTitles = this.allColumnTitles.get(table);

		RMLMapping rmlMapping = new RMLMapping();
		Map<Integer, RMLSubjectMap> subjectMaps = new HashMap<Integer, RMLSubjectMap>();

		Model model = ModelFactory.createMemModelMaker().createDefaultModel();

		Set<Integer> classColumns = new HashSet<Integer>();
		Map<Integer, RDFClass> rdfClasses = new HashMap<Integer, RDFClass>();

		for (CSVRecord record : this.ctaRecords.get(table)) {
			int columnNumber = Integer.valueOf(record.get(1));
			classColumns.add(columnNumber);
			String subjectClassURI = record.get(2);
			RDFClass subjectClass = new RDFClass(model.createResource(subjectClassURI));
			rdfClasses.put(columnNumber, subjectClass);
			RMLSubjectMap subjectMap = new RMLSubjectMap(subjectClass.getResource());
			subjectMap.setIdentifier(columnTitles.get(columnNumber));
			subjectMaps.put(columnNumber, subjectMap);

			rmlMapping.addType(subjectMap);

			// column type annotation is always a rdf:label relation
			RMLLiteralPredicateObjectMap literalPredicateObjectMap = new RMLLiteralPredicateObjectMap();
			literalPredicateObjectMap.setProperty(model.getProperty("http://www.w3.org/2000/01/rdf-schema#label"));
			literalPredicateObjectMap.setColumnId(columnTitles.get(columnNumber));
			subjectMap.addLiteralMap(literalPredicateObjectMap);
		}

		for (CSVRecord record : this.cpaRecords.get(table)) {

			int subjectColumnNumber = Integer.valueOf(record.get(1));
			int objectColumnNumber = Integer.valueOf(record.get(2));

			RMLSubjectMap subjectMap = subjectMaps.get(subjectColumnNumber);

			if (classColumns.contains(objectColumnNumber)) {
				// class relation
				RMLPredicateObjectMap predicateObjectMap = new RMLPredicateObjectMap();
				predicateObjectMap.setObject(rdfClasses.get(objectColumnNumber));
				predicateObjectMap.setProperty(model.getProperty(record.get(3)));
				predicateObjectMap.setIdentifier(columnTitles.get(objectColumnNumber));
				subjectMap.addRelationMap(predicateObjectMap);
			} else {
				// literal relation
				RMLLiteralPredicateObjectMap literalPredicateObjectMap = new RMLLiteralPredicateObjectMap();
				literalPredicateObjectMap.setProperty(model.getProperty(record.get(3)));
				literalPredicateObjectMap.setColumnId(columnTitles.get(objectColumnNumber));
				if (subjectMap == null) {
					System.out.println("Unknown error. Skip.");
					return null;
				}
				subjectMap.addLiteralMap(literalPredicateObjectMap);
			}

		}

		return rmlMapping;
	}

}
