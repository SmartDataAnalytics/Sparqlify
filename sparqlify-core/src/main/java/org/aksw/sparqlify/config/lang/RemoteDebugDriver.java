package org.aksw.sparqlify.config.lang;

import org.aksw.sparqlify.config.syntax.Config;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteDebugDriver {
	private static final Logger logger = LoggerFactory.getLogger(RemoteDebugDriver.class);
	
	public static void main(String[] args) throws Exception {

		logger.debug("Starting Remote Debugger");
		
		CommonTokenStream tokens = new CommonTokenStream();

		ANTLRInputStream input = new ANTLRInputStream(System.in);
		SparqlifyConfigLexer lexer = new SparqlifyConfigLexer(input);
		tokens.setTokenSource(lexer);


		SparqlifyConfigParser parser = new SparqlifyConfigParser(tokens);
		CommonTree ast = (CommonTree) parser.sparqlifyConfig().getTree();

		CommonTreeNodeStream nodes = new CommonTreeNodeStream(ast);

		SparqlifyConfigTree walker = new SparqlifyConfigTree(nodes);

		// remove these lines if not using templates
		/*
		 * FileReader groupFileReader = new FileReader("cpp.stg");
		 * StringTemplateGroup templates = new StringTemplateGroup(
		 * groupFileReader); groupFileReader.close(); //
		 * walker.setTemplateLib(templates);
		 */

		logger.debug("Waiting");
		Config example = walker.sparqlifyConfig();
		logger.debug(example.toString());

		System.exit(0);
	}
}
