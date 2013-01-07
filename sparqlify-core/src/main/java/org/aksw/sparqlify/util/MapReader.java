package org.aksw.sparqlify.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapReader {
	private static final Logger logger = LoggerFactory
			.getLogger(MapReader.class);

	public static InputStream getResourceAsStream(String name) {
		InputStream result = MapReader.class.getResourceAsStream(name);

		return result;
	}

	public static Map<String, String> readFromResource(String name)
			throws IOException
	{
		InputStream in = getResourceAsStream(name);
		if(in == null) {
			throw new RuntimeException("Resource not found: " + name);
		}

		Map<String, String> result = read(in);
		
		return result;

	}
		
	public static Map<String, String> read(File file) throws IOException {
		InputStream in = new FileInputStream(file);
		Map<String, String> result = read(in);
		return result;		
	}

	public static Map<String, String> read(InputStream in)
		throws IOException
	{
		InputStreamReader reader = new InputStreamReader(in);
		Map<String, String> result = read(reader);
		return result;
	}
	
	public static Map<String, String> read(InputStreamReader reader)
			throws IOException
	{
		BufferedReader bufferedReader = new BufferedReader(reader);		
		Map<String, String> result = read(bufferedReader);
		return result;
	}
	
	public static Map<String, String> read(BufferedReader reader)
			throws IOException
	{
		Map<String, String> result = new HashMap<String, String>();

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


	
	@Deprecated
	public static Map<String, String> readFile(File file) throws IOException {

		return read(file);
		
	}
	/*
	 * public Map<String, String> read(File file) throws IOException { }
	 */
}