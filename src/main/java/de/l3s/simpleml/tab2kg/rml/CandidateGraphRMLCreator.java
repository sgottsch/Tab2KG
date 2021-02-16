package de.l3s.simpleml.tab2kg.rml;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.graph.DataTableFromInputGraphCreator;
import de.l3s.simpleml.tab2kg.model.graph.CandidateGraph;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeTriple;
import de.l3s.simpleml.tab2kg.rml.model.RMLLiteralPredicateObjectMap;
import de.l3s.simpleml.tab2kg.rml.model.RMLMapping;
import de.l3s.simpleml.tab2kg.rml.model.RMLPredicateObjectMap;
import de.l3s.simpleml.tab2kg.rml.model.RMLSubjectMap;

public class CandidateGraphRMLCreator {

	public static RMLMapping createRMLMapping(DataTable dataTable, CandidateGraph graph,
			Map<Resource, Set<Property>> allIdentifiers) {

		RMLMapping mapping = new RMLMapping();
		mapping.setDelimiter(dataTable.getDelimiter());
		mapping.setSourceFileName(dataTable.getFileName());
		mapping.setNullValue(dataTable.getNullValue());

		Map<Resource, RMLSubjectMap> mapsPerType = new HashMap<Resource, RMLSubjectMap>();

		Map<Resource, String> identifiers = new HashMap<Resource, String>();

		Set<Resource> allTypes = new HashSet<Resource>();
		for (RDFNodeTriple relation : graph.getMiniSchema().getClassTriples()) {
			allTypes.add(relation.getSubject());
			allTypes.add(relation.getObject());
		}
		for (RDFNodeLiteralTriple literalRelation : graph.getMiniSchema().getLiteralTriples()) {
			allTypes.add(literalRelation.getSubject());
		}

		for (Resource type : allTypes) {
			RMLSubjectMap subjectMap = new RMLSubjectMap(type);
			mapsPerType.put(type, subjectMap);
			mapping.addType(subjectMap);

			if (allIdentifiers.containsKey(type)) {
				for (RDFNodeLiteralTriple literalRelation : graph.getMiniSchema().getLiteralTriples()) {
					if (literalRelation.getSubject() == type
							&& allIdentifiers.get(type).contains(literalRelation.getProperty())) {
						identifiers.put(type, literalRelation.getId());
						break;
					}
				}
			}
			if (!identifiers.containsKey(type))
				identifiers.put(type, DataTableFromInputGraphCreator.ROW_NUMBER);

			subjectMap.setIdentifier(identifiers.get(type));

		}

		for (RDFNodeLiteralTriple literalRelation : graph.getMiniSchema().getLiteralTriples()) {
			RMLLiteralPredicateObjectMap om = new RMLLiteralPredicateObjectMap();
			om.setColumnId(literalRelation.getId());
			om.setProperty(literalRelation.getProperty());
			om.setDataType(literalRelation.getObject().getDataType());
			mapsPerType.get(literalRelation.getSubject()).addLiteralMap(om);
		}

		for (RDFNodeTriple relation : graph.getMiniSchema().getClassTriples()) {
			RMLPredicateObjectMap om = new RMLPredicateObjectMap();
			om.setObjectResource(relation.getObject());
			om.setProperty(relation.getProperty());

			om.setIdentifier(identifiers.get(relation.getObject()));
			mapsPerType.get(relation.getSubject()).addRelationMap(om);
		}

		return mapping;
	}

