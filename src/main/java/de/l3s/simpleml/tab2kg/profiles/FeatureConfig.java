package de.l3s.simpleml.tab2kg.profiles;

import java.util.ArrayList;
import java.util.List;

import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeatureEnum;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeaturePlaceholder;

public class FeatureConfig {

	public static final int EMBEDDINGS_DIMENSION = 1024;

	public static List<ProfileFeaturePlaceholder> getProfileFeaturePlaceholders(FeatureConfigName featureConfigName) {

		List<ProfileFeaturePlaceholder> profileFeatures = new ArrayList<ProfileFeaturePlaceholder>();

		if (featureConfigName.useCompleteness()) {
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_DISTINCT_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_TRUE_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_FALSE_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_VALID_NON_NULL_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_VALID_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_NULL_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_INVALID_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NORMALISED_NUMBER_OF_DISTINCT_VALUES));
			profileFeatures
					.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NORMALISED_NUMBER_OF_VALID_NON_NULL_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NORMALISED_NUMBER_OF_VALID_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NORMALISED_NUMBER_OF_NULL_VALUES));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NORMALISED_NUMBER_OF_INVALID_VALUES));
		}

		if (featureConfigName.useBasicStatistics()) {
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_BELOW));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NUMBER_OF_OUTLIERS_ABOVE));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NORMALISED_NUMBER_OF_OUTLIERS_BELOW));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NORMALISED_NUMBER_OF_OUTLIERS_ABOVE));

			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.MEAN));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.SD));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.SKEWNESS));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.KURTOSIS));

			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.AVERAGE_NUMBER_OF_DIGITS));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CHARACTERS));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.AVERAGE_NUMBER_OF_TOKENS));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.AVERAGE_NUMBER_OF_SPECIAL_CHARACTERS));
			profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.AVERAGE_NUMBER_OF_CAPITALISED_VALUES));
		}

		if (featureConfigName.useDistributions()) {
			for (int rank = 0; rank <= 4; rank++) {
				profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.QUARTILE, rank));
			}
			for (int rank = 1; rank <= 9; rank++)
				profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.DECILE, rank));
			for (int rank = 0; rank <= 9; rank++) {
				profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.HISTOGRAM, rank));
				profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.NORMALISED_HISTOGRAM, rank));
			}
		}

		if (featureConfigName.useEmbeddings()) {
			for (int i = 0; i < EMBEDDINGS_DIMENSION; i++) {
				profileFeatures.add(new ProfileFeaturePlaceholder(ProfileFeatureEnum.EMBEDDING, i));
			}
		}

		return profileFeatures;
	}

}
