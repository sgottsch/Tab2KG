package de.l3s.simpleml.tab2kg.data.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.simpleml.tab2kg.data.ModelFilesCreator;
import de.l3s.simpleml.tab2kg.data.semtab.SemTabTableCreator;
import de.l3s.simpleml.tab2kg.graph.DataTableFromInputGraphCreator;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClass;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClassLiteralTriple;
import de.l3s.simpleml.tab2kg.rml.RMLMappingCreator;
import de.l3s.simpleml.tab2kg.rml.RMLMappingExecutor;
import de.l3s.simpleml.tab2kg.rml.model.RMLLiteralPredicateObjectMap;
import de.l3s.simpleml.tab2kg.rml.model.RMLMapping;
import de.l3s.simpleml.tab2kg.rml.model.RMLPredicateObjectMap;
import de.l3s.simpleml.tab2kg.rml.model.RMLSubjectMap;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Source;

public class ModelsDataSetTableCreator {

	private Model model;

	private Map<String, String> nodeToURI = new HashMap<String, String>();
	private Map<Resource, RDFClass> rdfClasses = new HashMap<Resource, RDFClass>();

	private Set<RDFClassLiteralTriple> literalRelations = new HashSet<RDFClassLiteralTriple>();

	private HashSet<String> usedColumnNames;

	public static void main(String[] args) {

		Source source = Source.valueOf(args[0]);

		String sourceFolder = Config.getPath(FileLocation.BASE_FOLDER) + source.getFolderName();

		String tablesFolderOriginal = args[1];
		String modelsFolderOriginal = args[2];

		if (!tablesFolderOriginal.endsWith("/"))
			tablesFolderOriginal = tablesFolderOriginal + "/";
		if (!modelsFolderOriginal.endsWith("/"))
			modelsFolderOriginal = modelsFolderOriginal + "/";

		String tablesFolder = sourceFolder + SemTabTableCreator.FOLDER_NAME_TABLES + "/";
		String mappingsFolder = sourceFolder + SemTabTableCreator.FOLDER_NAME_MAPPINGS + "/";
		String graphsFolder = sourceFolder + SemTabTableCreator.FOLDER_NAME_GRAPHS + "/";
		String modelsFolder = sourceFolder + SemTabTableCreator.FOLDER_NAME_MODELS + "/";

		try {
			Files.createDirectories(Paths.get(tablesFolder));
			Files.createDirectories(Paths.get(mappingsFolder));
			Files.createDirectories(Paths.get(graphsFolder));
			Files.createDirectories(Paths.get(modelsFolder));
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (File file : (new File(tablesFolderOriginal)).listFiles()) {

			if (source == Source.SOCCER && !file.getName().endsWith(".csv"))
				continue;

			if (file.getName().startsWith("."))
				continue;

			System.out.println(file.getName());

			ModelsDataSetTableCreator reader = new ModelsDataSetTableCreator();
			reader.init();
			String newFileName = tablesFolder + file.getName().replace(" ", "_");
			if (!newFileName.endsWith(".csv"))
				newFileName = newFileName + ".csv";

			RMLMapping rmlMapping = reader.readGraph(new File(modelsFolderOriginal + file.getName() + ".model.json"));
			rmlMapping.setSourceFileName(newFileName);
			rmlMapping.setDelimiter(",");

			reader.createDataTableFile(source, file, newFileName, ",");

			String mappingFileName = mappingsFolder + newFileName.substring(newFileName.lastIndexOf("/") + 1) + ".rml";
			RMLMappingCreator.createMappingString(rmlMapping, mappingFileName);

			String graphFileName = graphsFolder + newFileName.substring(newFileName.lastIndexOf("/") + 1) + ".ttl";
			RMLMappingExecutor exec = new RMLMappingExecutor(mappingFileName);

			exec.run(graphFileName, null);

			ModelFilesCreator.createModelFile(newFileName.substring(newFileName.lastIndexOf("/") + 1), mappingFileName,
					modelsFolder + newFileName.substring(newFileName.lastIndexOf("/") + 1) + ".model.json");
		}

	}

	private void createDataTableFile(Source source, File file, String newFileName, String delimiter) {

		if (source == Source.SOCCER)
			createDataTableFileISWC2016(file, newFileName, delimiter);
		else if (source == Source.WEAPONS)
			WeaponsToCSVFileTransformer.transform(file, newFileName, this.usedColumnNames);
		else
			System.out.println(source + " not supported.");

	}

	private String createDataTableFileISWC2016(File tableFile, String newFileName, String delimiter) {

		// String newFileName = tableFileName.substring(0,
		// tableFileName.lastIndexOf(".")) + ".colnum.csv";

		Set<Integer> relevantColumns = new HashSet<Integer>();
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(newFileName);
			CSVPrinter csvPrinter = null;
			Reader in;
			try {
				in = new FileReader(tableFile.getPath());

				Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
				// for (Iterator<CSVRecord> it = records.iterator();
				// it.hasNext();)
				// {

				int rowNumber = 0;
				for (CSVRecord record : records) {

					if (rowNumber == 0) {
						// header
						List<String> columnTitles = new ArrayList<String>();
						columnTitles.add(DataTableFromInputGraphCreator.ROW_NUMBER);

						for (int i = 0; i < record.size(); i++) {
							String columnTitle = record.get(i);
							if (this.usedColumnNames.contains(columnTitle)) {
								// columnTitles.add(columnTitle);
								columnTitles.add(columnTitle);
								relevantColumns.add(i);
							}
						}
						String[] header = new String[columnTitles.size()];
						int i = 0;
						for (String headerValue : columnTitles) {
							header[i] = headerValue;
							i += 1;
						}
						csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));
					} else {
						List<String> lineValues = new ArrayList<String>();
						lineValues.add(String.valueOf(rowNumber - 1));
						for (int i = 0; i < record.size(); i++) {
							if (relevantColumns.contains(i)) {
								String value = record.get(i).replace("�", "?");
								lineValues.add(value);
							}
						}
						csvPrinter.printRecord(lineValues);
					}
					rowNumber += 1;
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			writer.close();
		}

		return newFileName;
	}

