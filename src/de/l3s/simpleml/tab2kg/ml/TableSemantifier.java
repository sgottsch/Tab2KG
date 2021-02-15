package de.l3s.simpleml.tab2kg.ml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.datareader.DataTableProfilesCreator;
import de.l3s.simpleml.tab2kg.datareader.DataTableReader;
import de.l3s.simpleml.tab2kg.evaluation.EvaluationInstance;
import de.l3s.simpleml.tab2kg.evaluation.SingleEvaluationResult;
import de.l3s.simpleml.tab2kg.evaluation.TableMappingEvaluator;
import de.l3s.simpleml.tab2kg.graph.simple.SimpleGraphProfilesCreator;
import de.l3s.simpleml.tab2kg.graphcandidates.AttributeCandidatesSet;
import de.l3s.simpleml.tab2kg.graphcandidates.CandidateGraphCreator;
import de.l3s.simpleml.tab2kg.graphcandidates.ColumnCandidate;
import de.l3s.simpleml.tab2kg.graphcandidates.SubGraphsFinder2;
import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraph;
import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraphEdge;
import de.l3s.simpleml.tab2kg.graphcandidates.model.DummyGraphNode;
import de.l3s.simpleml.tab2kg.ml.baseline.DSLConfidencesLoader;
import de.l3s.simpleml.tab2kg.ml.baseline.T2KMatchConfidencesLoader;
import de.l3s.simpleml.tab2kg.model.graph.CandidateGraph;
import de.l3s.simpleml.tab2kg.model.graph.MiniSchema;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;
import de.l3s.simpleml.tab2kg.profiles.FeatureConfig;
import de.l3s.simpleml.tab2kg.profiles.ProfilePairNormaliser;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeaturePlaceholder;

public class TableSemantifier {

	private boolean useDSLConfidences = false;

	public static final int NUMBER_OF_FEATURES = 83;

	public static final List<Integer> NUMBERS_OF_QUANTILES = Arrays.asList(10);
	public static final List<Integer> NUMBERS_OF_INTERVALS = Arrays.asList(10);

	public static final int MAX_NUMBER_OF_ATTRIBUTE_SETS = 25;
	public static final String NULL_VALUE = "";
	public static int MAX_NUMBER_OF_CANDIDATE_GRAPHS = 50;

	private String pythonFile = ""; // anonymized
	private String weightsFile = ""; // anonymized

	private EvaluationInstance evaluationInstance;

	private TableMappingEvaluator evaluator;

	private DSLConfidencesLoader dslConfidences;
	private T2KMatchConfidencesLoader t2kConfidences;

	private boolean firstColumnHasRowNumber;

	public TableSemantifier(EvaluationInstance evaluationInstance, boolean useDSLConfidences,
			boolean firstColumnHasRowNumber) {

		super();
		this.evaluationInstance = evaluationInstance;
		this.useDSLConfidences = useDSLConfidences;
		this.firstColumnHasRowNumber = firstColumnHasRowNumber;
	}

	public boolean run() {

		System.out.println("Run TableSemantifier. " + (new Date()) + ")");

		DataTable dataTable = DataTableReader.readDataTable(evaluationInstance.getTableFileName(), ",", true,
				this.firstColumnHasRowNumber, true, NULL_VALUE);

		System.out.println("GRAPH: " + evaluationInstance.getGraphFileName());

		SimpleGraph simpleGraph = new SimpleGraph(evaluationInstance.getGraphFileName());

		System.out.println("TABLE: " + evaluationInstance.getTableFileName());

		boolean valid = DataTableProfilesCreator.createColumnProfiles(dataTable, NUMBERS_OF_QUANTILES,
				NUMBERS_OF_INTERVALS);
		if (!valid) {
			System.out.println("Skip.");
			return false;
		}

		valid = SimpleGraphProfilesCreator.createAttributeProfiles(simpleGraph, NUMBERS_OF_QUANTILES,
				NUMBERS_OF_INTERVALS);
		if (!valid) {
			System.out.println("Skip.");
			return false;
		}

		if (simpleGraph.getMiniSchema().getLiteralTriples().size() > 50) {
			System.out.println(
					"Skip: " + simpleGraph.getMiniSchema().getLiteralTriples().size() + " data type relations.");
			return false;
		}

		this.evaluator = new TableMappingEvaluator(dataTable, simpleGraph);
		evaluator.annotateTable(evaluationInstance.getMappingFileName());

		CandidateGraph resultGraph = findCandidateGraphGreedy(dataTable, simpleGraph);

		System.out.println("=== Result Graph ===");
		resultGraph.print();

		SingleEvaluationResult evaluationResult = evaluator.evaluate(resultGraph, simpleGraph);
		evaluationInstance.setEvaluationResult(evaluationResult);

		System.out.println("correct literal relations: " + evaluationResult.getCorrectLiteralRelations());
		System.out.println("wrong literal relations: " + evaluationResult.getWrongLiteralRelations());
		System.out.println("correct class relations: " + evaluationResult.getCorrectClassRelations());
		System.out.println("wrong class relations: " + evaluationResult.getWrongClassRelations());
		System.out.println("missing class relations: " + evaluationResult.getMissingClassRelations());

		for (Attribute column : dataTable.getAttributes()) {
			if (column.getPredictedAttribute() != null)
				System.out.println(column.getIdentifier() + " -> " + column.getPredictedAttribute().getSubjectClassURI()
						+ " " + column.getPredictedAttribute().getPredicateURI());
			else
				System.out.println("No attribute for " + column.getIdentifier());
		}

		return true;
	}

