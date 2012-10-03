package org.aksw.sparqlify.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapReader {
	private static final Logger logger = LoggerFactory
			.getLogger(MapReader.class);

	public static Map<String, String> readFile(File file) throws IOException {

		Map<String, String> result = new HashMap<String, String>();

		
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		try {
		
			String line = null;
			while((line = reader.readLine()) != null) {
				
				line = line.trim();
				if(line.isEmpty() || line.startsWith("#")) {
					continue;
				}
				
				String[] parts = line.split("\\s+", 2);
				if(parts.length != 2) {
					logger.debug("No mapping for: " + parts[0]);
					continue;
				}
				
				
				result.put(parts[0], parts[1]);				
			}
		} finally {
			reader.close();
		}
	
		return result;
		
	}
	/*
	 * public Map<String, String> read(File file) throws IOException { }
	 */
}