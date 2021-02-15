package de.l3s.simpleml.tab2kg.evaluation;

public class EvaluationInstance {

	private String id;

	private String tableFileName;
	private String graphFileName;
	private String mappingFileName;

	private SingleEvaluationResult evaluationResult;

	public EvaluationInstance(String tableFileName, String graphFileName, String mappingFileName) {
		super();
		this.tableFileName = tableFileName;
		this.graphFileName = graphFileName;
		this.mappingFileName = mappingFileName;
	}

	public String getTableFileName() {
		return tableFileName;
	}

	public String getMappingFileName() {
		return mappingFileName;
	}

	public String getGraphFileName() {
		return graphFileName;
	}

	public void setEvaluationResult(SingleEvaluationResult evaluationResult) {
		this.evaluationResult = evaluationResult;
	}

	public SingleEvaluationResult getResult() {
		return evaluationResult;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
