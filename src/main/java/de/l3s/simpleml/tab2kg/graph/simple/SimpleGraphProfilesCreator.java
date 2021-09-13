package de.l3s.simpleml.tab2kg.graph.simple;

import java.util.List;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.datareader.DataTableProfilesCreator;
import de.l3s.simpleml.tab2kg.datareader.EmbeddingsAdder;
import de.l3s.simpleml.tab2kg.graph.MiniSchemaCreator;
import de.l3s.simpleml.tab2kg.graph.TypeGraphBuilder;
import de.l3s.simpleml.tab2kg.model.graph.MiniSchema;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.profiles.FeatureConfigName;
import de.l3s.simpleml.tab2kg.profiles.LiteralRelationProfileCreator;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeaturePlaceholder;

public class SimpleGraphProfilesCreator {

	public static boolean createAttributeProfiles(SimpleGraph subGraph, List<Integer> numbersOfQuantiles,
			List<Integer> numbersOfIntervals, boolean useEmbeddings) {

		TypeGraphBuilder typeGraphBuilder = new TypeGraphBuilder();
		typeGraphBuilder.initModel(subGraph.getFileName());

		MiniSchemaCreator miniSchemaCreator = new MiniSchemaCreator(typeGraphBuilder);

		MiniSchema miniSchema = miniSchemaCreator.createMiniSchema();

		subGraph.setMiniSchema(miniSchema);
		subGraph.setModel(typeGraphBuilder.getModel());

		for (RDFNodeLiteralTriple literalRelation : miniSchema.getLiteralTriples()) {
			LiteralRelationProfileCreator profileCreator = new LiteralRelationProfileCreator(
					typeGraphBuilder.getModel(), literalRelation);
			Attribute attribute = profileCreator.createProfile(numbersOfQuantiles, numbersOfIntervals);

			if (attribute == null)
				return false;

			subGraph.getAttributes().add(attribute);
		}

		if (useEmbeddings)
			EmbeddingsAdder.extractValueFromGraph(subGraph);

		typeGraphBuilder.collectLiteralRelations();

		return true;
	}

	public static List<Double> getFeatures(Attribute attribute,
			List<ProfileFeaturePlaceholder> profileFeaturePlaceholders, FeatureConfigName featureConfigName) {
		return DataTableProfilesCreator.getFeatureValues(attribute, profileFeaturePlaceholders, featureConfigName);
	}

}
