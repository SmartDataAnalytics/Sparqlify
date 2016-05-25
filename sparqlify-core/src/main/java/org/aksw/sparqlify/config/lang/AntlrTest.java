package org.aksw.sparqlify.config.lang;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.views.PatternUtils;
import org.aksw.jena_sparql_api.views.SparqlSubstitute;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeOld;
import org.aksw.sparqlify.algebra.sql.nodes.SqlQuery;
import org.aksw.sparqlify.algebra.sql.nodes.SqlTable;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.ViewDefinition;
import org.aksw.sparqlify.core.ConstraintContainer;
import org.aksw.sparqlify.core.RdfView;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.QuadPattern;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparql.FilterUtils;

/**
 * Parser for the 0.1 config format
 *
 * Meh, class org.apache.jena.sparql.lang.ParserSPARQL11
 * would be pretty hard to adapt; there seems to be no way
 * of getting a JavaCharStream into it in order to have
 * line-column numbers in the error messages.
 *  
 * How to work with antlr 
 * http://www.antlr.org/wiki/pages/viewpage.action?pageId=789
 * 
 * How to use tree grammars
 * http://jnb.ociweb.com/jnb/jnbJun2008.html#OurTreeGrammar
 * 
 * How to do custom error recovery
 * http://www.antlr.org/wiki/display/ANTLR3/Custom+Syntax+Error+Recovery
 * 
 * Tree construction, custom error nodes
 * http://www.antlr.org/wiki/display/ANTLR3/Tree+construction
 * @author raven
 *
 */
public class AntlrTest {
	
	private static final Logger logger = LoggerFactory.getLogger("Parser");
	
	public static final Pattern patternCreateView = Pattern.compile("Create\\s+View", Pattern.CASE_INSENSITIVE);

	public static void printAst(CommonTree ast, int ident) {
		String p = "";
		for(int i = 0; i < ident; ++ i) {
			p += " ";
		}
		
		//RDF.type.asNode()
		//Triple x;
		//x.get
		
		System.out.println(p + ast.getText() + " | " + ast.getType());
		//System.out.println(p + ast.getType() + " | " + ast.getToken() + " | " + ast.getText());
		//System.out.println(ast.get);
		
		
		for(int i = 0; i < ast.getChildCount(); ++i) {
			printAst((CommonTree)ast.getChild(i), ident + 2);
		}
	}
	
	public static void main(String[] args)
		throws Exception
	{
		System.out.println("Start");
		PropertyConfigurator.configure("log4j.properties");
		
		//File file = new File("data/wortschatz.sparqlify");
		//InputStream in = new FileInputStream(file);

		InputStream in = AntlrTest.class.getResourceAsStream("/sparqlifyb.txt");
		CharStream cs = new ANTLRInputStream(in);
		//CharStream csFile = new ANTLRFileStream("data/test.sparqlify");
		//CharStream csFile = new ANTLRFileStream("data/wortschatz.sparqlify");
		
		//Node.createURI("").
		//new E_IRI(expr);
		//new E_Function();
		String str = "?s ?p ?o . ";
		CharStream csStr = new ANTLRStringStream(str);
		
		//CharStream cs = csFile;
		//NotImplementedException
		
		SparqlifyConfigLexer lexer = new SparqlifyConfigLexer(cs);
		CommonTokenStream tokens = new CommonTokenStream();
		tokens.setTokenSource(lexer);
		

		SparqlifyConfigParser parser = new SparqlifyConfigParser(tokens);
		CommonTree ast = (CommonTree)parser.sparqlifyConfig().getTree();
		
		
		System.out.println("ast built");
		printAst(ast, 0);

		
		System.out.println("done with ast");
		TypeMapper x;
		//System.out.println(ast.toStringTree());
		//System.out.println(x.getStop());
		
		SparqlifyConfigTree treeParser = new SparqlifyConfigTree(new CommonTreeNodeStream(ast));
		Config config;
		try {
			config = treeParser.sparqlifyConfig();
		
		} catch(Exception e) {
			e.printStackTrace();
			
			System.out.println(treeParser.getRuleInvocationStack());
			return;
		}

		List<RdfView> result = new ArrayList<RdfView>();
		for(ViewDefinition item :config.getViewDefinitions()) {
			RdfView rdfView = RdfView.create(item);
			result.add(rdfView);
			System.out.println(item);
		}
		
		//Node.createLiteral("test");
		//NodeValue.makN
		//new E_Bound(expr)
		//new ExprVar()
		//ExprN
		//E_LangMatches
		//Node.createV
		//Node x = Node.createAnon(new AnonId("blah"));
		//NodeValue.makeDecimal(1).asNode()
		//Node.createLiteral()
		//Node.creat
		//NodeValue.makeDe
		//E_Function
		//PrefixMapping m;
		//m.ex
		//Node.createU
		//Node.create
		
		System.out.println(config.getViewDefinitions().size());
		//x.ex
		//final fsqTreeParser treeParser = new fsqTreeParser(new CommonTreeNodeStream(ast));
		//final Formula queryFormula = treeParser.formula();

		//Query query = new Query();
		//String queryStr = "Select * { ?s ?p ?o . } aoeueouao";
		//QueryFactory.parse(query, queryStr, null, Syntax.syntaxSPARQL);
		
		
		
		System.out.println("done");
//		blah.
		//parse(in);
	}
	
