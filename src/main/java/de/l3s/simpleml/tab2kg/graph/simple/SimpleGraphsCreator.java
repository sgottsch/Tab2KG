package de.l3s.simpleml.tab2kg.graph.simple;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import de.l3s.simpleml.tab2kg.graph.TypeGraphBuilder;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Mode;
import de.l3s.simpleml.tab2kg.util.TimeLogger;

public class SimpleGraphsCreator {

	private static final double TRAINING_PERCENTAGE = 0.8;

	private static final int MINIMUM_NUMBER_OF_LINES = 50;

	public static void main(String[] args) {
		SimpleGraphsCreator.runForFolder(Config.getPath(FileLocation.SELECTED_FILES));

		SimpleGraphsCreator.createTrainingAndTestSet();
	}

	public static void runForFolder(String folderPath) {

		System.out.println("Create simple graphs for folder " + folderPath);

		int i = 0;
		for (File file : new File(folderPath).listFiles()) {
			String simpleSchemaFileName = Config.getPath(FileLocation.SIMPLE_GRAPHS_ALL_FOLDER) + "simple_"
					+ file.getName() + ".ttl";

			if (file.isDirectory())
				continue;

			i += 1;

			System.out.println(TimeLogger.getTime() + " - File " + i + ": " + simpleSchemaFileName);

			TypeGraphBuilder graphBuilder = new TypeGraphBuilder();

			boolean successfull = false;
			try {
				successfull = graphBuilder.initModel(file.getAbsolutePath(), false);
			} catch (Exception e) {
				successfull = false;
			}

			if (!successfull) {
				System.out.println("Failed: " + file.getName());
				continue;
			}

			int numberOfLines = graphBuilder.createSimpleRDFGraph(simpleSchemaFileName);

			if (numberOfLines < MINIMUM_NUMBER_OF_LINES) {
				System.out.println("Failed (not enough lines): " + file.getName());
				(new File(simpleSchemaFileName)).delete();
			} else
				System.out.println("Successful: " + file.getName());

		}
		System.out.println("\nFinished.");

		deleteDuplicateFiles(Config.getPath(FileLocation.SIMPLE_GRAPHS_ALL_FOLDER));
	}

	private static void deleteDuplicateFiles(String folderPath) {
		System.out.println("Delete duplicates from " + folderPath + ".");

		boolean changed = true;
		while (changed) {
			changed = false;
			File fileToDelete = null;
			filesLoop: for (File file1 : new File(folderPath).listFiles()) {
				if (file1.isDirectory())
					continue;
				for (File file2 : new File(folderPath).listFiles()) {
					if (file2.isDirectory())
						continue;
					if (file1 == file2)
						continue;
					if (file1.getName().equals(file2.getName()))
						continue;
					try {
						if (FileUtils.contentEquals(file1, file2)) {
							System.out.println("Equal files:");
							System.out.println(" " + file1.getName());
							System.out.println(" " + file2.getName());
							fileToDelete = file2;
							break filesLoop;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			if (fileToDelete != null) {
				System.out.println("Delete " + fileToDelete.getName() + ".");
				fileToDelete.delete();
				changed = true;
			}
		}

	}

	/**
	 * Given the folder of simple graphs, randomly split the files into two folders
	 * (training and test set)
	 **/
	public static void createTrainingAndTestSet() {
		List<File> allFiles = new ArrayList<File>();

		for (File file : (new File(Config.getPath(FileLocation.SIMPLE_GRAPHS_ALL_FOLDER))).listFiles()) {
			if (!file.isDirectory())
				allFiles.add(file);
		}

		Collections.shuffle(allFiles);

		Set<File> trainingSet = new HashSet<File>();
		Set<File> testSet = new HashSet<File>();

		int sizeOfTraining = (int) Math.ceil(TRAINING_PERCENTAGE * allFiles.size());
		for (int i = 0; i <= sizeOfTraining; i++)
			trainingSet.add(allFiles.remove(0));
		testSet.addAll(allFiles);

		for (File file : trainingSet) {
			try {
				FileUtils.moveFile(file,
						new File(Config.getPath(FileLocation.SIMPLE_GRAPHS_FOLDER, Mode.TRAINING) + file.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (File file : testSet) {
			try {
				FileUtils.moveFile(file,
						new File(Config.getPath(FileLocation.SIMPLE_GRAPHS_FOLDER, Mode.TEST) + file.getName()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
