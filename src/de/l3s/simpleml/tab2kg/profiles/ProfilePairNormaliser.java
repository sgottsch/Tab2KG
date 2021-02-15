package de.l3s.simpleml.tab2kg.profiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL1;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL2;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL3;
import de.l3s.simpleml.tab2kg.datareader.DataTableProfilesCreator;
import de.l3s.simpleml.tab2kg.datareader.DataTableReader;
import de.l3s.simpleml.tab2kg.graph.DataTableFromInputGraphCreator;
import de.l3s.simpleml.tab2kg.graph.simple.SimpleGraphProfilesCreator;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeaturePlaceholder;

public class ProfilePairNormaliser {

	public static void main(String[] args) {

		DataTable dataTable = DataTableReader.readDataTable("all_world_cup_players.csv", ",", true, true, true,
				DataTableFromInputGraphCreator.NULL_VALUE);

		SimpleGraph simpleGraph = new SimpleGraph("world_cup_2014_squads.csv.ttl");

		List<Integer> numbersOfQuantiles = Arrays.asList(4, 10);
		List<Integer> numbersOfIntervals = Arrays.asList(10);

		DataTableProfilesCreator.createColumnProfiles(dataTable, numbersOfQuantiles, numbersOfIntervals);
		SimpleGraphProfilesCreator.createAttributeProfiles(simpleGraph, numbersOfQuantiles, numbersOfIntervals);

		List<ProfileFeaturePlaceholder> profileFeaturePlaceholders = FeatureConfig.getProfileFeaturePlaceholders();

		System.out.println("#columns: " + dataTable.getAttributes().size());
		System.out.println("#attributes: " + simpleGraph.getAttributes().size());

		for (Attribute column : simpleGraph.getAttributes()) {
			System.out.println(column.getStatistics().getAttributeStatisticsTypeL3());
		}

		for (Attribute column : dataTable.getAttributes()) {

			if (!column.getIdentifier().contains("Number"))
				continue;

			List<Double> featuresColumn = DataTableProfilesCreator.getFeatureValues(column, profileFeaturePlaceholders);
			for (Attribute attribute : simpleGraph.getAttributes()) {
				List<Double> featuresAttribute = SimpleGraphProfilesCreator.getFeatures(attribute,
						profileFeaturePlaceholders);

				if (!attribute.getPredicateURI().contains("tag"))
					continue;

				System.out.println("\n\n--- " + column.getIdentifier() + " ---\n");
				System.out.println(attribute.getSubjectClassURI() + " " + attribute.getPredicateURI());

				System.out.println(featuresColumn);
				System.out.println(featuresAttribute);
				List<List<Double>> normed = normalizeProfilePair(featuresColumn, featuresAttribute,
						profileFeaturePlaceholders);
				System.out.println(normed.get(0));
				System.out.println(normed.get(1));
				break;
			}
			break;
		}

	}

	public static List<List<Double>> normalizeProfilePair(List<Double> features1, List<Double> features2,
			List<ProfileFeaturePlaceholder> profileFeaturePlaceholders) {

		List<Double> features1Norm = new ArrayList<Double>();
		List<Double> features2Norm = new ArrayList<Double>();

		int i = 0;
		// types
		for (@SuppressWarnings("unused")
		AttributeStatisticsTypeL1 type : AttributeStatisticsTypeL1.values()) {
			// System.out.println("F1, L1 " + type + " - " + features1.get(i));
			features1Norm.add(features1.get(i));
			features2Norm.add(features2.get(i));
			i += 1;
		}
		for (@SuppressWarnings("unused")
		AttributeStatisticsTypeL2 type : AttributeStatisticsTypeL2.values()) {
			// System.out.println("F1, L2 " + type + " - " + features1.get(i));
			features1Norm.add(features1.get(i));
			features2Norm.add(features2.get(i));
			i += 1;
		}
		for (@SuppressWarnings("unused")
		AttributeStatisticsTypeL3 type : AttributeStatisticsTypeL3.values()) {
			// System.out.println("F1, L3 " + type + " - " + features1.get(i));

			features1Norm.add(features1.get(i));
			features2Norm.add(features2.get(i));
			i += 1;
		}

		for (int j = i; j < features1.size(); j++) {
			ProfileFeaturePlaceholder profileFeaturePlaceholder = profileFeaturePlaceholders.get(j - i);

//			System.out.println(
//					"F1 " + j + " -> " + features1.get(j) + " - " + profileFeaturePlaceholder.getProfileFeatureEnum());
//			System.out.println(
//					"F2 " + j + " -> " + features2.get(j) + " - " + profileFeaturePlaceholder.getProfileFeatureEnum());

			if (profileFeaturePlaceholder.getProfileFeatureEnum().getNormaliser() == null) {

				boolean feature1IsNull = features1.get(j) == -1;
				boolean feature2IsNull = features2.get(j) == -1;

//				double max = Math.max(Math.abs(Math.max(features1.get(j), features2.get(j))),
//						Math.abs(Math.min(features1.get(j), features2.get(j))));

//				double sum = Math.max(Math.abs(Math.max(features1.get(j), features2.get(j))),
//						Math.abs(Math.min(features1.get(j), features2.get(j))));

				if (feature1IsNull && !feature2IsNull) {
					features1Norm.add(-1d);
					if (features2.get(j) <= 0)
						features2Norm.add(0d);
					else
						features2Norm.add(1d);
				} else if (!feature1IsNull && feature2IsNull) {
					features2Norm.add(-1d);
					if (features1.get(j) <= 0)
						features1Norm.add(0d);
					else
						features1Norm.add(1d);
				} else if (feature1IsNull && feature2IsNull) {
					features1Norm.add(-1d);
					features2Norm.add(-1d);
				} else {
					double sum = Math.abs(features1.get(j)) + Math.abs(features2.get(j));
					if (sum == 0) {
						features1Norm.add(0d);
						features2Norm.add(0d);
					} else {
						features1Norm.add(features1.get(j) / sum);
						features2Norm.add(features2.get(j) / sum);
					}
				}

			} else {
				features1Norm.add(features1.get(j));
				features2Norm.add(features2.get(j));
			}

//			System.out.println("F1, norm: " + features1Norm.get(j));
//			System.out.println("F2, norm: " + features2Norm.get(j));

		}

		List<List<Double>> normalisedFeatures = new ArrayList<List<Double>>();

		normalisedFeatures.add(features1Norm);
		normalisedFeatures.add(features2Norm);

		return normalisedFeatures;
	}

}
