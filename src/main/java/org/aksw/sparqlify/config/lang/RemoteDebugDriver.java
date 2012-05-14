package org.aksw.sparqlify.config.lang;

import org.aksw.sparqlify.config.syntax.Config;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;

public class RemoteDebugDriver {
	public static void main(String[] args) throws Exception {

		CommonTokenStream tokens = new CommonTokenStream();
		{
			ANTLRInputStream input = new ANTLRInputStream(System.in);
			SparqlifyConfigLexer lexer = new SparqlifyConfigLexer(input);
			tokens.setTokenSource(lexer);
		}

		CommonTreeNodeStream nodes;
		{
			SparqlifyConfigParser parser = new SparqlifyConfigParser(tokens);
			CommonTree ast = (CommonTree) parser.sparqlifyConfig().getTree();

			nodes = new CommonTreeNodeStream(ast);
		}

		{
			SparqlifyConfigTree walker = new SparqlifyConfigTree(nodes);

			// remove these lines if not using templates
			/*
			FileReader groupFileReader = new FileReader("cpp.stg");
			StringTemplateGroup templates = new StringTemplateGroup(
					groupFileReader);
			groupFileReader.close();
			// walker.setTemplateLib(templates);
			*/
			
			System.out.println("Waiting");
			Config example = walker.sparqlifyConfig();
			System.out.println(example.toString());
		}
		System.exit(0);

	}
}
