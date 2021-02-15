package de.l3s.simpleml.tab2kg.profiles;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.datatypes.DatatypeFormatException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKTReader;

import de.l3s.simpleml.tab2kg.catalog.model.Attribute;
import de.l3s.simpleml.tab2kg.datareader.AttributeAnalyzer;
import de.l3s.simpleml.tab2kg.model.rdf.RDFNodeLiteralTriple;
import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;
import de.l3s.simpleml.tab2kg.util.FileLoader;
import de.l3s.simpleml.tab2kg.util.GeoUtil;

public class LiteralRelationProfileCreator {

	public static final String NULL_STRING = "?";

	private Model model;
	private RDFNodeLiteralTriple literalRelation;

	private Attribute attribute;

	public LiteralRelationProfileCreator(Model model, RDFNodeLiteralTriple literalRelation) {
		super();
		this.model = model;
		this.literalRelation = literalRelation;
	}

	public Attribute createProfile(List<Integer> numbersOfQuantiles, List<Integer> numbersOfIntervals) {

		this.attribute = new Attribute();
		this.attribute.setPredicateURI(this.literalRelation.getProperty().getURI());
		this.attribute.setSubjectClass(this.literalRelation.getSubject());

		// we do first rounds: first, try to detect the data type from the rdf
		// data type annotation (e.g. "^^xs:string"). If that's unknown (e.g.
		// "^^Kilogram"), go by data values.
		// boolean collectedValuesForTypeTest = false;
		// String uri = this.literalRelation.getObject().getDataType().getURI();
		// while (true) {

		DataTypeClass dataTypeClass = detectDataType(attribute);

		ProfileCreator.setDataTypeAndSetStatistics(attribute, dataTypeClass);

		boolean valid = collectValues();

		if (!valid)
			return null;

		attribute.getStatistics().updateValueList();

		if (numbersOfQuantiles != null)
			StatisticsComputer.computeStatistics(attribute, numbersOfQuantiles, numbersOfIntervals);

		return this.attribute;
	}

	private DataTypeClass detectDataType(Attribute attribute) {

		// collect values as strings, without transformation

		// create fake column to collect values, so we can use the column
		// analyser later
		Attribute column = new Attribute(-1);
		QueryExecution queryExec = null;

		try {
			String queryString = FileLoader.readResourceFileToString("queries/profiles/values.sparql");
			queryString = queryString.replace("@subjectType@", this.literalRelation.getSubject().getURI());
			queryString = queryString.replace("@property@", this.literalRelation.getProperty().getURI());
			queryString = queryString.replace("@objectType@", this.literalRelation.getObject().getDataType().getURI());

			Query query = QueryFactory.create(queryString);
			queryExec = QueryExecutionFactory.create(query, model);
			ResultSet rs = queryExec.execSelect();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();

				try {
					column.addValue(s.getLiteral("object").getString());
				} catch (DatatypeFormatException e) {
					continue;
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			queryExec.close();
			queryExec = null;

		}

		AttributeAnalyzer ca = new AttributeAnalyzer();
		DataTypeClass dataType = ca.detectDataType(column);
		column = null;
		ca = null;

		return dataType;
	}

	public boolean collectValues() {

		boolean hasNonNullValue = false;

		WKTReader wktReader = new WKTReader();
		WKBReader wkbReader = new WKBReader();

		GeoUtil geoUtil = new GeoUtil();

		try {
			String queryString = FileLoader.readResourceFileToString("queries/profiles/values.sparql");
			queryString = queryString.replace("@subjectType@", this.literalRelation.getSubject().getURI());
			queryString = queryString.replace("@property@", this.literalRelation.getProperty().getURI());
			queryString = queryString.replace("@objectType@", this.literalRelation.getObject().getDataType().getURI());
			Query query = QueryFactory.create(queryString);
			QueryExecution queryExec = QueryExecutionFactory.create(query, model);
			ResultSet rs = queryExec.execSelect();

			int numberOfValues = 0;

			Set<String> uniqueValues = new HashSet<String>();

			while (rs.hasNext()) {
				QuerySolution s = rs.nextSolution();

				String value = null;

				try {
					value = s.getLiteral("object").getString();
				} catch (DatatypeFormatException e) {
					return false;
				}

				if (value.isEmpty())
					value = null;
				else
					hasNonNullValue = true;

				if (value != null) {
					uniqueValues.add(value);
					numberOfValues += 1;
				}

				StatisticsComputer.addToStatistics(attribute, value, wktReader, wkbReader, geoUtil);
			}
			int numberOfDistinctValues = uniqueValues.size();

			if (this.attribute.getDataTypeClass() == DataTypeClass.XS_STRING
					|| this.attribute.getDataTypeClass() == DataTypeClass.RDF_LANG_STRING)
				attribute.getStatistics()
						.setIsCategorical(AttributeAnalyzer.isCategorical(numberOfValues, numberOfDistinctValues));

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (!hasNonNullValue) {
			System.out.println(
					"Has no non-null value: " + attribute.getSubjectClassURI() + " " + attribute.getPredicateURI());
			return false;
		}

		return true;

	}

}
