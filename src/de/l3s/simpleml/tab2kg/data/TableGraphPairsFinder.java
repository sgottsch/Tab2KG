package de.l3s.simpleml.tab2kg.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import com.google.common.collect.Sets;

import de.l3s.simpleml.tab2kg.data.semtab.SemTabTableCreator;
import de.l3s.simpleml.tab2kg.evaluation.EvaluationInstance;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;
import de.l3s.simpleml.tab2kg.rml.ColumnLiteralMapping;
import de.l3s.simpleml.tab2kg.rml.RMLMappingReader;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Source;

public class TableGraphPairsFinder {

	private static final double TRAIN_SPLIT = 0.9;

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

	}

	private Set<String> gsClasses;

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

}
