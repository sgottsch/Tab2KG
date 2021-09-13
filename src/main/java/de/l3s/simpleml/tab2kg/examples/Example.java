package de.l3s.simpleml.tab2kg.examples;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.datareader.DataTableProfilesCreator;
import de.l3s.simpleml.tab2kg.datareader.DataTableReader;
import de.l3s.simpleml.tab2kg.graph.DataTableFromInputGraphCreator;
import de.l3s.simpleml.tab2kg.graph.MiniSchemaCreator;
import de.l3s.simpleml.tab2kg.graph.simple.SimpleGraphProfilesCreator;
import de.l3s.simpleml.tab2kg.ml.TableSemantifier;
import de.l3s.simpleml.tab2kg.model.graph.CandidateGraph;
import de.l3s.simpleml.tab2kg.model.graph.SimpleGraph;
import de.l3s.simpleml.tab2kg.profiles.FeatureConfig;
import de.l3s.simpleml.tab2kg.profiles.FeatureConfigName;
import de.l3s.simpleml.tab2kg.profiles.ProfilePairNormaliser;
import de.l3s.simpleml.tab2kg.profiles.features.ProfileFeaturePlaceholder;
import de.l3s.simpleml.tab2kg.profiles.rdf.ProfileCreatorTTL;
import de.l3s.simpleml.tab2kg.rml.CandidateGraphRMLCreator;
import de.l3s.simpleml.tab2kg.rml.RMLMappingExecutor;
import de.l3s.simpleml.tab2kg.rml.model.RMLMapping;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.IDGenerator;

public class Example {

	public static void main(String[] args) {

		// Input parameters
		String dataTableFileName = args[0]; // e.g., "soccer/tables/all_world_cup_players.csv"
		String domainGraphFilename = args[1]; // e.g., "soccer/graphs/world_cup_2014_squads.csv.ttl"

		System.out.println("Data table: " + dataTableFileName);
		System.out.println("Domain graph: " + domainGraphFilename);

		String dataTableId = dataTableFileName.substring(dataTableFileName.lastIndexOf("/") + 1);
		dataTableId = dataTableId.substring(0, dataTableId.lastIndexOf("."));
		dataTableId = IDGenerator.createURLString(dataTableId);

		String profileOutputFileName = Config.getPath(FileLocation.BASE_FOLDER) + "example_profile.ttl";
		String kgOutputFileName = Config.getPath(FileLocation.BASE_FOLDER) + "example_kg.ttl";
		String mappingFileName = Config.getPath(FileLocation.BASE_FOLDER) + "example_mapping.rml";

		// Create data table
		DataTable dataTable = DataTableReader.readDataTable(
				Config.getPath(FileLocation.BASE_FOLDER) + dataTableFileName, ",", true, true, true,
				DataTableFromInputGraphCreator.NULL_VALUE);
		dataTable.setId(dataTableId);
		for (Attribute column : dataTable.getAttributes()) {
			column.setURI(IDGenerator.createURLString(dataTable.getId(), "Attribute", column.getIdentifier()));
		}

		// Create knowledge graph
		SimpleGraph graph = new SimpleGraph(Config.getPath(FileLocation.BASE_FOLDER) + domainGraphFilename);

		// Feature configuration
		List<Integer> numbersOfQuantiles = Arrays.asList(4, 10);
		List<Integer> numbersOfIntervals = Arrays.asList(10);

		// Create profiles
		DataTableProfilesCreator.createColumnProfiles(dataTable, numbersOfQuantiles, numbersOfIntervals, false);
		SimpleGraphProfilesCreator.createAttributeProfiles(graph, numbersOfQuantiles, numbersOfIntervals, false);
//
		// Create semantic profile
		ProfileCreatorTTL pc = new ProfileCreatorTTL(dataTable);
		System.out.println("Create profile and store it in " + profileOutputFileName + ".");
		pc.createMetaFile("ExampleCatalog", profileOutputFileName);

		// Normalise the profile pairs between data type relation profiles and column
		// profiles
		List<ProfileFeaturePlaceholder> profileFeaturePlaceholders = FeatureConfig
				.getProfileFeaturePlaceholders(FeatureConfigName.ALL);

		System.out.println("#columns: " + dataTable.getAttributes().size());
		System.out.println("#attributes: " + graph.getAttributes().size());

		for (Attribute column : dataTable.getAttributes()) {

			List<Double> featuresColumn = DataTableProfilesCreator.getFeatureValues(column, profileFeaturePlaceholders,
					FeatureConfigName.ALL);
			for (Attribute attribute : graph.getAttributes()) {
				List<Double> featuresAttribute = SimpleGraphProfilesCreator.getFeatures(attribute,
						profileFeaturePlaceholders, FeatureConfigName.ALL);

				System.out.println("\n\n--- " + column.getIdentifier() + " ---");
				System.out.println(attribute.getSubjectClassURI() + " " + attribute.getPredicateURI());

				List<List<Double>> normed = ProfilePairNormaliser.normalizeProfilePair(featuresColumn,
						featuresAttribute, profileFeaturePlaceholders);
				System.out.println(normed.get(0));
				System.out.println(normed.get(1));
			}
		}
		System.out.println("===\n\n");

		// Semantic Table Interpretation
		TableSemantifier tableSemantifier = new TableSemantifier(null, false, false, FeatureConfigName.ALL, null, null);
		CandidateGraph resultGraph = tableSemantifier.findCandidateGraphGreedy(dataTable, graph);

		resultGraph.print();

		// create the knowledge graph inferred from the data table
		Map<Resource, Set<Property>> identifiers = MiniSchemaCreator.identifyIdentitiferLiterals(graph.getModel());

		System.out.println("Key properties:");
		for (Resource r : identifiers.keySet()) {
			System.out.println(" " + r.getLocalName());
			for (Property p : identifiers.get(r))
				System.out.println("  " + p);
		}

		RMLMapping mapping = CandidateGraphRMLCreator.createRMLMapping(dataTable, resultGraph, identifiers);

		CandidateGraphRMLCreator.createMappingString(mapping, mappingFileName);
		RMLMappingExecutor exec = new RMLMappingExecutor(mappingFileName);
		exec.run(kgOutputFileName, null);

	}

}
