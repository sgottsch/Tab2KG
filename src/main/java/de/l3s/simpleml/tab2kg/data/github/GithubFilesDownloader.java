package de.l3s.simpleml.tab2kg.data.github;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;
import org.apache.jena.util.FileManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;

public class GithubFilesDownloader {

	private Set<String> existingFiles = new HashSet<String>();
	private Map<Integer, Integer> urlCounts = new HashMap<Integer, Integer>();
	private int MINIMUM_NUMBER_OF_STATEMENTS = 50;
	private int MINIMUM_NUMBER_OF_LITERAL_STATEMENTS = 25;

	public static void main(String[] args) throws IOException, URISyntaxException {
		GithubFilesDownloader gfd = new GithubFilesDownloader();
		gfd.initFolders();
		gfd.downloadFiles();
		gfd.cleanFiles();
	}

	public void initFolders() {
		try {
			Files.createDirectories(Paths.get(Config.getPath(FileLocation.GITHUB_API_FOLDER)));
			Files.createDirectories(Paths.get(Config.getPath(FileLocation.GITHUB_DESCRIPTIONS_FOLDER)));
			Files.createDirectories(Paths.get(Config.getPath(FileLocation.GITHUB_RAW_FILES_FOLDER)));
			Files.createDirectories(Paths.get(Config.getPath(FileLocation.GITHUB_FILES_FOLDER)));
			Files.createDirectories(Paths.get(Config.getPath(FileLocation.GITHUB_RAW_DESCRIPTIONS_FOLDER)));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void downloadFiles() {

		List<String> endings = new ArrayList<String>();
		List<String> searchTerms = new ArrayList<String>();
		List<String> fileSizes = new ArrayList<String>();

		endings.add("ttl");
		endings.add("rdf");
		endings.add("nt");
		endings.add("nq");
		endings.add("trix");
		endings.add("n3");
		endings.add("owl");

		searchTerms.add("xsd");
		searchTerms.add("XMLSchema");

		fileSizes.add("size:>5000");
		fileSizes.add("size:>10000");
		fileSizes.add("size:>50000");
		fileSizes.add("size:>100000");

		GithubFilesDownloader githubFilesDownloader = new GithubFilesDownloader();
		githubFilesDownloader.loadExistingFiles();

		for (String ending : endings) {
			for (String searchTerm : searchTerms) {
				for (String fileSize : fileSizes) {

					String query = "extension:" + ending + " " + fileSize + " " + searchTerm;

					int page = 1;
					while (true) {
						boolean continueQuerying;
						try {
							continueQuerying = githubFilesDownloader.queryGithub(query, page);
							if (!continueQuerying)
								break;
							page += 1;
							if (page > 10)
								break;
						} catch (IOException | URISyntaxException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private void loadExistingFiles() {

		for (File file : new File(Config.getPath(FileLocation.GITHUB_RAW_FILES_FOLDER)).listFiles()) {
			existingFiles.add(file.getName());
		}

		System.out.println("Existing files: " + existingFiles.size());
	}

	private boolean queryGithub(String query, int page) throws IOException, URISyntaxException {

		System.out.println("Query: " + query);

		query = URLEncoder.encode(query, "UTF-8");

		int perPage = 100;

		String uri = "https://api.github.com/search/code?q=" + query;
		// uri += "&order=best+match";
		// uri += "&order=desc";
		uri += "&per_page=" + perPage;
		uri += "&page=" + page;

		System.out.println(" Page " + page);

		String res = null;
		boolean succesful = false;
		int numberOfCalls = 0;
		callsLoop: while (!succesful) {
			try {
				numberOfCalls += 1;

				URL url = new URL(uri);
				URLConnection urlConnection = url.openConnection();
				urlConnection.setRequestProperty("Authorization", "token " + Config.GITHUB_ACCESS_TOKEN);
				InputStream is = urlConnection.getInputStream();

				res = IOUtils.toString(is, "UTF-8");
				succesful = true;
			} catch (IOException e) {
				if (numberOfCalls < 10) {
					System.out.println(" API call failed (" + numberOfCalls + "). Wait and retry");
					System.out.println(" -> " + e.getMessage());
				} else {
					System.out.println(" API call failed too often. Stop.");
					System.out.println(" -> " + e.getMessage());
					break callsLoop;
				}

				try {
					// wait a little bit more than one minute
					Thread.sleep(60000 + (numberOfCalls * 2000));
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}

		if (!succesful) {
			System.exit(0);
		}

		JSONObject json = new JSONObject(res);
		json.put("tab2kg_query", query);
		json.put("tab2kg_page", page);
		json.put("tab2kg_query_url", uri);

		JSONArray arr = json.getJSONArray("items");

		double percentage = 100 * (double) (page * perPage) / json.getInt("total_count");

		System.out.println(String.format("%.2f", percentage) + "%" + " - Page " + page + ", Results: " + arr.length()
				+ ", Total Count: " + json.getInt("total_count"));

		PrintWriter writerAPI = null;
		try {
			writerAPI = new PrintWriter(Config.getPath(FileLocation.GITHUB_API_FOLDER) + page + ".json");
			writerAPI.write(json.toString());
		} finally {
			writerAPI.close();
		}

		for (int i = 0; i < arr.length(); i++) {
			JSONObject item = arr.getJSONObject(i);
			String url = item.getString("html_url");

			if (!item.has("repository"))
				continue;

			int repositoryId = item.getJSONObject("repository").getInt("id");

			if (!urlCounts.containsKey(repositoryId))
				urlCounts.put(repositoryId, 1);
			else {
				if (urlCounts.get(repositoryId) >= 3) {
					System.out.println("Skip url " + repositoryId + ".");
					continue;
				} else
					urlCounts.put(repositoryId, urlCounts.get(repositoryId) + 1);
			}

			url = url.replace("https://github.com/", "https://raw.githubusercontent.com/").replace("/blob", "");

			String fileName = item.getJSONObject("repository").getInt("id") + "_" + item.getString("sha") + "_"
					+ item.getString("name");

			if (existingFiles.contains(fileName)) {
				System.out.println(" Duplicate: " + fileName);
				continue;
			}

			existingFiles.add(fileName);

			System.out.println(" " + fileName);
			item.put("tab2kg_filename", fileName);
			item.put("tab2kg_url", url);

			try (InputStream in = new URL(url).openStream()) {
				Files.copy(in, Paths.get(Config.getPath(FileLocation.GITHUB_RAW_FILES_FOLDER) + fileName));
			} catch (FileNotFoundException e) {
				System.out.println(" Not found: " + fileName);
				continue;
			}

			PrintWriter writerDescription = null;
			try {
				writerDescription = new PrintWriter(
						Config.getPath(FileLocation.GITHUB_RAW_DESCRIPTIONS_FOLDER) + fileName);

				writerDescription.write(item.toString());
			} finally {
				writerDescription.close();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return arr.length() == perPage;
	}

	private void cleanFiles() {

		// conditions
		// max. 3 files per repository
		// knowledge graph can be parsed
		// knowledge graph has at least 50 lines
		// knowledge graph has at least x facts with data type properties

		Map<Integer, Integer> repositoryCounts = new HashMap<Integer, Integer>();

		for (File descriptionFile : new File(Config.getPath(FileLocation.GITHUB_RAW_DESCRIPTIONS_FOLDER)).listFiles()) {
			String fileName = descriptionFile.getName();
			System.out.println(fileName);

			try {
				int repositoryId = new JSONObject(FileUtils.readFileToString(descriptionFile, "UTF-8"))
						.getJSONObject("repository").getInt("id");

				if (!repositoryCounts.containsKey(repositoryId))
					repositoryCounts.put(repositoryId, 1);
				else {
					if (repositoryCounts.get(repositoryId) >= 3) {
						System.out.println("Skip file " + fileName + ".");
						continue;
					} else
						repositoryCounts.put(repositoryId, repositoryCounts.get(repositoryId) + 1);
				}

				File file = new File(Config.getPath(FileLocation.GITHUB_RAW_FILES_FOLDER) + fileName);
				checkFile(file);

			} catch (JSONException | IOException e) {
				System.out.println("Skip file " + fileName + " because of error: " + e.getMessage());
				continue;
			}

		}

	}

	private void checkFile(File file) {

		System.out.println("Check file: " + file.getName());

		Model model = null;
		try {
			model = FileManager.get().loadModel(file.getAbsolutePath(), null);
		} catch (RiotException | RuntimeIOException e) {
			System.out.println(" Skip file: " + e.getMessage());
			return;
		}

		List<Statement> statements = model.listStatements().toList();

		if (statements.size() < MINIMUM_NUMBER_OF_STATEMENTS)
			return;

		int numberOfLiteralStatements = 0;
		for (Statement statement : statements) {
			if (statement.getObject().isLiteral()) {
				numberOfLiteralStatements += 1;
				if (numberOfLiteralStatements >= MINIMUM_NUMBER_OF_LITERAL_STATEMENTS)
					break;
			}
		}

		if (numberOfLiteralStatements < MINIMUM_NUMBER_OF_LITERAL_STATEMENTS)
			return;

		OutputStream outputStream = null;
		try {
			String newFileName = file.getName() + ".ttl";

			outputStream = new FileOutputStream(Config.getPath(FileLocation.GITHUB_FILES_FOLDER) + newFileName);
			RDFDataMgr.write(outputStream, model, Lang.TTL);

			FileUtils.copyFile(new File(Config.getPath(FileLocation.GITHUB_RAW_DESCRIPTIONS_FOLDER) + file.getName()),
					new File(Config.getPath(FileLocation.GITHUB_DESCRIPTIONS_FOLDER) + newFileName));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println(" -> valid graph.");
	}

}
