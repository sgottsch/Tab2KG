package de.l3s.simpleml.tab2kg.graph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import com.github.sisyphsu.dateparser.DateParserUtils;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.DataTable;
import de.l3s.simpleml.tab2kg.catalog.model.dataset.datatable.Row;
import de.l3s.simpleml.tab2kg.model.rdf.QueryGraph;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClass;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClassLiteralTriple;
import de.l3s.simpleml.tab2kg.model.rdf.RDFClassTriple;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.profiles.ProfileCreator;
import de.l3s.simpleml.tab2kg.util.RDFUtil;

public class DataTableFromInputGraphCreator {

	private static final String CSV_SEPARATOR = "\t";
	public static final String NULL_VALUE = "";
	public static final String ROW_NUMBER = "rowNumber";

	private QueryGraph queryGraph;
	private Model model;

	private String literalsQueryString;
	private String completeQueryString;
	private List<PlaceHolderTriple> placeHolderTriples;

	public DataTableFromInputGraphCreator(Model model, QueryGraph queryGraph) {
		this.queryGraph = queryGraph;
		this.model = model;
	}

	public DataTable createDataTable(int minimumNumberOfLines, Map<String, Integer> uris) {
		Row headerRow = new Row(-1);
		headerRow.addValue(ROW_NUMBER);

		int columnNumber = 0;
		for (RDFClassLiteralTriple literalRelation : queryGraph.getLiteralTriples()) {
			columnNumber += 1;
			literalRelation.setId("c" + columnNumber);
			headerRow.addValue(literalRelation.getId());
		}

		DataTable dataTable = collectRows(this.literalsQueryString, 0, minimumNumberOfLines, uris);
		if (dataTable != null)
			dataTable.setHeaderRow(headerRow);

		return dataTable;
	}

	private DataTable collectRows(String queryString, int retry, int minimumNumberOfLines, Map<String, Integer> uris) {

		if (retry == 3) {
			return null;
		}

		DataTable dataTable = new DataTable(CSV_SEPARATOR);

		Map<RDFClassLiteralTriple, Attribute> columnsByRelation = new HashMap<RDFClassLiteralTriple, Attribute>();
		int columnNumber = 0;

		for (RDFClassLiteralTriple literalRelation : queryGraph.getLiteralTriples()) {
			Attribute column = new Attribute(columnNumber);
			column.setActive(true);
			columnNumber += 1;

			dataTable.addAttribute(column);
			columnsByRelation.put(literalRelation, column);

			String uri = literalRelation.getObject().getDataType().getURI();
			DataTypeClass dataTypeClass = RDFUtil.getDataTypeClass(uri);

			if (dataTypeClass == null) {
				System.out.println("Unknown data type: " + uri);
				dataTypeClass = DataTypeClass.XS_STRING;
			}

			ProfileCreator.setDataTypeAndSetStatistics(column, dataTypeClass);
		}

		Query query = QueryFactory.create(queryString);
		QueryExecution queryExec = QueryExecutionFactory.create(query, model);
		ResultSet rs = queryExec.execSelect();
		int numberOfLines = 0;

		int rowNumber = 0;
		try {

			try {
				while (rs.hasNext()) {
					QuerySolution s = rs.nextSolution();

					Row row = new Row(rowNumber);
					rowNumber += 1;

					for (RDFClassLiteralTriple literalRelation : queryGraph.getLiteralTriples()) {

						Attribute column = columnsByRelation.get(literalRelation);

						String value = null;
						if (s.getLiteral(literalRelation.getObject().getPlaceHolder()) != null)
							value = s.getLiteral(literalRelation.getObject().getPlaceHolder()).getString();

						if (value != null) {
							value = value.replace(CSV_SEPARATOR, "   ");

							switch (column.getStatistics().getAttributeStatisticsType()) {
							case INTEGER:
								try {
									int intValue = Integer.valueOf(value);
									value = String.valueOf(intValue);
								} catch (NumberFormatException e) {
									System.out.println("Warning. Cannot parse integer: " + value + ".");
									value = null;
								}
								break;
							case DOUBLE:
								double doubleValue = Double.valueOf(value);
								try {
									value = String.valueOf(doubleValue);
								} catch (NumberFormatException e) {
									System.out.println("Warning. Cannot parse double: " + value + ".");
									value = null;
								}
								break;
							case BOOLEAN:
								String valueLow = value.toLowerCase();
								if (valueLow.equals("true"))
									value = "true";
								else if (valueLow.equals("false"))
									value = "false";
								else {
									System.out.println("Warning. Cannot parse boolean: " + value + ".");
									value = null;
								}
								break;
							case GEO:
								break;
							case DATETIME:
								try {
									Date date = DateParserUtils.parseDate(value);
									value = String.valueOf(DataTable.DATETIME_FORMAT.format(date));
								} catch (DateTimeParseException e) {
									System.out.println("Warning. Cannot parse date: " + value + ".");
									value = null;
								}
								break;
							case STRING:
								break;
							default:
								break;
							}
						} else {
							value = DataTableFromInputGraphCreator.NULL_VALUE;
						}

						try {
							if (s.getLiteral(literalRelation.getObject().getPlaceHolder()) == null) {
								row.addValue(NULL_VALUE);
								column.addValue(NULL_VALUE);
							} else {
								value = value.replace(dataTable.getDelimiter(), " ").replace("\n", " ");
								row.addValue(value);
								column.addValue(value);
								// Literal literal =
								// s.getLiteral(literalRelation.getObject().getPlaceHolder());
								// row.addValue(literal.getLexicalForm().replace(CSV_SEPARATOR,
								// " ").replace("\n", " "));
							}
						} catch (ClassCastException e) {
							continue;
						}
					}

					dataTable.addRow(row);

					numberOfLines += 1;
				}
			} catch (IllegalArgumentException e) {
				// for some reason, rs.hasNext() sometimes throws a "Comparison
				// method violates its general contract"
				// IllegalArgumentException. Retry it two more times, maybe it
				// works.

				return collectRows(queryString, retry + 1, minimumNumberOfLines, uris);
			}
		} finally {
			queryExec.close();
		}

		if (numberOfLines < minimumNumberOfLines) {
			return null;
		}

		return dataTable;
	}

