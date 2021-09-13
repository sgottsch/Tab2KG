package de.l3s.simpleml.tab2kg.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.l3s.simpleml.tab2kg.data.semtab.SemTabTableCreator;
import de.l3s.simpleml.tab2kg.evaluation.EvaluationInstance;
import de.l3s.simpleml.tab2kg.util.Config;
import de.l3s.simpleml.tab2kg.util.FileLocation;
import de.l3s.simpleml.tab2kg.util.Mode;
import de.l3s.simpleml.tab2kg.util.Source;

public class PairsLoader {

	public static List<EvaluationInstance> loadPairs(Source source) {
		return loadPairs(source, null, false);
	}

	public static List<EvaluationInstance> loadPairs(Source source, boolean useCompleteGraphs) {
		return loadPairs(source, null, true);
	}

	public static List<EvaluationInstance> loadPairs(Source source, Mode mode, Boolean useCompleteGraphs) {

		if (source != Source.GITHUB)
			mode = null;

		List<EvaluationInstance> pairs = new ArrayList<EvaluationInstance>();

		String baseFolder = Config.getPath(FileLocation.BASE_FOLDER) + source.getFolderName();
		try {
			String fileName = baseFolder + SemTabTableCreator.FILE_NAME_PAIRS;

			if (useCompleteGraphs)
				fileName = baseFolder + SemTabTableCreator.FILE_NAME_PAIRS_DOMAIN_GRAPHS;

			if (mode != null && mode == Mode.TRAINING)
				fileName = baseFolder + SemTabTableCreator.FILE_NAME_PAIRS_TRAINING;
			else if (mode != null && mode == Mode.TEST)
				fileName = baseFolder + SemTabTableCreator.FILE_NAME_PAIRS_TEST;
			for (String line : FileUtils.readLines(new File(fileName), "UTF-8")) {
				String[] parts = line.split("\t");
				EvaluationInstance pair = new EvaluationInstance(Config.getPath(parts[0]), Config.getPath(parts[2]),
						Config.getPath(parts[3]));
				pairs.add(pair);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return pairs;
	}

}