	public void init(ModelsDataSetTableCreator otherReader) {
		if (otherReader == null)
			init();
		else {
			this.model = otherReader.getModel();
			this.nodeToURI = otherReader.getNodeToURI();
			this.rdfClasses = otherReader.getRdfClasses();
			this.literalRelations = otherReader.getLiteralRelations();
		}
	}

	private void init() {
		this.model = ModelFactory.createMemModelMaker().createDefaultModel();
	}

	public RMLMapping readGraph(File schemaFile) {

		RMLMapping rmlMapping = new RMLMapping();
		Map<RDFClass, RMLSubjectMap> subjectMaps = new HashMap<RDFClass, RMLSubjectMap>();
		Map<RDFClass, String> identifiers = new HashMap<RDFClass, String>();

		this.usedColumnNames = new HashSet<String>();
		Set<String> usedRelations = new HashSet<String>();

		try {
			JSONObject json = new JSONObject(FileUtils.readFileToString(schemaFile, "UTF-8"));

			JSONObject graphJSON = json.getJSONObject("graph");

			JSONArray nodesArr = graphJSON.getJSONArray("nodes");
			for (int i = 0; i < nodesArr.length(); i++) {
				JSONObject nodeJSON = nodesArr.getJSONObject(i);
				String nodeId = nodeJSON.getString("id");
				String type = nodeJSON.getString("type");
				if (type.equals("InternalNode")) {
					String uri = nodeJSON.getJSONObject("label").getString("uri");

					Resource resource = model.createResource(uri);
					nodeToURI.put(nodeId, uri);

					RDFClass rdfClass = rdfClasses.get(resource);
					if (rdfClass == null) {
						rdfClass = new RDFClass(resource);
						rdfClasses.put(resource, rdfClass);

						RMLSubjectMap subjectMap = new RMLSubjectMap(rdfClass.getResource());
						subjectMap.setIdentifier(DataTableFromInputGraphCreator.ROW_NUMBER);
						rmlMapping.addType(subjectMap);
						subjectMaps.put(rdfClass, subjectMap);
					}

				} else if (type.equals("ColumnNode")) {
					String columnName = nodeJSON.getString("columnName");

					usedColumnNames.add(columnName);

					JSONObject columnJSON = nodeJSON.getJSONArray("userSemanticTypes").getJSONObject(0);
					String domainURI = columnJSON.getJSONObject("domain").getString("uri");
					String typeURI = columnJSON.getJSONObject("type").getString("uri");

					String relationId = domainURI + " " + typeURI;
					// if a mapping to a relation occurs multiple times, only
					// take the first column
					if (usedRelations.contains(relationId)) {
						continue;
					}
					usedRelations.add(relationId);

					RDFClass rdfClass = rdfClasses.get(model.createResource(domainURI));
					RMLSubjectMap subjectMap = subjectMaps.get(rdfClass);

					RMLLiteralPredicateObjectMap literalPredicateObjectMap = new RMLLiteralPredicateObjectMap();

					if (typeURI.equals("http://schema.org/name")) {
						subjectMap.setIdentifier(columnName);
						identifiers.put(rdfClass, columnName);
					}

					literalPredicateObjectMap.setProperty(model.getProperty(typeURI));
					literalPredicateObjectMap.setColumnId(columnName);

					subjectMap.addLiteralMap(literalPredicateObjectMap);
				}
			}

			JSONArray linksArr = graphJSON.getJSONArray("links");

			for (int i = 0; i < linksArr.length(); i++) {
				JSONObject linkJSON = linksArr.getJSONObject(i);
				String linkId = linkJSON.getString("id");
				String[] linkIdParts = linkId.split("---");
				String subjectClassURI = nodeToURI.get(linkIdParts[0]);
				String propertyURI = linkIdParts[1];

				Property property = model.getProperty(propertyURI);

				String objectClassURI = linkIdParts[2];

				Resource subject = model.getResource(subjectClassURI);
				RDFClass subjectRDFClass = rdfClasses.get(subject);

				if (nodeToURI.containsKey(objectClassURI)) {
					Resource object = model.getResource(nodeToURI.get(objectClassURI));
					model.add(subject, property, object);

					RMLSubjectMap subjectMap = subjectMaps.get(subjectRDFClass);
					RMLPredicateObjectMap predicateObjectMap = new RMLPredicateObjectMap();
					predicateObjectMap.setObject(rdfClasses.get(object));
					predicateObjectMap.setProperty(property);

					if (identifiers.containsKey(rdfClasses.get(object)))
						predicateObjectMap.setIdentifier(identifiers.get(rdfClasses.get(object)));
					else
						predicateObjectMap.setIdentifier(DataTableFromInputGraphCreator.ROW_NUMBER);
					subjectMap.addRelationMap(predicateObjectMap);
				} else {

					boolean existsAlready = false;
					for (RDFClassLiteralTriple literalClassRelation : this.literalRelations) {
						if (literalClassRelation.getSubject() == subjectRDFClass
								&& literalClassRelation.getProperty() == property) {
							existsAlready = true;
							break;
						}
					}

					if (!existsAlready) {
						RDFClassLiteralTriple literalClassRelation = new RDFClassLiteralTriple(subjectRDFClass,
								property, null);
						literalRelations.add(literalClassRelation);
					}
				}
			}

		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}

		return rmlMapping;
	}

	public Model getModel() {
		return model;
	}

	public Map<String, String> getNodeToURI() {
		return nodeToURI;
	}

	public Map<Resource, RDFClass> getRdfClasses() {
		return rdfClasses;
	}

	public Set<RDFClassLiteralTriple> getLiteralRelations() {
		return literalRelations;
	}

}