	public String createQuery(int numberOfLines) {

		this.placeHolderTriples = new ArrayList<PlaceHolderTriple>();

		List<String> lines = new ArrayList<String>();

		lines.add("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>");

		int literalIndex = 0;

		String literalsSelectString = "SELECT DISTINCT ";

		Set<String> completeSelects = new HashSet<String>();

		String filterString = "FILTER ( ";
		String literalString = "FILTER ( ";

		int placeHolderTripleIndex = 0;

		for (RDFClassLiteralTriple literalRelation : queryGraph.getLiteralTriples()) {
			String literalVariableName = "?l" + literalIndex;
			literalRelation.getObject().setPlaceHolder(literalVariableName);

			literalIndex += 1;
			literalsSelectString += literalVariableName + " ";

			literalsSelectString += "(" + literalRelation.getSubject().getPlaceHolder() + " AS " + literalVariableName
					+ "t) ";

			filterString += "BOUND(" + literalVariableName + ") || ";
			literalString += "ISLITERAL(" + literalVariableName + ") && ";
			completeSelects.add(literalVariableName);
		}
		literalsSelectString += "WHERE {";
		filterString = filterString.substring(0, filterString.length() - 3) + ") .";
		literalString = literalString.substring(0, literalString.length() - 3) + ") .";

		lines.add(literalsSelectString);

		for (RDFClassTriple triple : queryGraph.getClassTriples()) {
			String tripleString = triple.getSubject().getPlaceHolder() + " <" + triple.getProperty().getURI() + "> "
					+ triple.getObject().getPlaceHolder() + " .";
			lines.add(tripleString);
			completeSelects.add(triple.getSubject().getPlaceHolder());
			completeSelects.add(triple.getObject().getPlaceHolder());

			String predicatePlaceHolder = "ph" + placeHolderTripleIndex;
			completeSelects.add("(<" + triple.getProperty().getURI() + "> AS ?" + predicatePlaceHolder + ")");
			placeHolderTripleIndex += 1;
			this.placeHolderTriples.add(new PlaceHolderTriple(triple.getSubject().getPlaceHolder(),
					predicatePlaceHolder, triple.getObject().getPlaceHolder()));
		}

		for (RDFClass type : queryGraph.getTypes()) {
			String tripleString = type.getPlaceHolder() + " rdf:type <" + type.getResource().getURI() + "> .";
			lines.add(tripleString);
			completeSelects.add(type.getPlaceHolder());

			String predicatePlaceHolder = "ph" + placeHolderTripleIndex;
			completeSelects.add("(rdf:type AS ?" + predicatePlaceHolder + ")");
			placeHolderTripleIndex += 1;
			String predicatePlaceHolder2 = "ph" + placeHolderTripleIndex;
			completeSelects.add("(<" + type.getResource().getURI() + "> AS ?" + predicatePlaceHolder2 + ")");
			placeHolderTripleIndex += 1;
			this.placeHolderTriples
					.add(new PlaceHolderTriple(type.getPlaceHolder(), predicatePlaceHolder, predicatePlaceHolder2));
		}

		for (RDFClassLiteralTriple triple : queryGraph.getLiteralTriples()) {
			String tripleString = "OPTIONAL { " + triple.getSubject().getPlaceHolder() + " <"
					+ triple.getProperty().getURI() + "> " + triple.getObject().getPlaceHolder() + " . }";
			lines.add(tripleString);

			String predicatePlaceHolder = "ph" + placeHolderTripleIndex;
			completeSelects.add("(<" + triple.getProperty().getURI() + "> AS ?" + predicatePlaceHolder + ")");
			placeHolderTripleIndex += 1;

			this.placeHolderTriples.add(new PlaceHolderTriple(triple.getSubject().getPlaceHolder(),
					predicatePlaceHolder, triple.getObject().getPlaceHolder()));
		}
		lines.add(filterString);
		lines.add(literalString);

		lines.add("} ORDER BY RAND() LIMIT " + numberOfLines);

		String query = StringUtils.join(lines, "\n");
		this.literalsQueryString = query;

		String completeSelectString = "SELECT DISTINCT ";
		for (String select : completeSelects)
			completeSelectString += select + " ";
		completeSelectString += " WHERE {";
		lines.set(1, completeSelectString);
		lines.set(lines.size() - 1, "}");

		this.completeQueryString = StringUtils.join(lines, "\n");

		return query;
	}

