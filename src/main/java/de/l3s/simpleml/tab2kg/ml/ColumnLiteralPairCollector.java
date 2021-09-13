package de.l3s.simpleml.tab2kg.ml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL1;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL2;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL3;
import de.l3s.simpleml.tab2kg.data.PairsLoader;
import de.l3s.simpleml.tab2kg.datareader.DataTableProfilesCreator;
import de.l3s.simpleml.tab2kg.datareader.DataTableReader;
import de.l3s.simpleml.tab2kg.evaluation.EvaluationInstance;
import de.l3s.simpleml.tab2kg.graph.simple.SimpleGraphProfilesCreator;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.profiles.FeatureConfig;
import de.l3s.simpleml.tab2kg.profiles.FeatureConfigName;
import de.l3s.simpleml.tab2kg.profiles.ProfilePairNormaliser;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeaturePlaceholder;
import de.l3s.simpleml.tab2kg.rml.ColumnLiteralMapping;
import de.l3s.simpleml.tab2kg.rml.RMLMappingReader;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.CountMap;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Mode;
import de.l3s.simpleml.tab2kg.util.Source;

public class ColumnLiteralPairCollector {

	private static final String POSITIVE_PAIRS_1_FILE_NAME = "positive_pairs_1.csv";
	private static final String POSITIVE_PAIRS_2_FILE_NAME = "positive_pairs_2.csv";
	private static final String NEGATIVE_PAIRS_1_FILE_NAME = "negative_pairs_1.csv";
	private static final String NEGATIVE_PAIRS_2_FILE_NAME = "negative_pairs_2.csv";

	private CountMap<AttributeStatisticsTypeL1> tableCountsL1 = new CountMap<AttributeStatisticsTypeL1>();
	private CountMap<AttributeStatisticsTypeL2> tableCountsL2 = new CountMap<AttributeStatisticsTypeL2>();
	private CountMap<AttributeStatisticsTypeL3> tableCountsL3 = new CountMap<AttributeStatisticsTypeL3>();
	private CountMap<AttributeStatisticsTypeL1> graphCountsL1 = new CountMap<AttributeStatisticsTypeL1>();
	private CountMap<AttributeStatisticsTypeL2> graphCountsL2 = new CountMap<AttributeStatisticsTypeL2>();
	private CountMap<AttributeStatisticsTypeL3> graphCountsL3 = new CountMap<AttributeStatisticsTypeL3>();

	public static void main(String[] args) {

		List<FeatureConfigName> featureConfigNames = new ArrayList<FeatureConfigName>();
		featureConfigNames.addAll(Arrays.asList(FeatureConfigName.values()));

		for (FeatureConfigName featureConfigName : featureConfigNames) {
			ColumnLiteralPairCollector clpc = new ColumnLiteralPairCollector();
			clpc.loadPairs(featureConfigName);
		}

	}

	private void loadPairs(FeatureConfigName featureConfigName) {
		loadPairs(Mode.TRAINING, featureConfigName);
		loadPairs(Mode.TEST, featureConfigName);
	}

