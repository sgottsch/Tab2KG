package de.l3s.simpleml.tab2kg.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Sets;

import de.l3s.simpleml.tab2kg.data.semtab.SemTabTableCreator;
import de.l3s.simpleml.tab2kg.evaluation.EvaluationInstance;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;
import de.l3s.simpleml.tab2kg.rml.ColumnLiteralMapping;
import de.l3s.simpleml.tab2kg.rml.RMLMappingReader;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.MapUtil;
import de.l3s.simpleml.tab2kg.util.Source;

public class TableGraphPairsFinder {

	private static final double TRAIN_SPLIT = 0.9;

	private Set<String> gsClasses;

	public static void main(String[] args) {

		Source source = Source.valueOf(args[0].toUpperCase());

		TableGraphPairsFinder tgpf = new TableGraphPairsFinder();

		String folderInput = Config.getPath(FileLocation.BASE_FOLDER) + source.getFolderName();
		if (source == Source.SEMTAB_EASY)
			folderInput = Config.getPath(FileLocation.BASE_FOLDER) + Source.SEMTAB.getFolderName();

		String folderOutput = Config.getPath(FileLocation.BASE_FOLDER) + source.getFolderName();

		List<EvaluationInstance> evaluationInstances = tgpf.collectPairs(folderInput, source);

		PrintWriter pairsWriter = null;
		try {
			pairsWriter = new PrintWriter(folderOutput + SemTabTableCreator.FILE_NAME_PAIRS);
			tgpf.writePairs(pairsWriter, evaluationInstances, folderOutput);
			System.out.println("#pairs: " + evaluationInstances.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			pairsWriter.close();
		}

		if (source == Source.GITHUB) {

			List<String> prefixes = new ArrayList<String>();

			// same repository should not be in training and test
			for (EvaluationInstance pair : evaluationInstances) {
				String prefix = pair.getTableFileName().substring(0, pair.getTableFileName().lastIndexOf("_"));
				prefix = prefix.substring(0, prefix.lastIndexOf("_"));
				if (!prefixes.contains(prefix))
					prefixes.add(prefix);
			}

			// split into test and training
			Collections.shuffle(prefixes);
			List<String> prefixesTraining = prefixes.subList(0, (int) Math.ceil(TRAIN_SPLIT * prefixes.size()));

			List<EvaluationInstance> evaluationInstancesTraining = new ArrayList<EvaluationInstance>();
			List<EvaluationInstance> evaluationInstancesTest = new ArrayList<EvaluationInstance>();
			for (EvaluationInstance pair : evaluationInstances) {
				String prefix = pair.getTableFileName().substring(0, pair.getTableFileName().lastIndexOf("_"));
				prefix = prefix.substring(0, prefix.lastIndexOf("_"));
				if (prefixesTraining.contains(prefix))
					evaluationInstancesTraining.add(pair);
				else
					evaluationInstancesTest.add(pair);
			}

			PrintWriter pairsWriterTraining = null;
			try {
				pairsWriterTraining = new PrintWriter(folderOutput + SemTabTableCreator.FILE_NAME_PAIRS_TRAINING);
				tgpf.writePairs(pairsWriterTraining, evaluationInstancesTraining, folderOutput);
				System.out.println("#training pairs: " + evaluationInstancesTraining.size());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				pairsWriterTraining.close();
			}
			PrintWriter pairsWriterTest = null;
			try {
				pairsWriterTest = new PrintWriter(folderOutput + SemTabTableCreator.FILE_NAME_PAIRS_TEST);
				tgpf.writePairs(pairsWriterTest, evaluationInstancesTest, folderOutput);
				System.out.println("#test pairs: " + evaluationInstancesTest.size());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				pairsWriterTest.close();
			}

		}

		tgpf.createDomainOntologies(source, folderInput, folderOutput);
	}

	private void createDomainOntologies(Source source, String folder, String folderOutput) {

		getValidSemtabEasyTables(folder);

		if (source == Source.SEMTAB)
			createSemtabDomainOntologies(folder, folderOutput, getValidSemtabEasyTables(folder));
		else if (source == Source.SEMTAB_EASY)
			System.out.println("domain ontology creation for SEMTAB_EASY is done together with SEMTAB");
		else if (source == Source.SOCCER || source == Source.WEAPONS)
			createCompleteDomainOntology(folder, folderOutput);
	}

	private void createSemtabDomainOntologies(String folder, String folderOutput, Set<String> validTables) {

		List<EvaluationInstance> evaluationInstances = new ArrayList<EvaluationInstance>();
		List<EvaluationInstance> evaluationInstancesEasy = new ArrayList<EvaluationInstance>();

		Map<String, Map<String, Integer>> groupsByClass = new HashMap<String, Map<String, Integer>>();
		Map<String, Map<String, Integer>> propertiesByClass = new HashMap<String, Map<String, Integer>>();

		File[] mappingFiles = (new File(folder + "mappings")).listFiles();

		Map<String, List<File>> filesByMainClass = new HashMap<String, List<File>>();

		for (File file : mappingFiles) {

			Map<String, Integer> classCounts = getClasses(file);

			Set<String> relations = getRelationsAndClasses(file).get(0);

			int highestCount = -1;
			Set<String> mainClasses = new HashSet<String>();
			for (String domainClass : classCounts.keySet()) {
				if (classCounts.get(domainClass) > highestCount) {
					highestCount = classCounts.get(domainClass);
					mainClasses.add(domainClass);
				}
			}

			if (mainClasses.size() > 1)
				continue;

			String mainClass = null;
			for (String m : mainClasses)
				mainClass = m;

			if (!groupsByClass.containsKey(mainClass)) {
				groupsByClass.put(mainClass, new HashMap<String, Integer>());
				filesByMainClass.put(mainClass, new ArrayList<File>());
				propertiesByClass.put(mainClass, new HashMap<String, Integer>());
			}

			for (String cl : classCounts.keySet()) {
				if (!groupsByClass.get(mainClass).containsKey(cl))
					groupsByClass.get(mainClass).put(cl, 1);
				else
					groupsByClass.get(mainClass).put(cl, groupsByClass.get(mainClass).get(cl) + 1);
			}

			for (String pr : relations) {
				if (!propertiesByClass.get(mainClass).containsKey(pr))
					propertiesByClass.get(mainClass).put(pr, 1);
				else
					propertiesByClass.get(mainClass).put(pr, propertiesByClass.get(mainClass).get(pr) + 1);
			}

			filesByMainClass.get(mainClass).add(file);
		}

		PrintWriter ontologiesListWriter = null;
		try {
			ontologiesListWriter = new PrintWriter(folder + "/" + SemTabTableCreator.FILE_NAME_DOMAIN_ONTOLOGIES_LIST);
			for (String domainClass : groupsByClass.keySet()) {
				if (filesByMainClass.get(domainClass).size() < 10 || propertiesByClass.get(domainClass).size() < 5)
					continue;

				ontologiesListWriter.println(domainClass.replace("http://dbpedia.org/ontology/", "") + "\t"
						+ groupsByClass.get(domainClass).size() + "\t" + propertiesByClass.get(domainClass).size()
						+ "\t" + getSortedString(groupsByClass.get(domainClass)) + "\t"
						+ getSortedString(propertiesByClass.get(domainClass)));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			ontologiesListWriter.close();
		}

		// create domain ontologies

		for (String domainClass : filesByMainClass.keySet()) {

			if (filesByMainClass.get(domainClass).size() < 10 || propertiesByClass.get(domainClass).size() < 5)
				continue;

			PrintWriter completeGraphWriter = null;
			String fileName = folder + SemTabTableCreator.FOLDER_NAME_DOMAIN_ONTOLOGIES + "/"
					+ domainClass.replace("http://dbpedia.org/ontology/", "").replace("/", "-") + ".ttl";

			String fileNameTable = folder + SemTabTableCreator.FOLDER_NAME_DOMAIN_ONTOLOGY_TABLES + "/"
					+ domainClass.replace("http://dbpedia.org/ontology/", "").replace("/", "-") + ".csv";
			String fileNameMapping = folder + SemTabTableCreator.FOLDER_NAME_DOMAIN_ONTOLOGY_MODELS + "/"
					+ domainClass.replace("http://dbpedia.org/ontology/", "").replace("/", "-") + ".csv.model.json";

			OutputStream os;
			try {
				os = new FileOutputStream(fileName);

				completeGraphWriter = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

				Map<String, String> blocks = new LinkedHashMap<String, String>();
				Map<String, String> replacements = new HashMap<String, String>();
				Map<String, String> blocksOriginal = new HashMap<String, String>();

				for (File file : filesByMainClass.get(domainClass)) {

					File graphFile1 = new File(file.getPath().replace("mappings", "graphs").replace(".rml", ".ttl"));

					String tableFileName = folder + SemTabTableCreator.FOLDER_NAME_TABLES + "/"
							+ file.getName().replaceAll(".ttl$", "").replaceAll(".rml$", "");

					if (!tableFileName.endsWith(".csv"))
						tableFileName = tableFileName + ".csv";

					FileReader fileReader = new FileReader(graphFile1);
					try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
						String line;
						String block = "";
						while ((line = bufferedReader.readLine()) != null) {

							if (line.isEmpty() && !block.isEmpty()) {

								if (block.contains(" ")) {

									String resourceURI = block.substring(0, block.indexOf(" ")).trim().replace("\n",
											"");
									String blockReplaced = "XY@Z" + block.substring(block.indexOf(" "));

									if (blocks.containsKey(blockReplaced)) {
										replacements.put(resourceURI, blocks.get(blockReplaced));
									} else {
										blocksOriginal.put(blockReplaced, block);
										blocks.put(blockReplaced, resourceURI);
									}
								}

								block = "";
							}

							if (!line.isEmpty())
								block += line + "\n";
						}

					} catch (IOException e) {
						e.printStackTrace();
					}

					EvaluationInstance instance = new EvaluationInstance(tableFileName, fileName, graphFile1.getPath()
							.replace("\\", "/").replace("/graphs/", "/mappings/").replaceAll(".ttl$", ".rml"));
					evaluationInstances.add(instance);

					if (validTables.contains(
							tableFileName.replace(Source.SEMTAB.getFolderName(), Source.SEMTAB_EASY.getFolderName()))) {
						EvaluationInstance instanceEasy = new EvaluationInstance(
								tableFileName
										.replace(Source.SEMTAB.getFolderName(), Source.SEMTAB_EASY.getFolderName()),
								fileName,
								graphFile1.getPath().replace("\\", "/").replace("/graphs/", "/mappings/")
										.replaceAll(".ttl$", ".rml")
										.replace(Source.SEMTAB.getFolderName(), Source.SEMTAB_EASY.getFolderName()));
						evaluationInstancesEasy.add(instanceEasy);
					}

				}

				for (String blockReplaced : blocks.keySet()) {
					String block = blocksOriginal.get(blockReplaced);
					for (String replace : replacements.keySet()) {
						block = block.replace(replace, replacements.get(replace));
					}
					completeGraphWriter.println(block);
				}

			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			} finally {
				completeGraphWriter.close();
			}

			DomainGraphToTableConverter.run(fileName, fileNameTable, fileNameMapping);

		}

		PrintWriter pairsWriter = null;
		try {
			pairsWriter = new PrintWriter(folderOutput + SemTabTableCreator.FILE_NAME_PAIRS_DOMAIN_GRAPHS);
			writePairs(pairsWriter, evaluationInstances, folder);
			System.out.println("#pairs: " + evaluationInstances.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			pairsWriter.close();
		}

		PrintWriter pairsWriterEasy = null;
		try {
			pairsWriterEasy = new PrintWriter(
					folderOutput.replace(Source.SEMTAB.getFolderName(), Source.SEMTAB_EASY.getFolderName())
							+ SemTabTableCreator.FILE_NAME_PAIRS_DOMAIN_GRAPHS);
			writePairs(pairsWriterEasy, evaluationInstancesEasy, folder);
			System.out.println("#pairs (easy): " + evaluationInstancesEasy.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			pairsWriterEasy.close();
		}

	}

	private void createCompleteDomainOntology(String folder, String folderOutput) {

		File[] graphFiles = (new File(folder + SemTabTableCreator.FOLDER_NAME_GRAPHS)).listFiles();

		List<EvaluationInstance> evaluationInstances = new ArrayList<EvaluationInstance>();

		PrintWriter completeGraphWriter = null;
		String fileName = folderOutput + SemTabTableCreator.FOLDER_NAME_DOMAIN_ONTOLOGIES + "/domain_graph.ttl";
		String fileNameTable = folderOutput + SemTabTableCreator.FOLDER_NAME_DOMAIN_ONTOLOGY_TABLES
				+ "/domain_graph.csv";
		String fileNameMapping = folderOutput + SemTabTableCreator.FOLDER_NAME_DOMAIN_ONTOLOGY_MODELS
				+ "/domain_graph.csv.model.json";

		Map<String, Integer> classes = new HashMap<String, Integer>();
		Map<String, Integer> relations = new HashMap<String, Integer>();

		PrintWriter ontologiesListWriter = null;
		OutputStream os;
		try {
			ontologiesListWriter = new PrintWriter(folder + "/" + SemTabTableCreator.FILE_NAME_DOMAIN_ONTOLOGIES_LIST);
			os = new FileOutputStream(fileName);

			completeGraphWriter = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));

			Map<String, String> blocks = new LinkedHashMap<String, String>();
			Map<String, String> replacements = new HashMap<String, String>();
			Map<String, String> blocksOriginal = new HashMap<String, String>();

			for (File graphFile1 : graphFiles) {

				File mappingFile = new File(graphFile1.getPath().replace("graphs", "mappings").replace(".ttl", ".rml"));
				List<Set<String>> relationsAndClasses = getRelationsAndClasses(mappingFile);

				for (String cl : relationsAndClasses.get(1)) {
					if (!classes.containsKey(cl))
						classes.put(cl, 1);
					else
						classes.put(cl, classes.get(cl) + 1);
				}
				for (String pr : relationsAndClasses.get(0)) {
					if (!relations.containsKey(pr))
						relations.put(pr, 1);
					else
						relations.put(pr, relations.get(pr) + 1);
				}
				String tableFileName = folder + SemTabTableCreator.FOLDER_NAME_TABLES + "/"
						+ graphFile1.getName().replaceAll(".ttl$", "");
				if (!tableFileName.endsWith(".csv"))
					tableFileName = tableFileName + ".csv";

				FileReader fileReader = new FileReader(graphFile1);
				try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
					String line;
					String block = "";
					while ((line = bufferedReader.readLine()) != null) {

						if (line.isEmpty() && !block.isEmpty()) {

							if (block.contains(" ")) {

								String resourceURI = block.substring(0, block.indexOf(" ")).trim().replace("\n", "");
								String blockReplaced = "XY@Z" + block.substring(block.indexOf(" "));

								if (blocks.containsKey(blockReplaced)) {
									replacements.put(resourceURI, blocks.get(blockReplaced));
								} else {
									blocksOriginal.put(blockReplaced, block);
									blocks.put(blockReplaced, resourceURI);
								}
							}

							block = "";
						}

						if (!line.isEmpty())
							block += line + "\n";
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

				EvaluationInstance instance = new EvaluationInstance(tableFileName, fileName, graphFile1.getPath()
						.replace("\\", "/").replace("/graphs/", "/mappings/").replaceAll(".ttl$", ".rml"));
				evaluationInstances.add(instance);

			}

			for (String blockReplaced : blocks.keySet()) {
				String block = blocksOriginal.get(blockReplaced);
				for (String replace : replacements.keySet()) {
					block = block.replace(replace, replacements.get(replace));
				}
				completeGraphWriter.println(block);
			}

			ontologiesListWriter.println("\t" + classes.size() + "\t" + relations.size() + "\t"
					+ getSortedString(classes) + "\t" + getSortedString(relations));

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} finally {
			completeGraphWriter.close();
			ontologiesListWriter.close();
		}

		DomainGraphToTableConverter.run(fileName, fileNameTable, fileNameMapping);

		PrintWriter pairsWriter = null;
		try {
			pairsWriter = new PrintWriter(folderOutput + SemTabTableCreator.FILE_NAME_PAIRS_DOMAIN_GRAPHS);
			writePairs(pairsWriter, evaluationInstances, folder);
			System.out.println("#pairs: " + evaluationInstances.size());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			pairsWriter.close();
		}
	}

//	private void createCompleteGraphs(String folder, String folderOutput) {
//		File[] graphFiles = (new File(folder + SemTabTableCreator.FOLDER_NAME_GRAPHS)).listFiles();
//
//		List<EvaluationInstance> evaluationInstances = new ArrayList<EvaluationInstance>();
//
//		for (File graphFile1 : graphFiles) {
//			PrintWriter completeGraphWriter = null;
//
//			try {
//				// completeGraphWriter = new PrintWriter(folderOutput +
//				// SemTabTableCreator.FILE_NAME_COMPLETE_GRAPH);
//
//				String fileName = folder + SemTabTableCreator.FOLDER_NAME_COMPLETE_GRAPHS + "/" + graphFile1.getName();
//
//				OutputStream os = new FileOutputStream(fileName);
//				completeGraphWriter = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
//				// completeGraphWriter = new PrintWriter(fileName);
//
//				for (File graphFile2 : graphFiles) {
//					if (graphFile1 == graphFile2)
//						continue;
//
//					FileReader fileReader = new FileReader(graphFile2);
//					try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
//						String line;
//						while ((line = bufferedReader.readLine()) != null) {
//							completeGraphWriter.println(line);
//						}
//
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//
//				}
//
//				EvaluationInstance instance = new EvaluationInstance(
//						folder + SemTabTableCreator.FOLDER_NAME_TABLES + "/"
//								+ graphFile1.getName().replaceAll(".ttl$", ""),
//						folder + SemTabTableCreator.FOLDER_NAME_COMPLETE_GRAPHS + "/" + graphFile1.getName(),
//						graphFile1.getPath().replace("\\","/").replace("/graphs/","/mappings/").replaceAll(".ttl$", ".rml"));
//
//				evaluationInstances.add(instance);
//
//			} catch (FileNotFoundException | UnsupportedEncodingException e) {
//				e.printStackTrace();
//			} finally {
//				completeGraphWriter.close();
//			}
//		}
//
//		PrintWriter pairsWriter = null;
//		try {
//			pairsWriter = new PrintWriter(folderOutput + SemTabTableCreator.FILE_NAME_PAIRS_COMPLETE_GRAPHS);
//			writePairs(pairsWriter, evaluationInstances, folderOutput);
//			System.out.println("#pairs: " + evaluationInstances.size());
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			pairsWriter.close();
//		}
//
//	}

	private void writePairs(PrintWriter pairsWriter, List<EvaluationInstance> evaluationInstances, String folder) {
		for (EvaluationInstance evaluationInstance : evaluationInstances) {
			String id = evaluationInstance.getTableFileName()
					.substring(evaluationInstance.getTableFileName().lastIndexOf("/"));

			pairsWriter.println(Config.getFolderNameRelativeToBase(evaluationInstance.getTableFileName()) + "\t"
					+ Config.getFolderNameRelativeToBase(folder + SemTabTableCreator.FOLDER_NAME_MODELS) + id
					+ ".model.json" + "\t" + Config.getFolderNameRelativeToBase(evaluationInstance.getGraphFileName())
					+ "\t" + Config.getFolderNameRelativeToBase(evaluationInstance.getMappingFileName()));
		}
	}

	/**
	 * Search for graph pairs where one of the schema graphs is a sub graph of the
	 * other schema graph.
	 * 
	 * @param source
	 */
	public List<EvaluationInstance> collectPairs(String folder, Source source) {
		return collectPairs(folder, null, null, source);
	}

	/**
	 * Search for graph pairs where one of the schema graphs is a sub graph of the
	 * other schema graph.
	 */
	public List<EvaluationInstance> collectPairs(String folder, Map<String, String> validPairs, Set<String> validFiles,
			Source source) {

		System.out.println("Collect pairs in " + folder);

		if (source == Source.SEMTAB_EASY)
			this.gsClasses = getGoldStandardClasses();

		List<EvaluationInstance> evaluationInstances = new ArrayList<EvaluationInstance>();

		File[] mappingFiles = (new File(folder + "mappings")).listFiles();

		Map<File, Set<String>> relationsPerFile = new HashMap<File, Set<String>>();
		Map<File, Set<String>> classesPerFile = new HashMap<File, Set<String>>();

		for (File file : mappingFiles) {

			List<Set<String>> relationsAndClasses = getRelationsAndClasses(file);

			if (!relationsPerFile.containsKey(file))
				relationsPerFile.put(file, relationsAndClasses.get(0));

			if (!classesPerFile.containsKey(file))
				classesPerFile.put(file, relationsAndClasses.get(1));
		}

		int fileNo = 0;
		for (File file1 : mappingFiles) {

			if (fileNo % 100 == 0)
				System.out.println(
						fileNo + "/" + mappingFiles.length + " (" + ((double) fileNo / mappingFiles.length) + ")");
			fileNo += 1;

			String fileName1 = null;
			if (validFiles != null) {
				fileName1 = file1.getName().substring(file1.getName().lastIndexOf("/") + 1);
				fileName1 = fileName1.replaceAll(".rml$", "").replaceAll(".csv.model.json$", "").replaceAll(".csv$", "")
						.replaceAll(".ttl$", "");
				if (!validFiles.contains(fileName1))
					continue;
			}

			for (File file2 : mappingFiles) {

				if (file1 == file2)
					continue;

				if (source == Source.GITHUB) {
					String file1Prefix = file1.getName().substring(0, file1.getName().lastIndexOf("_"));
					String file2Prefix = file2.getName().substring(0, file2.getName().lastIndexOf("_"));
					if (!file1Prefix.equals(file2Prefix))
						continue;
				}
//				else if (source == Source.SEMTAB || source == Source.SEMTAB_EASY) {
//					// SEMTAB: only pairs from same round
//					int round1 = Integer.parseInt(file1.getName().substring(0, file1.getName().indexOf("_")));
//					int round2 = Integer.parseInt(file2.getName().substring(0, file2.getName().indexOf("_")));
//					if (round1 != round2)
//						continue;
//				}

				String evaluationInstanceId = null;

				if (!relationsPerFile.containsKey(file2))
					relationsPerFile.put(file2, getRelationsAndClasses(file2).get(0));

				if (validPairs != null) {
					String fileName2 = file2.getName().substring(file2.getName().lastIndexOf("/") + 1);
					fileName2 = fileName2.replaceAll(".rml$", "").replaceAll(".csv.model.json$", "")
							.replaceAll(".csv$", "").replaceAll(".ttl$", "");

					evaluationInstanceId = validPairs.get(fileName1 + " " + fileName2);

					if (evaluationInstanceId == null)
						continue;
				}

				boolean isValid = checkPair(file1, file2, relationsPerFile);
				if (source == Source.SEMTAB_EASY && (!isValidForSemTabEasy(classesPerFile.get(file1))
						|| !isValidForSemTabEasy(classesPerFile.get(file2))))
					isValid = false;

				if (isValid) {
//					System.out.println(folder + "data_cleaned/"
//							+ file1.getName().replaceAll(".rml$", "").replaceAll(".csv$", "") + ".csv");
					EvaluationInstance instance = new EvaluationInstance(
							folder + SemTabTableCreator.FOLDER_NAME_TABLES + "/"
									+ file1.getName().replaceAll(".rml$", "").replaceAll(".csv$", "") + ".csv",
							folder + SemTabTableCreator.FOLDER_NAME_GRAPHS + "/"
									+ file2.getName().replaceAll(".rml$", ".ttl"),
							file1.getPath());
					evaluationInstances.add(instance);
					instance.setId(evaluationInstanceId);

					// in SemTab, we only want to use each input file once
					if (source == Source.SEMTAB || source == Source.SEMTAB_EASY)
						break;
				}
			}
		}

		// System.out.println("numberOfPairs: " + numberOfPairs);

		return evaluationInstances;
	}

	private boolean isValidForSemTabEasy(Set<String> classes) {

		if (classes.size() > 1)
			return false;

		if (!Sets.difference(classes, gsClasses).isEmpty())
			return false;

		return true;
	}

	private boolean checkPair(File file1, File file2, Map<File, Set<String>> relationsPerFile) {

		Set<String> relations1 = relationsPerFile.get(file1);
		Set<String> relations2 = relationsPerFile.get(file2);

		// file1 is sub graph of file2

		if (Sets.difference(relations1, relations2).isEmpty() && !Sets.difference(relations2, relations1).isEmpty()) {
			// System.out.println("");
			// System.out.println("Super graph: " + file2.getName());
			// System.out.println("File graph: " + file1.getName());

			// System.out.println(relations1 + " / " + relations2);
			return true;
		}

		return false;
	}

	public List<Set<String>> getRelationsAndClasses(File file) {

		List<Set<String>> res = new ArrayList<Set<String>>();

		Set<String> relations = new HashSet<String>();
		res.add(relations);
		Set<String> classes = new HashSet<String>();
		res.add(classes);

		List<ColumnLiteralMapping> mappings1;
		try {
			RMLMappingReader rmr = new RMLMappingReader();
			mappings1 = rmr.getMappings(file.getPath());

			for (ColumnLiteralMapping clm : mappings1) {
				relations.add(clm.getProperty().getURI() + " " + clm.getSubjectClass().getURI());
				classes.add(clm.getSubjectClass().getURI().replace("http://dbpedia.org/ontology/", ""));
			}
			List<RDFNodeTriple> mappings1ClassRelations = rmr.getClassMappings(file.getPath());
			for (RDFNodeTriple cr : mappings1ClassRelations) {
				relations.add(
						cr.getProperty().getURI() + " " + cr.getSubject().getURI() + " " + cr.getObject().getURI());
				classes.add(cr.getSubject().getURI().replace("http://dbpedia.org/ontology/", ""));
				classes.add(cr.getObject().getURI().replace("http://dbpedia.org/ontology/", ""));
			}
			return res;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, Integer> getClasses(File file) {

		Map<String, Integer> res = new HashMap<String, Integer>();

		List<ColumnLiteralMapping> mappings1;
		try {
			RMLMappingReader rmr = new RMLMappingReader();
			mappings1 = rmr.getMappings(file.getPath());

			for (ColumnLiteralMapping clm : mappings1) {
				if (!res.containsKey(clm.getSubjectClass().getURI()))
					res.put(clm.getSubjectClass().getURI(), 1);
				else
					res.put(clm.getSubjectClass().getURI(), res.get(clm.getSubjectClass().getURI()) + 1);
			}
			return res;

		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public Set<String> getGoldStandardClasses() {
		Set<String> gsClasses = new HashSet<String>();

		LineIterator it = null;
		try {
			it = FileUtils.lineIterator(new File(Config.getPath(FileLocation.GS_CLASSES)), "UTF-8");
			while (it.hasNext()) {
				String line = it.nextLine();
				gsClasses.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				it.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return gsClasses;
	}

	public Set<String> getValidSemtabEasyTables(String folder) {

		folder = folder.replace(Source.SEMTAB.getFolderName(), Source.SEMTAB_EASY.getFolderName());

		if (this.gsClasses == null)
			this.gsClasses = getGoldStandardClasses();

		Set<String> tableNames = new HashSet<String>();

		File[] mappingFiles = (new File(folder + "mappings")).listFiles();

		for (File file : mappingFiles) {

			Set<String> classes = getRelationsAndClasses(file).get(1);

			if (isValidForSemTabEasy(classes))
				tableNames.add(file.getPath().replace("mappings", "tables").replace(".rml", ".csv"));
		}

		System.out.println("Valid SemTab_Easy tables: " + tableNames.size());

		return tableNames;
	}

	private String getSortedString(Map<String, Integer> map) {
		List<String> sorted = new ArrayList<String>();

		for (String value : MapUtil.sortByValueDescending(map).keySet())
			sorted.add(value.replace("http://dbpedia.org/ontology/", "").replace("http://dbpedia.org/ontology/", "")
					.replace("http://www.w3.org/2000/01/rdf-schema#", "rdfs:").replace("http://schema.org/", "")
					.replace("http://schema.dig.isi.edu/ontology/", ""));

		return StringUtils.join(sorted, ", ");
	}

}
