package de.l3s.simpleml.tab2kg.data.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.l3s.simpleml.tab2kg.util.TimeLimitedCodeBlock;
import de.l3s.simpleml.tab2kg.util.TimeLogger;

public class GitHubTablesCreatorExecutor implements Runnable {

	private int threadNumber;
	private int numberOfFiles;
	private String folderPath;
	private String processedFileName;
	private List<File> files;

	public GitHubTablesCreatorExecutor(List<File> files, String processedFileName, String folderPath, int numberOfFiles,
			int batchNumber) {
		this.files = files;
		this.processedFileName = processedFileName;
		this.folderPath = folderPath;
		this.numberOfFiles = numberOfFiles;
		this.threadNumber = batchNumber;

		System.out.println("Thread " + batchNumber + ": " + files.size() + "\t" + processedFileName + "\t" + folderPath
				+ "\t" + numberOfFiles);
	}

	@Override
	public void run() {
		processedFileName = processedFileName.replace("$batchNumber$", String.valueOf(threadNumber));

		try {
			final PrintWriter processedFilesWriter = new PrintWriter(processedFileName + "_" + threadNumber);
			processedFilesWriter.write(folderPath + "\t" + TimeLogger.getDateTime() + "\n");

			for (File file : files) {
				boolean successfull = run(3, file, numberOfFiles, processedFilesWriter, threadNumber);
				if (!successfull) {
					run(2, file, numberOfFiles, processedFilesWriter, threadNumber);
				}
				processedFilesWriter.flush();
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

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
						System.out.println("Error when creating tables (in executor): " + e.getMessage());
						e.printStackTrace();
						System.out.println("Skip file " + file.getAbsolutePath() + " - " + TimeLogger.getTime());
						processedFilesWriter.write("Error\t" + e.toString() + "\t" + TimeLogger.getDateTime() + "\n");
					}
					sgc = null;
					System.out.println(System.currentTimeMillis() - ms);
				}
			}, GitHubTablesCreator.MAX_TIME_IN_SECONDS, TimeUnit.SECONDS);
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

}