	private void loadPairs(Mode mode, FeatureConfigName featureConfigName) {

		System.out.println("Load pairs (" + mode + ", " + featureConfigName + ")");

		List<EvaluationInstance> pairs = PairsLoader.loadPairs(Source.GITHUB, mode, false);

		// create model folder if not exists
		File directoryModel = new File(
				Config.getPath(FileLocation.COLUMN_MATCHING_MODEL_FOLDER) + featureConfigName.getName());
		if (!directoryModel.exists()) {
			directoryModel.mkdirs();
		}

		// create pairs folder if not exists
		File directoryPairs = new File(
				Config.getPath(FileLocation.COLUMN_MATCHING_FOLDER, mode) + featureConfigName.getName());
		if (!directoryPairs.exists()) {
			directoryPairs.mkdirs();
		}

		PrintWriter writerPositive1 = null;
		PrintWriter writerPositive2 = null;
		PrintWriter writerNegative1 = null;
		PrintWriter writerNegative2 = null;
		try {
			writerPositive1 = new PrintWriter(Config.getPath(FileLocation.COLUMN_MATCHING_FOLDER, mode)
					+ featureConfigName.getName() + "/" + POSITIVE_PAIRS_1_FILE_NAME);
			writerPositive2 = new PrintWriter(Config.getPath(FileLocation.COLUMN_MATCHING_FOLDER, mode)
					+ featureConfigName.getName() + "/" + POSITIVE_PAIRS_2_FILE_NAME);
			writerNegative1 = new PrintWriter(Config.getPath(FileLocation.COLUMN_MATCHING_FOLDER, mode)
					+ featureConfigName.getName() + "/" + NEGATIVE_PAIRS_1_FILE_NAME);
			writerNegative2 = new PrintWriter(Config.getPath(FileLocation.COLUMN_MATCHING_FOLDER, mode)
					+ featureConfigName.getName() + "/" + NEGATIVE_PAIRS_2_FILE_NAME);

			int i = 0;
			for (EvaluationInstance pair : pairs) {
				if (i % 100 == 0)
					System.out.println(((double) i / pairs.size()) + "\t" + i + "/" + pairs.size() + " ("
							+ mode.toString().toLowerCase() + ")");
				i += 1;
				findMappings(pair, writerPositive1, writerPositive2, writerNegative1, writerNegative2,
						featureConfigName);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writerPositive1.close();
			writerPositive2.close();
			writerNegative1.close();
			writerNegative2.close();
		}

		System.out.println("Type statistics, tables:");

		System.out.println("=== L1 ===");
		tableCountsL1.print();
		System.out.println("=== L2 ===");
		tableCountsL2.print();
		System.out.println("=== L3 ===");
		tableCountsL3.print();

		System.out.println("Type statistics, graphs:");

		System.out.println("=== L1 ===");
		graphCountsL1.print();
		System.out.println("=== L2 ===");
		graphCountsL2.print();
		System.out.println("=== L3 ===");
		graphCountsL3.print();

	}

	private void findMappings(EvaluationInstance pair, PrintWriter writerPositive1, PrintWriter writerPositive2,
			PrintWriter writerNegative1, PrintWriter writerNegative2, FeatureConfigName featureConfigName) {

		List<Integer> numbersOfQuantiles = Arrays.asList(4, 10);
		List<Integer> numbersOfIntervals = Arrays.asList(10);

		DataTable dataTable = DataTableReader.readDataTable(pair.getTableFileName(), ",", true, true, true);

		SimpleGraph simpleGraph = new SimpleGraph(pair.getGraphFileName());

		DataTableProfilesCreator.createColumnProfiles(dataTable, numbersOfQuantiles, numbersOfIntervals,
				featureConfigName.useEmbeddings());
		SimpleGraphProfilesCreator.createAttributeProfiles(simpleGraph, numbersOfQuantiles, numbersOfIntervals,
				featureConfigName.useEmbeddings());
		List<ProfileFeaturePlaceholder> profileFeaturePlaceholders = FeatureConfig
				.getProfileFeaturePlaceholders(featureConfigName);

		RMLMappingReader rmr = new RMLMappingReader();
		List<ColumnLiteralMapping> mappings = rmr.getMappings(dataTable, simpleGraph, pair.getMappingFileName());

		for (Attribute column : dataTable.getAttributes()) {
			DataTableProfilesCreator.getFeatureValues(column, profileFeaturePlaceholders, featureConfigName);
			tableCountsL1.addValue(column.getStatistics().getAttributeStatisticsType().getTypeL1());
			tableCountsL2.addValue(column.getStatistics().getAttributeStatisticsType());
			tableCountsL3.addValue(column.getStatistics().getAttributeStatisticsTypeL3());
		}

		for (Attribute attribute : simpleGraph.getAttributes()) {
			SimpleGraphProfilesCreator.getFeatures(attribute, profileFeaturePlaceholders, featureConfigName);
			graphCountsL1.addValue(attribute.getStatistics().getAttributeStatisticsType().getTypeL1());
			graphCountsL2.addValue(attribute.getStatistics().getAttributeStatisticsType());
			graphCountsL3.addValue(attribute.getStatistics().getAttributeStatisticsTypeL3());
		}

		for (ColumnLiteralMapping mapping : mappings) {
			Attribute column = mapping.getColumn();
			Attribute attribute = mapping.getAttribute();

			column.setRepresentedAttribute(attribute);

			List<Double> featuresColumn = column.getFeatures();
			List<Double> featuresAttribute = attribute.getFeatures();

			for (Double d : featuresColumn) {
				if (Double.isNaN(d)) {
					System.out.println("ERROR: " + pair.getTableFileName());
				}
			}

			List<List<Double>> normed = ProfilePairNormaliser.normalizeProfilePair(featuresColumn, featuresAttribute,
					profileFeaturePlaceholders);

			writerPositive1.println(StringUtils.join(normed.get(0), ","));
			writerPositive2.println(StringUtils.join(normed.get(1), ","));
		}

		for (Attribute column : dataTable.getAttributes()) {
			for (Attribute attribute : simpleGraph.getAttributes()) {
				if (column.getRepresentedAttribute() != attribute) {
					List<List<Double>> normed = ProfilePairNormaliser.normalizeProfilePair(column.getFeatures(),
							attribute.getFeatures(), profileFeaturePlaceholders);

					writerNegative1.println(StringUtils.join(normed.get(0), ","));
					writerNegative2.println(StringUtils.join(normed.get(1), ","));
				}
			}
		}

	}

}
