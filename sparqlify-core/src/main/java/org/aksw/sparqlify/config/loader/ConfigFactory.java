package org.aksw.sparqlify.config.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.validation.LoggerCount;
import org.antlr.runtime.RecognitionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigFactory {
	private static final Logger logger = LoggerFactory
			.getLogger(ConfigFactory.class);
	
	private File configFile;
	
	
	/**
	 * Static convenience method 
	 * 
	 * @param configFile
	 * @return
	 * @throws IOException
	 * @throws RecognitionException
	 */
	public static Config create(File configFile) throws IOException, RecognitionException {
		ConfigFactory factory = new ConfigFactory();
		factory.setConfigFile(configFile);
		Config result = factory.create();
		
		return result;
	}
	
	
	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}
	
	public File getConfigFile() {
		return configFile;
	}
	
	public Config create() throws IOException,
			RecognitionException {

		if (!configFile.exists()) {
			logger.error("File does not exist: " + configFile.getAbsolutePath());
		}

		LoggerCount loggerCount = new LoggerCount(logger);

		ConfigParser parser = new ConfigParser();

		InputStream in = new FileInputStream(configFile);
		Config config;
		try {
			config = parser.parse(in, loggerCount);
		} finally {
			in.close();
		}

		return config;
	}
}