	public static void parse(InputStream in) {
		Scanner scanner = new Scanner(in);
		
		while(scanner.hasNext()) {
			String p = scanner.next(patternCreateView);
		
			System.out.println(p);
		}
		
	}
	
	
	
	public static RdfView create(String str) {
		Map<String, String> defaultPrefixes = new HashMap<String, String>();
		defaultPrefixes.put("bif", "http://bif/");
		defaultPrefixes.put("rdf", RDF.getURI());
		defaultPrefixes.put("rdfs", RDFS.getURI());
		defaultPrefixes.put("geo", "http://ex.org/");
		defaultPrefixes.put("beef", "http://aksw.org/beef/");
		
		defaultPrefixes.put("wso", "http://aksw.org/wortschatz/ontology/");
		//defaultPrefixes.put("beef", "http://aksw.org/beef/");
		defaultPrefixes.put("rdf", RDF.getURI());
		defaultPrefixes.put("owl", OWL.getURI());
		
		return create(str, defaultPrefixes);
	}

	
	public static RdfView create(String str, Map<String, String> defaultPrefixes) {		
		
		PrefixMapping defaultPrefixMapping = new PrefixMappingImpl();
		defaultPrefixMapping.setNsPrefixes(defaultPrefixes);		
		
		String parts1[] = str.split("\\swith\\s", 2);
		//String parts2[] = parts1[1].split("\\sselect\\s", 2);
		String parts2[] = parts1[1].split(";");
		
		String sqlStr = parts2[parts2.length - 1].trim();
		
		String queryStr = "Select * " + parts1[0];
		String bindingStrs[] = Arrays.copyOf(parts2, parts2.length - 1);
		//String sqlStr = "SELECT " + parts2[1];
		
		
		Query query = new Query();
		query.setPrefixMapping(defaultPrefixMapping);
		QueryFactory.parse(query, queryStr, null, Syntax.syntaxSPARQL);
		Op op = Algebra.compile(query);
		op = Algebra.toQuadForm(op);
		
		QuadPattern quadPattern = new QuadPattern();

		for(Quad quad : PatternUtils.collectQuads(op)) {
			quadPattern.add(quad);
		}
		
		//PatternUtils.
		
		
		Map<Node, Expr> binding = new HashMap<Node, Expr>();
		
		for(String bindingStr : bindingStrs) {
			Expr expr = ExprUtils.parse(bindingStr, defaultPrefixMapping);
			
			if(!(expr instanceof E_Equals)) {
				throw new RuntimeException("Binding expr must have form ?var = ... --- instead got: " + bindingStr);
			}
			
			// Do macro expansion
			// TODO Keep track of a non-macro-expanded version for human readability
			// and easier debugging
			
			Expr definition = expr.getFunction().getArg(2);
			definition = SparqlSubstitute.substituteExpr(definition);

			
			
			Var var = expr.getFunction().getArg(1).asVar();
			binding.put(var, definition);
		}
		
		
		System.out.println("Binding = " + binding);

		SqlNodeOld sqlExpr;
		if(sqlStr.startsWith("select")) {
			sqlExpr = new SqlQuery(null, sqlStr);
		} else {
			sqlExpr = new SqlTable(sqlStr);
		}
		
		ExprList filter = FilterUtils.collectExprs(op, new ExprList());
		return new RdfView("test", quadPattern, filter, binding, new ConstraintContainer(), sqlExpr);
	}

	
}
