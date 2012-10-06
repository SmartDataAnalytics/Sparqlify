package org.aksw.sparqlify.core;

import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FunctionDeclarationTests {

	private static final Logger logger = LoggerFactory.getLogger(FunctionDeclarationTests.class);
	
	@Test
	public void test() throws RecognitionException {
		ConfigParser parser = new ConfigParser();

		Config config = parser.parse("PREFIX ex:<http://ex.org> DECLARE FUNCTION ex:intersects(geometry ?a, geometry ?b) AS STINTERSECTS(?a, ?b)", logger);
		
		System.out.println(config.getFunctionDeclarations());
	}
}