	private CandidateGraph findCandidateGraphGreedy(DataTable dataTable, SimpleGraph simpleGraph) {
		List<Double> results = getConfidences(simpleGraph, dataTable);

		List<ColumnCandidate> candidateColumns = new ArrayList<ColumnCandidate>();
		for (Attribute column : dataTable.getAttributes()) {
			for (Attribute attribute : simpleGraph.getAttributes()) {

				if (this.dslConfidences == null && attribute.getStatistics().getAttributeStatisticsType()
						.getTypeL1() != column.getStatistics().getAttributeStatisticsType().getTypeL1())
					continue;

				Double result = results.remove(0);

				if (result == null)
					result = 0d;

				ColumnCandidate cc = new ColumnCandidate(column, attribute, result);

				System.out.println(column.getIdentifier() + " / " + attribute.getSubjectClassURI() + " "
						+ attribute.getPredicateURI() + " => " + result);

				candidateColumns.add(cc);
			}
		}

		Set<Attribute> assignedColumns = new HashSet<Attribute>();
		AttributeCandidatesSet acs = new AttributeCandidatesSet();
		Set<String> assignedSubjects = new HashSet<String>();
		Set<String> assignedLiterals = new HashSet<String>();

		boolean error = false;
		while (assignedColumns.size() != dataTable.getAttributes().size()) {
			Double bestScore = null;

			for (ColumnCandidate cc : candidateColumns) {
				if (bestScore == null || cc.getConfidence() > bestScore) {
					bestScore = cc.getConfidence();
				}
			}

			if (bestScore == null) {
				error = true;
				break;
			}

			for (Iterator<ColumnCandidate> it = candidateColumns.iterator(); it.hasNext();) {
				ColumnCandidate cc = it.next();
				if (cc.getConfidence() == bestScore) {
					it.remove();
					String literalId = cc.getAttribute().getSubjectClassURI() + " "
							+ cc.getAttribute().getPredicateURI();
					if (!assignedColumns.contains(cc.getColumn()) && !assignedLiterals.contains(literalId)) {
						acs.addCandidate(cc);
						assignedColumns.add(cc.getColumn());
						assignedSubjects.add(cc.getAttribute().getSubjectClassURI());
						assignedLiterals.add(literalId);
					}
				}
			}

		}

		Set<DummyGraphEdge> smallestGraph = null;
		DummyGraph dummyGraph = CandidateGraphCreator.createDummyGraph(simpleGraph);

		if (!error) {
			System.out.println("Greedy candidate set:");
			for (ColumnCandidate cand : acs.getAttributes()) {
				System.out.println(cand.getColumn().getIdentifier() + " / " + cand.getAttribute().getSubjectClassURI()
						+ " " + cand.getAttribute().getPredicateURI() + " -> " + cand.getConfidence());
			}

			Set<DummyGraphNode> targetNodes = new HashSet<DummyGraphNode>();
			for (DummyGraphNode node : dummyGraph.getNodes()) {
				if (assignedSubjects.contains(node.getResource().getURI()))
					targetNodes.add(node);
			}

			SubGraphsFinder2 sgf = new SubGraphsFinder2();
			Set<DummyGraph> candidateGraphs = sgf.findGraphs(dummyGraph, targetNodes);

			Integer sizeOfSmallestGraph = null;
			System.out.println("#candidateGraphs: " + candidateGraphs.size());
			for (DummyGraph candidateDummyGraph : candidateGraphs) {
				if (sizeOfSmallestGraph == null || candidateDummyGraph.getEdges().size() < sizeOfSmallestGraph) {
					sizeOfSmallestGraph = candidateDummyGraph.getEdges().size();
					smallestGraph = candidateDummyGraph.getEdges();
				}
			}
		}

		DummyGraph resultDummyGraph = new DummyGraph();

		if (smallestGraph == null) {
			System.out.println("Could not find graph.");

			// there is no connection between the chosen nodes. Create an
			// erroneous graph

			for (String subject : assignedSubjects) {
				for (DummyGraphNode node : dummyGraph.getNodes()) {
					if (node.getResource().getURI().equals(subject))
						resultDummyGraph.addNode(node);
				}
			}
		} else if (smallestGraph.isEmpty()) {
			for (DummyGraphNode node : dummyGraph.getNodes()) {
				if (assignedSubjects.contains(node.getResource().getURI()))
					resultDummyGraph.addNode(node);
			}
		} else {
			for (DummyGraphEdge edge : smallestGraph) {
				resultDummyGraph.addEdge(edge);
				resultDummyGraph.addNode(edge.getNode1());
				resultDummyGraph.addNode(edge.getNode2());
			}
		}

		Set<RDFNodeLiteralTriple> literalTriples = new HashSet<RDFNodeLiteralTriple>();
		Map<RDFNodeLiteralTriple, ColumnCandidate> literalTriplesColumns = new HashMap<RDFNodeLiteralTriple, ColumnCandidate>();

		for (DummyGraphNode node : resultDummyGraph.getNodes()) {
			for (ColumnCandidate cand : acs.getAttributes()) {
				if (cand.getAttribute().getSubjectClassURI().equals(node.getResource().getURI())) {
					for (RDFNodeLiteralTriple literalTriple : simpleGraph.getMiniSchema().getLiteralTriples()) {
						if (literalTriple.getSubject().getURI().equals(node.getResource().getURI())
								&& literalTriple.getProperty().getURI().equals(cand.getAttribute().getPredicateURI())) {
							RDFNodeLiteralTriple literalTripleCopy = literalTriple.copy();
							literalTripleCopy.setId(cand.getColumn().getIdentifier());
							literalTriples.add(literalTripleCopy);
							literalTriplesColumns.put(literalTripleCopy, cand);
						}
					}
				}
			}
		}

		Set<Set<RDFNodeTriple>> classTripleGroups = new HashSet<Set<RDFNodeTriple>>();

		for (DummyGraphEdge edge : resultDummyGraph.getEdges()) {

			Set<RDFNodeTriple> classTriples = new HashSet<RDFNodeTriple>();

			for (RDFNodeTriple classTriple : simpleGraph.getMiniSchema().getClassTriples()) {
				if (classTriple.getSubject() == edge.getNode1().getResource()
						&& classTriple.getObject() == edge.getNode2().getResource())
					classTriples.add(classTriple);
				else if (classTriple.getSubject() == edge.getNode2().getResource()
						&& classTriple.getObject() == edge.getNode1().getResource())
					classTriples.add(classTriple);
			}

			classTripleGroups.add(classTriples);
		}

		List<Set<RDFNodeTriple>> classTripleGroupsCombined = CandidateGraphCreator.combineGroups(classTripleGroups);
		for (Set<RDFNodeTriple> classTripleGroupCombined : classTripleGroupsCombined) {
			CandidateGraph resultSimpleGraph = new CandidateGraph();
			resultSimpleGraph.setMiniSchema(new MiniSchema());
			for (RDFNodeLiteralTriple literalTriple : literalTriples) {
				resultSimpleGraph.getMiniSchema().addLiteralTriple(literalTriple);

				Attribute attribute = new Attribute();
				attribute.setPredicateURI(literalTriple.getProperty().getURI());
				attribute.setSubjectClass(literalTriple.getSubject());
				resultSimpleGraph.getAttributes().add(attribute);

				ColumnCandidate cand = literalTriplesColumns.get(literalTriple);
				resultSimpleGraph.getColumns().put(attribute, cand.getColumn());

				attribute.setStatistics(cand.getColumn().getStatistics());
			}
			for (RDFNodeTriple classTriple : classTripleGroupCombined)
				resultSimpleGraph.getMiniSchema().addClassTriple(classTriple);
			resultSimpleGraph.setSize(classTripleGroupCombined.size());
			resultSimpleGraph.setAttributeCandidatesSet(acs);
			return resultSimpleGraph;
		}

		return null;
	}

