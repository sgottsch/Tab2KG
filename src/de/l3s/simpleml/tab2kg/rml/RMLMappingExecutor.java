package de.l3s.simpleml.tab2kg.rml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Statement;

import be.ugent.rml.Executor;
import be.ugent.rml.Utils;
import be.ugent.rml.functions.FunctionLoader;
import be.ugent.rml.functions.lib.IDLabFunctions;
import be.ugent.rml.records.RecordsFactory;
import be.ugent.rml.store.QuadStore;
import be.ugent.rml.store.QuadStoreFactory;
import be.ugent.rml.store.RDF4JStore;
import de.l3s.simpleml.tab2kg.util.RDFUtil;

public class RMLMappingExecutor {

	private String mappingFileName;

	public RMLMappingExecutor(String mappingFileName) {
		super();
		this.mappingFileName = mappingFileName;
	}

	public void run() {
		run(null, null);
	}

	public boolean run(String outputFileName, Set<Statement> typeStatements) {

		try {

			String mapPath = this.mappingFileName;
			File mappingFile = new File(mapPath);

			// Get the mapping string stream
			InputStream mappingStream = new FileInputStream(mappingFile);

			// Load the mapping in a QuadStore
			QuadStore rmlStore = QuadStoreFactory.read(mappingStream);

			// Set up the basepath for the records factory, i.e., the basepath
			// for the (local file) data sources
			RecordsFactory factory = new RecordsFactory(mappingFile.getParent());

			// Set up the functions used during the mapping
			@SuppressWarnings("rawtypes")
			Map<String, Class> libraryMap = new HashMap<String, Class>();
			// libraryMap.put("GrelFunctions", GrelProcessor.class);
			libraryMap.put("IDLabFunctions", IDLabFunctions.class);

			// URL url = Resources.getResource("rml/functions.ttl");
			// File functionsFile = new File(url.toURI());
			//
			// File functionsFile = new
			// File(getClass().getClassLoader().getResource("rml/functions.ttl").getFile());

			// File functionsFile = new File(Config.getPath(FileLocation.FUNCTIONS_TTL));

			FunctionLoader functionLoader = new FunctionLoader(null, libraryMap);
			// FunctionLoader functionLoader = new FunctionLoader(null, null,
			// libraryMap);

			// Set up the outputstore (needed when you want to output something
			// else than nquads
			QuadStore outputStore = new RDF4JStore();

			// Create the Executor
			Executor executor = new Executor(rmlStore, factory, functionLoader, outputStore,
					Utils.getBaseDirectiveTurtle(mappingStream));

			// Execute the mapping
			QuadStore result = null;
			try {
				result = executor.execute(null);
			} catch (IOException e) {
				System.out.println("Error when executing mapping: " + e.getMessage());
				return false;
			}

			if (outputFileName == null) {
				// Output the result
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
				result.write(out, "turtle");

				out.close();
			} else {
				FileWriter fw = null;
				BufferedWriter out = null;
				try {
					fw = new FileWriter(outputFileName);
					out = new BufferedWriter(fw);
					result.write(out, "turtle");

					if (typeStatements != null) {
						if (!typeStatements.isEmpty())
							out.append("\n");

						out.append(RDFUtil.transformStatementsToString(typeStatements));
					}
				} finally {
					out.close();
					fw.close();
				}
			}

		} catch (Exception e) {
			// e.printStackTrace();
			System.out.println("Error when executing mapping: " + e.getMessage());
			return false;
		}

		return true;
	}

}
