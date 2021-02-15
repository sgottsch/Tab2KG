package de.l3s.simpleml.tab2kg.data.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;

import de.l3s.simpleml.tab2kg.data.semtab.SemTabTableCreator;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Mode;
import de.l3s.simpleml.tab2kg.util.TimeLimitedCodeBlock;
import de.l3s.simpleml.tab2kg.util.TimeLogger;

public class GitHubTablesCreator {

	static final int MAX_TIME_IN_SECONDS = 60 * 5;

	public static void main(String[] args) {

		try {
			Files.createDirectories(
					Paths.get(Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_GRAPHS));
			Files.createDirectories(
					Paths.get(Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_MAPPINGS));
			Files.createDirectories(
					Paths.get(Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_MODELS));
			Files.createDirectories(
					Paths.get(Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_TABLES));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (Config.isLocal()) {
			List<String> fileNames = new ArrayList<String>();
			fileNames.add(""); // anonymized
			GitHubTablesCreator.runForFileNamesLocal(fileNames, 1);
		}

		else {
			List<String> missingFileNames = getMissingFiles();
			int numberOfThreads = 6;

			GitHubTablesCreator.runForFolder(Config.getPath(FileLocation.GITHUB_FILES_FOLDER), Config
					.getPath(FileLocation.PROCESSED_FILES_FILE).replace("$time$", TimeLogger.getFileNameDateTime()), 8,
					numberOfThreads, missingFileNames);
		}

	}

	public static void runForFolder(String folderPath, String processedFileName, int numberOfFiles, int numberOfThreads,
			List<String> missingFileNames) {

		System.out.println("Process folder " + folderPath);

		List<List<File>> files = new ArrayList<List<File>>(numberOfThreads);

		for (int i = 0; i < numberOfThreads; i++)
			files.add(new ArrayList<File>());

		int totalNumberOfFilesToProcess = 0;
		int i = 0;
		for (File file : new File(folderPath).listFiles()) {

			if (!missingFileNames.contains(file.getName())) {
				System.out.println("Skip " + file.getName() + " (was done before).");
				continue;
			}
			totalNumberOfFilesToProcess += 1;
			i += 1;
			if (i == numberOfThreads)
				i = 0;
			files.get(i).add(file);
		}

		System.out.println("Numer of files to process: " + totalNumberOfFilesToProcess + ".");

		ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

		for (int threadNumber = 0; threadNumber < numberOfThreads; threadNumber++) {
			List<File> threadFiles = files.get(threadNumber);
			Runnable worker = new GitHubTablesCreatorExecutor(threadFiles, processedFileName, folderPath, numberOfFiles,
					threadNumber);
			executor.execute(worker);
		}
		executor.shutdown();
		while (executor.isTerminated() == false) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
		System.out.println("\nFinished all threads");

	}

	private static boolean run(int numberOfTemplateNodes, File file, int numberOfFiles,
			PrintWriter processedFilesWriter, int batchNumber) {

		System.out.println(
				"=== File: " + file.getAbsolutePath() + ", " + numberOfTemplateNodes + " - " + TimeLogger.getTime());
		processedFilesWriter.write(
				"\n" + file.getAbsolutePath() + "\t" + TimeLogger.getDateTime() + "\t" + numberOfTemplateNodes + "\n");
		processedFilesWriter.flush();

		try {
			TimeLimitedCodeBlock.runWithTimeout(new Runnable() {
				@Override
				public void run() {
					GitHubGraphToTablesTransformer sgc = new GitHubGraphToTablesTransformer();
					long ms = System.currentTimeMillis();
					try {
						int numberOfCreatedFiles = sgc.createTables(file.getAbsolutePath(), numberOfFiles,
								numberOfTemplateNodes, batchNumber);
						System.out.println("Created " + numberOfCreatedFiles + " files for " + file.getAbsolutePath()
								+ " - " + TimeLogger.getTime());
						processedFilesWriter
								.write("Success\t" + numberOfCreatedFiles + "\t" + TimeLogger.getDateTime() + "\n");
					} catch (Exception e) {
						System.out.println("Error when creating creating tables (1): " + e.getMessage());
						e.printStackTrace();
						System.out.println("Skip file " + file.getAbsolutePath() + " - " + TimeLogger.getTime());
						processedFilesWriter.write("Error\t" + e.toString() + "\t" + TimeLogger.getDateTime() + "\n");
					}
					sgc = null;
					System.out.println(System.currentTimeMillis() - ms);
				}
			}, MAX_TIME_IN_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			processedFilesWriter.write("Timeout (" + numberOfTemplateNodes + ") \t" + TimeLogger.getDateTime() + "\n");
			return false;
		} catch (Exception e) {
			processedFilesWriter.write(
					"Error (" + numberOfTemplateNodes + ") \t" + e.toString() + "\t" + TimeLogger.getDateTime() + "\n");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static void runForFileNamesLocal(List<String> fileNames, int numberOfFiles) {
		for (String fileName : fileNames) {
			System.out.println("=== File: " + fileName);
			GitHubGraphToTablesTransformer sgc = new GitHubGraphToTablesTransformer();
			long ms = System.currentTimeMillis();
			try {
				int numberOfCreatedFiles = sgc.createTables(fileName, numberOfFiles, 3, 0);
				System.out.println("Created " + numberOfCreatedFiles + " files.");
			} catch (Exception e) {
				System.out.println("Error when creating tables (2): " + e.getMessage());
				e.printStackTrace();
				continue;
			}
			System.out.println("Time: " + (System.currentTimeMillis() - ms));
		}
	}

	public static void runForFiles(Mode mode, Set<File> files, String processedFileName, String folderPath,
			int numberOfFiles, int batchNumber) {

		processedFileName = processedFileName.replace("$batchNumber$", String.valueOf(batchNumber));

		try {
			final PrintWriter processedFilesWriter = new PrintWriter(processedFileName + "_" + batchNumber);
			processedFilesWriter.write(folderPath + "\t" + TimeLogger.getDateTime() + "\n");

			for (File file : files) {
				boolean successfull = run(3, file, 10, processedFilesWriter, batchNumber);
				if (!successfull) {
					run(2, file, 10, processedFilesWriter, batchNumber);
				}
				processedFilesWriter.flush();
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

	}

	public static List<String> getMissingFiles() {

		// load processed files
		String folderOfProcessedFileNames = Config.getPath(FileLocation.PROCESSED_FILES_FILE).substring(0,
				Config.getPath(FileLocation.PROCESSED_FILES_FILE).lastIndexOf("/"));
		String processedFileNamesPrefixes = Config.getPath(FileLocation.PROCESSED_FILES_FILE)
				.substring(Config.getPath(FileLocation.PROCESSED_FILES_FILE).lastIndexOf("/") + 1);
		processedFileNamesPrefixes = processedFileNamesPrefixes.substring(0, processedFileNamesPrefixes.indexOf("$"));

		System.out.println(folderOfProcessedFileNames + "---->" + processedFileNamesPrefixes);

		Set<String> processedFileNames = new HashSet<String>();

		for (File file : (new File(folderOfProcessedFileNames).listFiles())) {
			if (!file.isDirectory() && file.getName().startsWith(processedFileNamesPrefixes))
				loadProcessedFileNames(file, processedFileNames);
		}

		System.out.println("#processed: " + processedFileNames.size());

		Map<String, File> simpleGraphsFileNames = new HashMap<String, File>();
		for (File file : (new File(Config.getPath(FileLocation.GITHUB_FILES_FOLDER)).listFiles())) {
			String fileName = file.getName();
			if (!file.isDirectory() && fileName.endsWith(".ttl")) {
				fileName = fileName.substring(0, fileName.length() - 4); // remove
																			// ".ttl"
				System.out.println("File: " + fileName);
				simpleGraphsFileNames.put(fileName, file);
			}
		}

		System.out.println("#Files: " + simpleGraphsFileNames.size());

		Map<String, Integer> processedGraphs = new HashMap<String, Integer>();
		for (File file : (new File(Config.getPath(FileLocation.GITHUB_FOLDER) + SemTabTableCreator.FOLDER_NAME_TABLES)
				.listFiles())) {
			String fileName = file.getName();

			fileName = fileName.substring(0, file.getName().lastIndexOf("_"));
			fileName = fileName.substring(0, fileName.lastIndexOf("_"));

			System.out.println("Processed: " + fileName);

			processedFileNames.add(fileName);

			if (!processedGraphs.containsKey(fileName))
				processedGraphs.put(fileName, 1);
			else
				processedGraphs.put(fileName, processedGraphs.get(fileName) + 1);
		}

		int errors = 0;
		for (String processedGraph : processedGraphs.keySet()) {
			if (!simpleGraphsFileNames.containsKey(processedGraph)) {
				errors += 1;
				System.out.println("Missing " + processedGraph);
			}
			System.out.println(processedGraphs.get(processedGraph) + ": " + processedGraph);
		}

		System.out.println("#Errors: " + errors);
		System.out.println("#Simple graphs: " + simpleGraphsFileNames.size());
		System.out.println("#Processed graphs: " + processedGraphs.size());
		System.out.println("#Processed files: " + processedFileNames.size());

		List<String> missingFileNames = new ArrayList<String>();
		for (String simpleGraphFileName : simpleGraphsFileNames.keySet()) {
			if (!processedFileNames.contains(simpleGraphFileName)) {
				missingFileNames.add(simpleGraphsFileNames.get(simpleGraphFileName).getName());
				System.out.println("Missing " + simpleGraphsFileNames.get(simpleGraphFileName).getName());
			}
		}

		System.out.println("To do: " + missingFileNames.size());
		return missingFileNames;
	}

	private static void loadProcessedFileNames(File file, Set<String> processedFileNames) {
		System.out.println("Load lines: " + file.getName());
		try {
			for (String line : FileUtils.readLines(file, "UTF-8")) {
				if (line.startsWith("/")) {
					String fileName = line.split("\t")[0];

					fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
					if (fileName.trim().isEmpty())
						continue;
					fileName = fileName.substring(0, fileName.length() - 4); // remove ".ttl"

					processedFileNames.add(fileName);
					System.out.println("Processed (according to log file): " + fileName);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