	private List<Double> getDSLConfidences(SimpleGraph simpleGraph, DataTable dataTable) {

		List<Double> results = new ArrayList<Double>();

		for (Attribute column : dataTable.getAttributes()) {
			for (Attribute attribute : simpleGraph.getAttributes()) {
				Double confidence = this.dslConfidences.getConfidence(this.evaluationInstance.getGraphFileName(),
						this.evaluationInstance.getTableFileName(), column, attribute);
				results.add(confidence);
			}
		}

		return results;
	}

	private List<Double> getT2KMatchConfidences(SimpleGraph simpleGraph, DataTable dataTable) {

		List<Double> results = new ArrayList<Double>();

		for (Attribute column : dataTable.getAttributes()) {
			for (Attribute attribute : simpleGraph.getAttributes()) {
				Double confidence = this.t2kConfidences.getConfidence(this.evaluationInstance.getTableFileName(),
						column, attribute);
				System.out.println("T2K confidence: " + confidence);
				results.add(confidence);
			}
		}

		return results;
	}

	public List<Double> getConfidences(SimpleGraph simpleGraph, DataTable dataTable) {

		if (this.useDSLConfidences)
			return getDSLConfidences(simpleGraph, dataTable);
		else if (this.t2kConfidences != null) {
			return getT2KMatchConfidences(simpleGraph, dataTable);
		}

		List<String> graphFeatureStrings = new ArrayList<String>();
		List<String> tableFeaturesStrings = new ArrayList<String>();
		// List<String> weCosSimStrings = new ArrayList<String>();

		List<Double> results = new ArrayList<Double>();
		List<ProfileFeaturePlaceholder> profileFeaturePlaceholders = FeatureConfig.getProfileFeaturePlaceholders();

		Map<Attribute, List<Double>> graphFeatures = new HashMap<Attribute, List<Double>>();
		for (Attribute attribute : simpleGraph.getAttributes()) {
			List<Double> attributeGraphFeatures = SimpleGraphProfilesCreator.getFeatures(attribute,
					profileFeaturePlaceholders);
			graphFeatures.put(attribute, attributeGraphFeatures);
		}

		for (Attribute column : dataTable.getAttributes()) {

			List<Double> columnFeatures = DataTableProfilesCreator.getFeatureValues(column, profileFeaturePlaceholders);

			for (Attribute attribute : simpleGraph.getAttributes()) {

				List<Double> graphFeaturesOfAttribute = graphFeatures.get(attribute);

				if (attribute.getStatistics().getAttributeStatisticsType().getTypeL1() != column.getStatistics()
						.getAttributeStatisticsType().getTypeL1())
					continue;

				List<List<Double>> featuresNormalised = ProfilePairNormaliser.normalizeProfilePair(columnFeatures,
						graphFeaturesOfAttribute, profileFeaturePlaceholders);

				tableFeaturesStrings.add(StringUtils.join(featuresNormalised.get(0), ","));
				graphFeatureStrings.add(StringUtils.join(featuresNormalised.get(1), ","));
			}
		}

		List<List<String>> tableFeaturesStringSlices = new ArrayList<List<String>>();
		List<List<String>> graphFeatureStringSlices = new ArrayList<List<String>>();

		int stepSize = 150;
		// avoid "argument list too long" for python
		for (int i = 0; i < tableFeaturesStrings.size(); i = i + stepSize) {
			List<String> tableSlice = new ArrayList<String>();
			List<String> graphSlice = new ArrayList<String>();
			for (int j = i; j < Math.min(tableFeaturesStrings.size(), i + stepSize); j++) {
				tableSlice.add(tableFeaturesStrings.get(j));
				graphSlice.add(graphFeatureStrings.get(j));
			}
			tableFeaturesStringSlices.add(tableSlice);
			graphFeatureStringSlices.add(graphSlice);
		}

		for (int i = 0; i < graphFeatureStringSlices.size(); i++) {
			try {

				tableFeaturesStrings = tableFeaturesStringSlices.get(i);
				graphFeatureStrings = graphFeatureStringSlices.get(i);

				ProcessBuilder pb = new ProcessBuilder("python3", pythonFile, weightsFile,
						String.valueOf(NUMBER_OF_FEATURES), StringUtils.join(graphFeatureStrings, "\n"),
						StringUtils.join(tableFeaturesStrings, "\n"));// , StringUtils.join(weCosSimStrings, "\n"));

				Process p = pb.start();
				BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String res = in.readLine();

				for (String strRes : res.split(" "))
					results.add(Double.parseDouble(strRes));

				p.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return results;
	}

	public void setDSLConfidences(DSLConfidencesLoader dslConfidences) {
		this.dslConfidences = dslConfidences;
	}

	public void setT2KConfidences(T2KMatchConfidencesLoader t2kConfidences) {
		this.t2kConfidences = t2kConfidences;
	}

}