	public static void createMappingString(RMLMapping mapping, String fileName) {

		PrintWriter writer = null;

		if (fileName != null)
			try {
				writer = new PrintWriter(fileName);

				List<String> lines = new ArrayList<String>();

				// for (String prefix : mapping.getPrefixes().keySet())
				// lines.add("@prefix " + prefix + " " +
				// mapping.getPrefixes().get(prefix));

				lines.add("@prefix rr: <http://www.w3.org/ns/r2rml#>.");
				lines.add("@prefix rml: <http://semweb.mmlab.be/ns/rml#>.");
				lines.add("@prefix xsd: <http://www.w3.org/2001/XMLSchema#>.");
				lines.add("@prefix ex: <http://example.com/resource/>.");
				lines.add("@prefix ql: <http://semweb.mmlab.be/ns/ql#>.");
				lines.add("@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>.");
				lines.add("@prefix csvw: <http://www.w3.org/ns/csvw#>.");

				// file
				lines.addAll(createRMLFileDescription(mapping.getSourceFileName(), mapping.getDelimiter(),
						mapping.getNullValue()));

				Map<Resource, Integer> resourceCounts = new HashMap<Resource, Integer>();

				int i = 0;
				for (RMLSubjectMap type : mapping.getTypes()) {
					lines.add("ex:Mapping" + i);
					lines.add("\trdf:type rr:TriplesMap ;");
					lines.add("\trml:logicalSource ex:File ;");
					lines.add("\trr:subjectMap [");
					lines.add("\t\trr:class <" + type.getRdfClass() + "> ;");

					// If same class appears the second time in a subject map,
					// change the template, e.g. "2_{no}";
					String prefix = "";
					Integer resourceCount = resourceCounts.get(type.getRdfClass());
					if (resourceCount != null) {
						prefix = resourceCount + "-";
						resourceCounts.put(type.getRdfClass(), resourceCount + 1);
					} else {
						resourceCounts.put(type.getRdfClass(), 2);
					}

					lines.add("\t\trr:template \"" + type.getRdfClass().getNameSpace()
							+ type.getRdfClass().getLocalName() + prefix + "{" + type.getIdentifier() + "}\" ;");
					lines.add("];");

					for (RMLLiteralPredicateObjectMap literalMap : type.getLiteralMaps()) {
						lines.add("rr:predicateObjectMap [");
						lines.add("\trr:predicate <" + literalMap.getProperty().getURI() + "> ;");
						lines.add("\trr:objectMap [");
						lines.add("\t\trml:reference \"" + literalMap.getColumnId() + "\";");

						if (literalMap.getDataType().getURI()
								.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"))
							lines.add("\t\trr:datatype <http://www.w3.org/2001/XMLSchema#string>");
						else
							lines.add("\t\trr:datatype <" + literalMap.getDataType().getURI() + ">");
						lines.add("\t]");
						lines.add("] ;");
					}

					for (RMLPredicateObjectMap classMap : type.getRelationMaps()) {
						lines.add("rr:predicateObjectMap [");
						lines.add("\trr:predicate <" + classMap.getProperty().getURI() + "> ;");
						lines.add("\trr:objectMap [");
						// lines.add("\t\trml:reference \"" +
						// literalMap.getColumnId() +
						// "\";");
						lines.add("\t\trr:template \"" + classMap.getObjectResource().getNameSpace()
								+ classMap.getObjectResource().getLocalName() + "{" + classMap.getIdentifier()
								+ "}\";");

						lines.add("\t]");
						lines.add("] ;");
					}

					i += 1;

					lines.set(lines.size() - 1, lines.get(lines.size() - 1).replace(";", "."));
				}

				int j = 0;
				for (String line : lines) {
					j += 1;

					if (writer == null) {
						if (j == lines.size())
							System.out.println(line.replace(";", "."));
						else
							System.out.println(line);
					} else {
						if (j == lines.size())
							writer.write(line.replace(";", ".") + "\n");
						else
							writer.write(line + "\n");
					}

				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (writer != null)
					writer.close();
			}

	}

	private static List<String> createRMLFileDescription(String sourceFileName, String delimiter, String nullValue) {

		List<String> lines = new ArrayList<String>();

		lines.add("ex:File");
		lines.add("\trdf:type rml:source ;");
		lines.add("\trml:source ex:FileSource ;");
		lines.add("\trml:referenceFormulation ql:CSV .");

		lines.add("ex:FileSource a csvw:Table;");
		lines.add("\tcsvw:url \"" + sourceFileName + "\" ;");
		lines.add("\tcsvw:dialect [");
		lines.add("\t\ta csvw:Dialect;");
		lines.add("\t\tcsvw:delimiter \"" + delimiter + "\";");
		lines.add("\t\tcsvw:null \"" + nullValue + "\";");

		lines.add("] .");

		return lines;
	}

}
