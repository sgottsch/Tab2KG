package de.l3s.simpleml.tab2kg.evaluation;

import java.io.PrintWriter;

public class EvaluationResult {

	private int correctLiteralRelations = 0;
	private int wrongLiteralRelations = 0;
	private Integer correctClassRelations = null;
	private Integer wrongClassRelations = null;
	private Integer missingClassRelations = 0;

	private int numberOfClassRelations = 0;

	private int correctGraphs = 0;
	private int wrongGraphs = 0;

	public int getCorrectLiteralRelations() {
		return correctLiteralRelations;
	}

	public void setCorrectLiteralRelations(int correctLiteralRelations) {
		this.correctLiteralRelations = correctLiteralRelations;
	}

	public void incrementCorrectLiteralRelations() {
		this.correctLiteralRelations += 1;
	}

	public void incrementCorrectLiteralRelations(int inc) {
		this.correctLiteralRelations += inc;
	}

	public int getWrongLiteralRelations() {
		return wrongLiteralRelations;
	}

	public void setWrongLiteralRelations(int wrongLiteralRelations) {
		this.wrongLiteralRelations = wrongLiteralRelations;
	}

	public void incrementWrongLiteralRelations() {
		this.wrongLiteralRelations += 1;
	}

	public void incrementWrongLiteralRelations(int inc) {
		this.wrongLiteralRelations += inc;
	}

	public void incrementMissingClassRelations(int inc) {
		this.missingClassRelations += inc;
	}

	public Integer getCorrectClassRelations() {
		return correctClassRelations;
	}

	public void setCorrectClassRelations(int correctClassRelations) {
		this.correctClassRelations = correctClassRelations;
	}

	public Integer getMissingClassRelations() {
		return missingClassRelations;
	}

	public void setMissingClassRelations(Integer missingClassRelations) {
		this.missingClassRelations = missingClassRelations;
	}

	public void increaseMissingClassRelations(int inc) {
		if (missingClassRelations == null)
			missingClassRelations = 0;
		this.missingClassRelations += 1;
	}

	public void incrementCorrectClassRelations() {
		if (this.correctClassRelations == null) {
			this.correctClassRelations = 0;
			this.wrongClassRelations = 0;
		}
		this.correctClassRelations += 1;
	}

	public void incrementCorrectClassRelations(int inc) {
		if (this.correctClassRelations == null) {
			this.correctClassRelations = 0;
			this.wrongClassRelations = 0;
		}
		this.correctClassRelations += inc;
	}

	public Integer getWrongClassRelations() {
		return wrongClassRelations;
	}

	public void setWrongClassRelations(int wrongClassRelations) {
		this.wrongClassRelations = wrongClassRelations;
	}

	public void incrementWrongClassRelations() {
		if (this.wrongClassRelations == null) {
			this.correctClassRelations = 0;
			this.wrongClassRelations = 0;
		}
		this.wrongClassRelations += 1;
	}

	public void incrementWrongClassRelations(int inc) {
		if (this.wrongClassRelations == null) {
			this.correctClassRelations = 0;
			this.wrongClassRelations = 0;
		}
		this.wrongClassRelations += inc;
	}

	public double getLiteralRelationsPrecision() {
		return (double) this.correctLiteralRelations / (this.correctLiteralRelations + this.wrongLiteralRelations);
	}

	public Double getClassRelationsPrecision() {
		if (this.correctClassRelations == null || this.correctClassRelations + this.wrongClassRelations == 0)
			return null;
		return (double) this.correctClassRelations
				/ (this.correctClassRelations + this.wrongClassRelations + this.missingClassRelations);
	}

	public Double getPrecision() {

		int correctClassRelationsOr0 = 0;
		if (this.correctClassRelations != null)
			correctClassRelationsOr0 = this.correctClassRelations;

		int wrongClassRelationsOr0 = 0;
		if (this.wrongClassRelations != null)
			wrongClassRelationsOr0 = this.wrongClassRelations;

		return (double) (correctClassRelationsOr0 + this.correctLiteralRelations)
				/ (correctClassRelationsOr0 + wrongClassRelationsOr0 + this.missingClassRelations
						+ this.correctLiteralRelations + this.wrongLiteralRelations);

	}

	public void update(SingleEvaluationResult otherEvaluationResult) {

		incrementCorrectLiteralRelations(otherEvaluationResult.getCorrectLiteralRelations());
		incrementWrongLiteralRelations(otherEvaluationResult.getWrongLiteralRelations());
		incrementMissingClassRelations(otherEvaluationResult.getMissingClassRelations());

		if (otherEvaluationResult.getCorrectClassRelations() != null) {
			incrementCorrectClassRelations(otherEvaluationResult.getCorrectClassRelations());
		}

		if (otherEvaluationResult.getWrongClassRelations() != null) {
			incrementWrongClassRelations(otherEvaluationResult.getWrongClassRelations());
		}

		if (otherEvaluationResult.isCorrect())
			this.correctGraphs += 1;
		else
			this.wrongGraphs += 1;

		this.numberOfClassRelations += otherEvaluationResult.getNumberOfClassRelations();
	}

	public void print() {
		print(null);
	}

	public void print(PrintWriter writer) {

		String line1 = "Literals: " + this.correctLiteralRelations + " / "
				+ (this.correctLiteralRelations + this.wrongLiteralRelations) + " -> "
				+ 100d * this.getLiteralRelationsPrecision() + "%";
		String line2 = null;
		// String line2b = null;
		if (this.correctClassRelations != null && this.wrongClassRelations != null) {
			line2 = "Relations: " + this.correctClassRelations + " / "
					+ (this.correctClassRelations + this.wrongClassRelations) + " -> "
					+ 100d * this.getClassRelationsPrecision() + "%";
		}
//		if (this.correctClassRelations != null && this.wrongClassRelations != null) {
//			line2b = "Relations: " + this.correctClassRelations + " / " + (this.numberOfClassRelations) + " -> "
//					+ 100d * this.getClassRelationsPrecision() + "%";
//		}

		String line3 = "Correct graphs: " + this.correctGraphs + ", wrong graphs: " + this.wrongGraphs;
		String line4 = "Precision: " + getPrecision();

		if (writer == null) {
			System.out.println(line1);
			if (line2 != null)
				System.out.println(line2);
			System.out.println(line3);
			System.out.println(line4);
		} else {
			writer.println(line1);
			if (line2 != null)
				writer.println(line2);
			writer.println(line3);
			writer.println(line4);
		}

	}

	public int getCorrectGraphs() {
		return correctGraphs;
	}

	public void setCorrectGraphs(int correctGraphs) {
		this.correctGraphs = correctGraphs;
	}

	public int getWrongGraphs() {
		return wrongGraphs;
	}

	public void setWrongGraphs(int wrongGraphs) {
		this.wrongGraphs = wrongGraphs;
	}

	public int getNumberOfClassRelations() {
		return numberOfClassRelations;
	}

	public void setNumberOfClassRelations(int numberOfClassRelations) {
		this.numberOfClassRelations = numberOfClassRelations;
	}

}