	public static void writeDataTableToFile(DataTable dataTable) {
		System.out.println("Write data table to file: " + dataTable.getFileName());

		PrintWriter writer = null;

		try {
			writer = new PrintWriter(dataTable.getFileName());

			String[] header = new String[dataTable.getHeaderRow().getValues().size()];
			int i = 0;
			for (String headerValue : dataTable.getHeaderRow().getValues()) {
				header[i] = headerValue;
				i += 1;
			}

			CSVPrinter csvPrinter = null;
			try {
				csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(header));
				for (Row row : dataTable.getRows()) {
					List<String> values = new ArrayList<String>();
					values.add(String.valueOf(row.getRowNumber()));
					values.addAll(row.getValues());
					csvPrinter.printRecord(values);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					csvPrinter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			writer.close();
		}
	}

	// private JSONObject createJSON(Map<RDFClassLiteralTriple, Attribute>
	// attributes, Map<String, Integer> uris,
	// int numberOfLines) {
	//
	// JSONObject columnsJSON = new JSONObject();
	// columnsJSON.put("numberOfLines", numberOfLines);
	//
	// int columnNumber = 0;
	// for (RDFClassLiteralTriple triple : attributes.keySet()) {
	//
	// // triple.setId("column" + columnNumber);
	// Attribute attribute = attributes.get(triple);
	//
	// StatisticsComputer.computeStatistics(attribute,
	// MiniSchemaCreator.NUMBER_OF_QUANTILES,
	// MiniSchemaCreator.HISTOGRAM_SIZE);
	//
	// JSONObject columnJSON = MiniSchemaCreator.createLiteralTripleJSON(triple,
	// columnNumber, attribute, uris);
	// columnsJSON.put(triple.getId(), columnJSON);
	//
	// columnNumber += 1;
	// }
	//
	// return columnsJSON;
	// }

	public void createSubGraph(Model graphModel, String fileName, Set<Statement> typeStatements) {

		Model subGraphModel = ModelFactory.createDefaultModel();

		Query query = QueryFactory.create(this.completeQueryString);

		QueryExecution queryExec = QueryExecutionFactory.create(query, graphModel);
		ResultSet rs = queryExec.execSelect();

		while (rs.hasNext()) {
			QuerySolution s = rs.nextSolution();

			for (PlaceHolderTriple phTriple : this.placeHolderTriples) {

				if (s.get(phTriple.getSubject()) == null || s.get(phTriple.getPredicate()) == null
						|| s.get(phTriple.getObject()) == null)
					continue;

				Resource subject = s.getResource(phTriple.getSubject());
				Property property = graphModel.getProperty(s.getResource(phTriple.getPredicate()).getURI());
				RDFNode objectNode = s.get(phTriple.getObject());

				if (objectNode.isLiteral()) {
					subGraphModel.addLiteral(subject, property, objectNode.asLiteral());
				} else
					subGraphModel.add(subject, property, objectNode.asResource());
			}

		}

		for (Statement typeStatement : typeStatements) {
			subGraphModel.add(typeStatement);
		}

		RDFUtil.writeTTLFile(subGraphModel, fileName);
	}

	private class PlaceHolderTriple {
		private String subject;
		private String predicate;
		private String object;

		public PlaceHolderTriple(String subject, String predicate, String object) {
			super();
			this.subject = subject;
			this.predicate = predicate;
			this.object = object;
		}

		public String getSubject() {
			return subject;
		}

		public String getPredicate() {
			return predicate;
		}

		public String getObject() {
			return object;
		}

	}

}
