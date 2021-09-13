package de.l3s.simpleml.tab2kg.evaluation.baselines.dsl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import de.l3s.simpleml.tab2kg.data.PairsLoader;
import de.l3s.simpleml.tab2kg.data.semtab.SemTabTableCreator;
import de.l3s.simpleml.tab2kg.evaluation.EvaluationInstance;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Mode;
import de.l3s.simpleml.tab2kg.util.Source;

public class DSLTrainTestSetsCreator {

	public static void main(String[] args) {

		String outputFolder = args[0];
		if (!outputFolder.endsWith("/"))
			outputFolder = outputFolder + "/";

		Source dataset = Source.valueOf(args[1]);

		boolean useDomainGraphs = args[2].toLowerCase().equals("true");

		createFolders(outputFolder, dataset, useDomainGraphs);
	}

	public static void createFolders(String outputFolder, Source dataset, boolean useDomainGraphs) {

		String modelsFolder = SemTabTableCreator.FOLDER_NAME_MODELS;
		if (useDomainGraphs)
			modelsFolder = SemTabTableCreator.FOLDER_NAME_DOMAIN_ONTOLOGY_MODELS;

		String tablesFolder = SemTabTableCreator.FOLDER_NAME_TABLES;
		if (useDomainGraphs)
			tablesFolder = SemTabTableCreator.FOLDER_NAME_DOMAIN_ONTOLOGY_TABLES;

		String suffix = "p";
		if (useDomainGraphs)
			suffix = "d";

		String folder = Config.getPath(FileLocation.BASE_FOLDER) + dataset.getFolderName();
		if (dataset == Source.SEMTAB_EASY)
			folder = Config.getPath(FileLocation.BASE_FOLDER) + Source.SEMTAB.getFolderName();

		try {
			Files.createDirectories(Paths.get(outputFolder));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Map<String, String> dataSetIds = new HashMap<String, String>();
		int curDatasetId = 0;

		PrintWriter infoFileWriter = null;
		try {
			infoFileWriter = new PrintWriter(
					outputFolder + "info_" + dataset.toString().toLowerCase() + "_" + suffix + ".txt");

			int pairId = 0;

			List<EvaluationInstance> evaluationInstances = PairsLoader.loadPairs(dataset, Mode.TEST, useDomainGraphs);
			System.out.println("evaluationInstances: " + evaluationInstances.size());

			// if (dataset == EvaluationDataSet.GHODP)
//				evaluationInstances = GHODPInstancesLoader.collectionEvaluationInstances(Mode.TEST);
//			else
//			TableGraphPairsFinder tgpf = new TableGraphPairsFinder();
//			evaluationInstances = tgpf.collectPairs(folder, null);

			// Collections.shuffle(evaluationInstances);

			for (EvaluationInstance evaluationInstance : evaluationInstances) {

				String idTraining = evaluationInstance.getGraphFileName()
						.substring(evaluationInstance.getGraphFileName().lastIndexOf("/") + 1);
				idTraining = idTraining.replaceAll(".ttl$", "").replace(".csv", "");

				String idTrainingFolder = dataSetIds.get(idTraining);

				System.out.println("idTrainingFolder: " + idTrainingFolder + " - " + idTraining);

				if (idTrainingFolder == null) {
					idTrainingFolder = outputFolder + dataset.toString().toLowerCase() + "_" + suffix + "_"
							+ curDatasetId + "/";
					new File(idTrainingFolder).mkdirs();
					dataSetIds.put(idTraining, idTrainingFolder);
					curDatasetId += 1;

					new File(idTrainingFolder + SemTabTableCreator.FOLDER_NAME_TABLES + "/").mkdirs();
					new File(idTrainingFolder + SemTabTableCreator.FOLDER_NAME_MODELS + "/").mkdirs();

					try {
						FileUtils.copyFile(
								new File(folder + modelsFolder + "/" + idTraining
										+ ".csv.model.json"),
								new File(idTrainingFolder + SemTabTableCreator.FOLDER_NAME_MODELS
										+ "/train.csv.model.json"));
						FileUtils.copyFile(
								new File(folder + tablesFolder + "/" + idTraining + ".csv"),
								new File(idTrainingFolder + SemTabTableCreator.FOLDER_NAME_TABLES + "/train.csv"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				String idTest = evaluationInstance.getTableFileName()
						.substring(evaluationInstance.getTableFileName().lastIndexOf("/") + 1);
				idTest = idTest.replaceAll(".ttl$", "").replace(".csv", "");

				String idTestFolder = dataSetIds.get(idTest);

				System.out.println("idTestFolder: " + idTestFolder + " - " + idTest);

				if (idTestFolder == null) {
					idTestFolder = outputFolder + dataset.toString().toLowerCase() + "_" + suffix + "_" + curDatasetId
							+ "/";
					new File(idTestFolder).mkdirs();
					dataSetIds.put(idTest, idTestFolder);
					curDatasetId += 1;

					new File(idTestFolder + SemTabTableCreator.FOLDER_NAME_TABLES + "/").mkdirs();
					new File(idTestFolder + SemTabTableCreator.FOLDER_NAME_MODELS + "/").mkdirs();

					try {
						FileUtils.copyFile(
								new File(folder + SemTabTableCreator.FOLDER_NAME_MODELS + "/" + idTest
										+ ".csv.model.json"),
								new File(
										idTestFolder + SemTabTableCreator.FOLDER_NAME_MODELS + "/test.csv.model.json"));
						FileUtils.copyFile(
								new File(folder + SemTabTableCreator.FOLDER_NAME_TABLES + "/" + idTest + ".csv"),
								new File(idTestFolder + SemTabTableCreator.FOLDER_NAME_TABLES + "/test.csv"));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				String idTrainingFolderShort = idTrainingFolder.substring(0, idTrainingFolder.lastIndexOf("/"));
				idTrainingFolderShort = idTrainingFolderShort.substring(idTrainingFolderShort.lastIndexOf("/") + 1);

				String idTestFolderShort = idTestFolder.substring(0, idTestFolder.lastIndexOf("/"));
				idTestFolderShort = idTestFolderShort.substring(idTestFolderShort.lastIndexOf("/") + 1);

				infoFileWriter.println(pairId + " " + idTrainingFolderShort + " " + idTestFolderShort + " "
						+ idTrainingFolder + " " + idTestFolder + " " + idTraining + " " + idTest);

//				String pairFolderTrain = "";
//				String pairFolderTest = "";
//
//				new File(pairFolderTrain).mkdirs();
//				new File(pairFolderTest).mkdirs();
//
//				new File(pairFolderTrain + "data_cleaned/").mkdirs();
//				new File(pairFolderTrain + "model_cleaned/").mkdirs();
//
//				new File(pairFolderTest + "data_cleaned/").mkdirs();
//				new File(pairFolderTest + "model_cleaned/").mkdirs();
//
//				try {
//					FileUtils.copyFile(new File(folder + "model_cleaned/" + idTraining.replace(".ttl", ".model.json")),
//							new File(pairFolderTrain + "/model_cleaned/" + idTraining.replace(".ttl", ".model.json")));
//					FileUtils.copyFile(new File(folder + "data_cleaned/" + idTraining.replace(".ttl", "")),
//							new File(pairFolderTrain + "data_cleaned/" + idTraining.replace(".ttl", "")));
//					FileUtils.copyFile(new File(folder + "model_cleaned/" + idTest + ".model.json"),
//							new File(pairFolderTest + "model_cleaned/" + idTest + ".model.json"));
//					FileUtils.copyFile(new File(folder + "data_cleaned/" + idTest),
//							new File(pairFolderTest + "data_cleaned/" + idTest));
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				if (pairId == 250)
//					break;
//
				pairId += 1;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			infoFileWriter.close();
		}

		if (dataset == Source.GITHUB)
			createTrainingDataSet(Source.GITHUB, folder, outputFolder, useDomainGraphs);
	}

	private static void createTrainingDataSet(Source source, String folder, String outputFolder,
			boolean useDomainGraphs) {

		List<EvaluationInstance> evaluationInstances = PairsLoader.loadPairs(source, Mode.TRAINING, useDomainGraphs);
		System.out.println("training evaluation instances: " + evaluationInstances.size());

		String testFolderTables = outputFolder + source.toString().toLowerCase() + "_train/"
				+ SemTabTableCreator.FOLDER_NAME_TABLES + "/";
		String testFolderModels = outputFolder + source.toString().toLowerCase() + "_train/"
				+ SemTabTableCreator.FOLDER_NAME_MODELS + "/";
		new File(testFolderTables).mkdirs();
		new File(testFolderModels).mkdirs();

		for (EvaluationInstance evaluationInstance : evaluationInstances) {

			String idTraining = evaluationInstance.getTableFileName()
					.substring(evaluationInstance.getTableFileName().lastIndexOf("/") + 1);
			idTraining = idTraining.replaceAll(".ttl$", "").replace(".csv", "");

			try {
				FileUtils.copyFile(
						new File(folder + SemTabTableCreator.FOLDER_NAME_MODELS + "/" + idTraining + ".csv.model.json"),
						new File(testFolderModels + "/" + idTraining + ".csv.model.json"));
				FileUtils.copyFile(new File(folder + SemTabTableCreator.FOLDER_NAME_TABLES + "/" + idTraining + ".csv"),
						new File(testFolderTables + "/" + idTraining + ".csv"));
			} catch (IOException e) {
				System.out.println("File missing: " + idTraining);
				// e.printStackTrace();
			}
		}

	}

}
