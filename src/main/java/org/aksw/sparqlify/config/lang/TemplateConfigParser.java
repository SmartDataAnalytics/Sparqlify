package org.aksw.sparqlify.config.lang;

import java.io.IOException;
import java.io.InputStream;

import org.aksw.sparqlify.config.syntax.TemplateConfig;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author raven
 *
 */
public class TemplateConfigParser {
	private static final Logger logger = LoggerFactory.getLogger("Parser");

	public TemplateConfig parse(InputStream in)
			throws IOException, RecognitionException
	{
		CharStream cs = new ANTLRInputStream(in);
		
		SparqlifyConfigLexer lexer = new SparqlifyConfigLexer(cs);
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		

		SparqlifyConfigParser parser = new SparqlifyConfigParser(tokens);
		CommonTree ast = (CommonTree)parser.templateConfig().getTree();

		//printAst(ast, 0);

		SparqlifyConfigTree treeParser = new SparqlifyConfigTree(new CommonTreeNodeStream(ast));
		TemplateConfig config = treeParser.templateConfig();
		return config;
	} 
}
