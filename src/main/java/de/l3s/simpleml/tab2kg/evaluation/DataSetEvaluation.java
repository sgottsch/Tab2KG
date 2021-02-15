package de.l3s.simpleml.tab2kg.evaluation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import de.l3s.simpleml.tab2kg.data.PairsLoader;
import de.l3s.simpleml.tab2kg.ml.TableSemantifier;
import de.l3s.simpleml.tab2kg.ml.baseline.DSLConfidencesLoader;
import de.l3s.simpleml.tab2kg.ml.baseline.T2KMatchConfidencesLoader;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Mode;
import de.l3s.simpleml.tab2kg.util.Source;

public class DataSetEvaluation {

	@Parameter(names = { "-source" }, required = true)
	private Source source;

	@Parameter(names = { "-mode" }, required = false)
	private Mode mode;

	@Parameter(names = { "-pairs" }, required = false)
	private Integer numberOfPairs;

	@Parameter(names = { "-target" }, required = false)
	private Integer targetPairId;

	private List<EvaluationInstance> evaluationInstances;

	@Parameter(names = { "-dslPairsFile" })
	private String dslPairsFile;

	@Parameter(names = { "-dslOutputFolder" })
	private String dslOutputFolder;

	@Parameter(names = { "-t2kClassesFile" })
	private String t2kClassesFile;

	@Parameter(names = { "-t2kPropertiesFile" })
	private String t2kPropertiesFile;

	@Parameter(names = { "-comment" })
	private String comment;

	public static void main(String[] args) {

		DataSetEvaluation evaluation = new DataSetEvaluation();
		JCommander.newBuilder().addObject(evaluation).build().parse(args);

		evaluation.collectData();
		evaluation.evaluate();
	}

	public void collectData() {

		if (mode == null)
			mode = Mode.TEST;

		this.evaluationInstances = PairsLoader.loadPairs(source, mode);

		if (numberOfPairs != null)
			this.evaluationInstances = this.evaluationInstances.subList(0, numberOfPairs);

		System.out.println("Evaluation instances: " + this.evaluationInstances.size());
	}

	private void evaluate() {

		DSLConfidencesLoader dslConfidences = null;
		T2KMatchConfidencesLoader t2kConfidences = null;

		if (this.dslPairsFile != null) {
			dslConfidences = new DSLConfidencesLoader();
			dslConfidences.loadPairs(dslPairsFile, dslOutputFolder, source, this.evaluationInstances);
		} else if (this.t2kClassesFile != null) {
			t2kConfidences = new T2KMatchConfidencesLoader();
			t2kConfidences.loadPairs(t2kClassesFile, t2kPropertiesFile, source);
		}

		boolean firstColumnHasRowNumber = true;
		if (source == Source.SEMTAB || source == Source.SEMTAB_EASY)
			firstColumnHasRowNumber = false;

		System.out.println("firstColumnHasRowNumber: " + firstColumnHasRowNumber);

		// PrintWriter writerScores = null;
		PrintWriter writer = null;
		try {

			String greedy = "";
			String dsl = "";
			if (this.dslPairsFile != null)
				dsl = "_dsl";
			String t2k = "";
			if (this.t2kClassesFile != null)
				t2k = "_t2k";
			String comment = "";
			if (this.comment != null)
				comment = "_" + this.comment;

			writer = new PrintWriter(Config.getPath(FileLocation.RESULTS) + this.source.getName() + greedy + dsl + t2k
					+ comment + ".txt");

			EvaluationResult totalEvaluationResult = new EvaluationResult();
			int i = 0;
			for (EvaluationInstance evaluationInstance : this.evaluationInstances) {

				if (targetPairId != null && i != targetPairId) {
					i += 1;
					continue;
				}

				if (evaluationInstance.getId() != null)
					System.out.println(evaluationInstance.getId());

				String line1 = "--- " + i + "/" + this.evaluationInstances.size() + " ("
						+ ((double) i / (double) this.evaluationInstances.size()) + ") ---";
				String line2 = evaluationInstance.getTableFileName() + " / " + evaluationInstance.getGraphFileName();

				System.out.println(line1);
				System.out.println(line2);

				TableSemantifier tableSemantifier = new TableSemantifier(evaluationInstance, this.dslPairsFile != null,
						firstColumnHasRowNumber);
				tableSemantifier.setDSLConfidences(dslConfidences);
				tableSemantifier.setT2KConfidences(t2kConfidences);

				boolean valid = tableSemantifier.run();
				if (!valid)
					continue;

				totalEvaluationResult.update(evaluationInstance.getResult());

				writer.println(line1);
				writer.println(line2);
				totalEvaluationResult.print();
				totalEvaluationResult.print(writer);
				writer.println("");
				writer.flush();

				i += 1;
			}

			System.out.println("--- FINAL ---");
			totalEvaluationResult.print();
			totalEvaluationResult.print(writer);
			System.out.println("Concept relations: " + totalEvaluationResult.getNumberOfClassRelations());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}

	}

}
