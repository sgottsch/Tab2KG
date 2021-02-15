package de.l3s.simpleml.tab2kg.datareader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL1;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL2;
import de.l3s.simpleml.tab2kg.catalog.model.statistics.AttributeStatisticsTypeL3;
import de.l3s.simpleml.tab2kg.profiles.ColumnProfileCreator;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeature;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeaturePlaceholder;
import de.l3s.simpleml.tab2kg.util.Tab2KGConfiguration;

public class DataTableProfilesCreator {

	public static void main(String[] args) {

		DataTable dataTable = DataTableReader.readDataTable("all_world_cup_players.csv", ",", true, true,
				Tab2KGConfiguration.NULL_VALUE);

		createColumnProfiles(dataTable, Arrays.asList(10), Arrays.asList(10));
	}

	public static boolean createColumnProfiles(DataTable dataTable, List<Integer> numberOfQuantiles,
			List<Integer> numberOfIntervals) {
		for (Attribute column : dataTable.getAttributes()) {
			ColumnProfileCreator columnProfileCreator = new ColumnProfileCreator(column);

			boolean valid = columnProfileCreator.collectValues();

			if (!valid)
				return false;

			column.getStatistics().updateValueList();

			columnProfileCreator.computeStatistics(numberOfQuantiles, numberOfIntervals);
		}

		return true;
	}

	public static List<Double> getFeatureValues(Attribute column,
			List<ProfileFeaturePlaceholder> profileFeaturePlaceholders) {

		List<Double> features = new ArrayList<Double>();

		// type
		for (AttributeStatisticsTypeL1 type : AttributeStatisticsTypeL1.values()) {
			if (column.getStatistics().getAttributeStatisticsType().getTypeL1() == type) {
				features.add(1d);
			} else
				features.add(0d);
		}
		for (AttributeStatisticsTypeL2 type : AttributeStatisticsTypeL2.values()) {
			if (column.getStatistics().getAttributeStatisticsType() == type) {
				features.add(1d);
			} else
				features.add(0d);
		}
		for (AttributeStatisticsTypeL3 type : AttributeStatisticsTypeL3.values()) {
			if (column.getStatistics().getAttributeStatisticsTypeL3() == type) {
				features.add(1d);
			} else
				features.add(0d);
		}

		for (ProfileFeaturePlaceholder profileFeaturePlaceholder : profileFeaturePlaceholders) {
			ProfileFeature profileFeature = column.getStatistics().getProfileFeature(
					profileFeaturePlaceholder.getProfileFeatureEnum(), profileFeaturePlaceholder.getRank());

			if (profileFeature == null && profileFeaturePlaceholder.getProfileFeatureEnum().getBaseFeature() != null)
				profileFeature = column.getStatistics().getProfileFeature(
						profileFeaturePlaceholder.getProfileFeatureEnum().getBaseFeature(),
						profileFeaturePlaceholder.getRank());
			if (profileFeature == null)
				features.add(-1d); // TODO: READ FROM CONFIG
			else {
				double value = profileFeature.getDoubleValue();

				if (profileFeaturePlaceholder.getProfileFeatureEnum().getNormaliser() != null) {
					value = value / column.getStatistics()
							.getProfileFeature(profileFeaturePlaceholder.getProfileFeatureEnum().getNormaliser(), null)
							.getDoubleValue();
				}

				features.add(value);
			}
		}

		column.setFeatures(features);

		return features;
	}

}
