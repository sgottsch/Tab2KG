package de.l3s.simpleml.tab2kg.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import de.l3s.simpleml.tab2kg.model.sparql.datatypes.DataTypeClass;

public class RDFUtil {

	public static void writeTTLFile(Model model, String fileName) {
		FileOutputStream outputFile = null;
		try {
			outputFile = new FileOutputStream(fileName);
			RDFDataMgr.write(outputFile, model, Lang.TTL);
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.err);
		} finally {
			try {
				outputFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String transformStatementsToString(Set<Statement> statements) {

		Model model = ModelFactory.createDefaultModel();
		for (Statement statement : statements)
			model.add(statement);

		StringWriter writer = new StringWriter();
		RDFDataMgr.write(writer, model, Lang.TTL);

		return writer.toString();
	}

	public static Date parseRDFDate(String dateString) throws ParseException {

		List<SimpleDateFormat> dateFormats = new ArrayList<SimpleDateFormat>();
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd"));
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
		dateFormats.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ"));

		for (SimpleDateFormat dateFormat : dateFormats) {
			try {
				return dateFormat.parse(dateString);
			} catch (java.text.ParseException e) {
				continue;
			}
		}

		throw new java.text.ParseException("Cannot parse date: " + dateString, 0);
	}

	public static DataTypeClass getDataTypeClass(OntProperty dataType) {

		if (dataType.getRange().getURI().equals("http://www.w3.org/2000/01/rdf-schema#Literal")) {
			return DataTypeClass.XS_STRING;
		}

		return getDataTypeClass(dataType.getRange().getURI());
	}

	public static DataTypeClass getDataTypeClass(String uri) {
		for (DataTypeClass dataTypeClass : DataTypeClass.values()) {
			if (dataTypeClass.getURL().equals(uri))
				return dataTypeClass;
		}

		return null;

	}
}
