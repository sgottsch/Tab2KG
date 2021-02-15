package de.l3s.simpleml.tab2kg.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

import de.l3s.simpleml.tab2kg.graph.TypeGraphBuilder;

public class FileLoader {

	public static String readResourceFileToString(String fileName) throws IOException {
		if (!Config.isLocal())
			fileName = "resources/" + fileName;
		InputStream is = TypeGraphBuilder.class.getResourceAsStream("/" + fileName);
		return IOUtils.toString(is, StandardCharsets.UTF_8);
	}

}
