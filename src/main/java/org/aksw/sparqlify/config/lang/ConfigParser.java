package org.aksw.sparqlify.config.lang;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.validation.LoggerCount;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConfigParser {
	//private static final Logger logger = LoggerFactory.getLogger("Parser");

	
	public Config parse(InputStream in, Logger logger)
			throws IOException, RecognitionException
	{
		CharStream cs = new ANTLRInputStream(in);
		
		SparqlifyConfigLexer lexer = new SparqlifyConfigLexer(cs);
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		

		SparqlifyConfigParser parser = new SparqlifyConfigParser(tokens);

		parser.setLogger(logger);
		
		CommonTree ast = (CommonTree)parser.sparqlifyConfig().getTree();

		//printAst(ast, 0);

		SparqlifyConfigTree treeParser = new SparqlifyConfigTree(new CommonTreeNodeStream(ast));
		Config config = treeParser.sparqlifyConfig();
		
		
		return config;
	}
}
