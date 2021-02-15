package de.l3s.simpleml.tab2kg.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RiotException;
import org.apache.jena.util.FileManager;

import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;

public class LabelFinder {

	private Model model;

	private Map<Resource, Map<String, Set<String>>> labels = new HashMap<Resource, Map<String, Set<String>>>();

	private Set<String> doneNamespaces = new HashSet<String>();

	public static void main(String[] args) {
		LabelFinder lf = new LabelFinder();
		lf.extractLabels("http://dublincore.org/2012/06/14/dcterms.rdf");
		lf.extractLabels("http://www.scholarlydata.org/ontology/conference-ontology.owl");
		lf.extractLabels("http://purl.org/seo/");
	}

	public LabelFinder() {

	}

	public LabelFinder(Model model) {
		this.model = model;
	}

	public void init() {
		for (final File fileEntry : new File(Config.getPath(FileLocation.LABELS_FOLDER)).listFiles()) {
			readFile(fileEntry);
		}
	}

	private void readFile(File file) {

		try {
			boolean firstLine = true;

			for (String line : Files.readAllLines(Paths.get(file.getPath()))) {

				if (firstLine) {
					doneNamespaces.add(line);
					firstLine = false;
					continue;
				}

				String[] parts = line.split("\t");

				Resource resource = model.getResource(parts[0]);
				if (resource == null)
					resource = model.createResource(parts[0]);

				String language = null;

				if (parts.length > 2)
					language = parts[2];

				if (!labels.containsKey(resource)) {
					labels.put(resource, new HashMap<String, Set<String>>());
				}
				if (!labels.get(resource).containsKey(language)) {
					labels.get(resource).put(language, new HashSet<String>());
				}

				Set<String> rdfsLabels = new HashSet<String>();

				rdfsLabels.add(parts[1].trim());

				if (parts[1].trim().startsWith("has"))
					rdfsLabels.add(parts[1].trim().replaceAll("^has", "").trim());

				Set<String> labelsToAdd = new HashSet<String>();
				labelsToAdd.addAll(rdfsLabels);

				for (String rdfsLabel : rdfsLabels) {
					// de-camelcase the local name and add it, if it does not
					// equal the label
					labelsToAdd.add(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(rdfsLabel), " ").trim()
							.replaceAll(" +", " "));
				}

				for (String rdfsLabel : labelsToAdd) {
					labels.get(resource).get(language).add(rdfsLabel);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getLabels(Resource resource, String language) {

		Set<String> labels = new HashSet<String>();

		if (!this.labels.containsKey(resource)) {
			if (!this.doneNamespaces.contains(resource.getNameSpace()))
				extractLabels(resource.getNameSpace());
		}

		if (this.labels.containsKey(resource)) {
			if (this.labels.get(resource).containsKey(language))
				labels.addAll(this.labels.get(resource).get(language));
			else if (this.labels.get(resource).containsKey(null))
				labels.addAll(this.labels.get(resource).get(null));
			else if (this.labels.get(resource).containsKey("en"))
				labels.addAll(this.labels.get(resource).get("en"));
		}

		if (labels.isEmpty())
			labels.add(StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(resource.getLocalName()), " ")
					.toLowerCase().trim().replaceAll(" +", " ").trim());

		return labels;
	}

	public String getRandomLabel(Resource resource, String language) {
		List<String> labels = new ArrayList<String>();
		labels.addAll(getLabels(resource, language));
		Collections.shuffle(labels);

		if (labels.isEmpty())
			return null;
		else
			return labels.get(0).trim();
	}

	public void extractLabels(String url) {

		String fileName = Config.getPath(FileLocation.LABELS_FOLDER)
				+ url.replace("/", "_").replace(".", "-").replace(":", "--") + ".tsv";
		File file = new File(fileName);

		if (!file.exists()) {

			PrintWriter writer = null;
			try {
				writer = new PrintWriter(fileName);
				writer.write(url + "\n");

				try {
					Model namespaceModel = FileManager.get().loadModel(url);

					String queryString = "SELECT ?resource ?label WHERE { ?resource <http://www.w3.org/2000/01/rdf-schema#label> ?label } ";

					Query query = QueryFactory.create(queryString);
					query.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
					QueryExecution queryExec = QueryExecutionFactory.create(query, namespaceModel);
					ResultSet rs = queryExec.execSelect();
					while (rs.hasNext()) {
						QuerySolution s = rs.nextSolution();

						Literal literal = s.getLiteral("label");
						if (s.getResource("resource").getURI() == null)
							continue;
						writer.write(s.getResource("resource").getURI() + "\t" + literal.getString() + "\t"
								+ literal.getLanguage() + "\n");

					}

				} catch (RiotException | HttpException e) {
					System.err.println(url + ": " + e.getMessage());
					return;
				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				writer.close();
			}

			// read the newly extracted data into the current model
			if (this.model != null) {
				readFile(new File(fileName));
			}
		}
	}
}
