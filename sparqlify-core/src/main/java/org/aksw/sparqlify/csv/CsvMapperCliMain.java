package org.aksw.sparqlify.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.aksw.commons.sparql.api.core.ConstructIterator;
import org.aksw.sparqlify.algebra.sparql.transform.SparqlSubstitute;
import org.aksw.sparqlify.algebra.sql.nodes.VarDef;
import org.aksw.sparqlify.config.lang.TemplateConfigParser;
import org.aksw.sparqlify.config.syntax.NamedViewTemplateDefinition;
import org.aksw.sparqlify.config.syntax.TemplateConfig;
import org.aksw.sparqlify.config.syntax.ViewTemplateDefinition;
import org.aksw.sparqlify.core.IteratorResultSetSparqlifyBinding;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.ResultSetSparqlify;
import org.aksw.sparqlify.validation.LoggerCount;
import org.aksw.sparqlify.web.HttpSparqlEndpoint;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.h2.tools.Csv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.ModelUtils;

class NodeUtils {
	public static String toNTriplesString(Node node) {
		String result; 
		if(node.isURI()) {
			result = "<" + node.getURI() + ">";
		}
		else if(node.isLiteral()) {
			String lex = node.getLiteralLexicalForm();
			String lang = node.getLiteralLanguage();
			String dt = node.getLiteralDatatypeURI();
			
			String encoded = lex.replace("\"", "\\\"");
			
			result =  "\"" + encoded + "\"";
			
			if(!StringUtils.isEmpty(dt)) {
				result = result + "^^<" + dt+ ">";  
			} else {
				if(!lang.isEmpty()) {
					result = result + "@" + lang;
				}
			}			
		}
		else if(node.isBlank()) {
			result = node.getBlankNodeLabel();
		} else {
			throw new RuntimeException("Should not happen");
		}
		
		return result;
	}
}

class TripleUtils {
	public static String toNTripleString(Triple triple) {
		String s = NodeUtils.toNTriplesString(triple.getSubject());
		String p = NodeUtils.toNTriplesString(triple.getPredicate());
		String o = NodeUtils.toNTriplesString(triple.getObject());
		
		String result = s + " " + p + " " + o + " .\n";
		
		return result;
	}
}


public class CsvMapperCliMain {

	private static final Logger logger = LoggerFactory
			.getLogger(CsvMapperCliMain.class);

	private static final Options cliOptions = new Options();

	/**
	 * @param exitCode
	 */
	public static void printHelpAndExit(int exitCode) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(HttpSparqlEndpoint.class.getName(), cliOptions);
		System.exit(exitCode);
	}

	public static File extractFile(CommandLine commandLine, String optionName) {
		// Option option = commandLine.getO(optionName);

		String filename = commandLine.getOptionValue(optionName);

		String optionLabel = optionName;

		if (filename == null) {
			logger.error("No file given for option: " + optionLabel);

			printHelpAndExit(-1);
		}

		File file = new File(filename);
		if (!file.exists()) {
			logger.error("File given as argument for option " + optionLabel
					+ " does not exist: " + filename);

			printHelpAndExit(-1);
		}

		return file;
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		/*
		PropertyConfigurator.configure("log4j.properties");
		LogManager.getLogManager().readConfiguration(
				new FileInputStream("jdklog.properties"));
		*/

		CommandLineParser cliParser = new GnuParser();

		// cliOptions.addOption("P", "port", true, "Server port");
		// cliOptions.addOption("C", "context", true,
		// "Context e.g. /sparqlify");
		// cliOptions.addOption("B", "backlog", true,
		// "Maximum number of connections");
		//
		// cliOptions.addOption("t", "type", true,
		// "Database type (posgres, mysql,...)");
		// cliOptions.addOption("d", "database", true, "Database name");
		// cliOptions.addOption("u", "username", true, "");
		// cliOptions.addOption("p", "password", true, "");
		// cliOptions.addOption("h", "hostname", true, "");

		cliOptions.addOption("c", "config", true, "Sparqlify config file");
		cliOptions.addOption("f", "config", true, "Input data file");
		cliOptions.addOption("v", "config", true, "View name (only needed if config contains more than one view)");

		CommandLine commandLine = cliParser.parse(cliOptions, args);

		File configFile = extractFile(commandLine, "c");

		File dataFile = extractFile(commandLine, "f");

		String viewName = StringUtils.trim(commandLine.getOptionValue("v"));
		
		// Iterator<List<String>> it = getCsvIterator(dataFile, "\t");

//		while (it.hasNext()) {
//			List<String> line = it.next();
//
//			System.out.println(line);
//
//		}
		
		LoggerCount loggerCount = new LoggerCount(logger);


		TemplateConfigParser parser = new TemplateConfigParser();

		InputStream in = new FileInputStream(configFile);
		TemplateConfig config;
		try {
			config = parser.parse(in, loggerCount);
		} finally {
			in.close();
		}

		
		logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());
		
		if(loggerCount.getErrorCount() > 0) {
			throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
		}

	
		//System.out.println("Test here");

		// TODO Move the method to a better place
		RdfViewSystemOld.initSparqlifyFunctions();
	
		ResultSet rs = new Csv().read(dataFile.getAbsolutePath(), null, null);
		//ResultSetMetaData meta = rs.getMetaData();

		/*
		for(int i = 1; i <= meta.getColumnCount(); ++i) {
			System.out.println(meta.getColumnName(i));
		}
		*/

		List<NamedViewTemplateDefinition> views = config.getDefinitions();
		
		if(views.isEmpty()) {
			logger.warn("No view definitions found");
		}
		
		// Index the views by name
		Map<String, NamedViewTemplateDefinition> nameToView = new HashMap<String, NamedViewTemplateDefinition>();
		for(NamedViewTemplateDefinition view : views) {
			String name = view.getName();
			
			if(nameToView.containsKey(name)) {
				logger.warn("Omitting duplicate view definition: " + name);
			}
			
			nameToView.put(name, view);
		}
		
		
		ViewTemplateDefinition view = null;
		if(StringUtils.isEmpty(viewName)) {
			if(views.size() == 1) {
				view = views.get(0);
			} else {
				logger.error("Multiple views present in config file; please specify which to use");
				printHelpAndExit(1);
			}
		} else {
			view = nameToView.get(viewName);
			if(view == null) {
				logger.error("View '" + viewName + "' not found in config file");
				System.exit(1);
			}
		}
		
		VarExprList varExprs = view.getVarExprList();
		
		List<String> vars = new ArrayList<String>();
		for(Var var : varExprs.getVars()) {
			vars.add(var.getName());
		}
		
		Multimap<Var, VarDef> sparqlVarMap = HashMultimap.create();
		for(Entry<Var, Expr> entry : varExprs.getExprs().entrySet()) {
			
			Expr e = SparqlSubstitute.substituteExpr(entry.getValue());
			//Expr e = FunctionExpander.transform(ex);
			//System.out.println(e);
			
			sparqlVarMap.put(entry.getKey(), new VarDef(e));
		}
		
		Iterator<Binding> itBinding = new IteratorResultSetSparqlifyBinding(rs, sparqlVarMap, 1, "rowId");
		ResultSetSparqlify rss = new ResultSetSparqlify(itBinding, vars, 0);
		

        // insertPrefixesInto(result) ;
        Template template = view.getConstructTemplate();
        
        //System.out.println(template.getTriples());
        

        Iterator<Triple> it = new ConstructIterator(template, rss);

        

        while(it.hasNext()) {
        	Triple t = it.next();
        	//logger.trace("Triple: " + t);

        	if(t.getSubject() == null || t.getPredicate() == null || t.getObject() == null) {
        		logger.warn("Omitting null statement, triple was: " + t);
        		continue;        		
        	}
        	
        	String str = TripleUtils.toNTripleString(t);
        	
        	System.out.println(str);
        }
		
        /*
		System.exit(0);
		
		
		for (ViewTemplateDefinition x : config.getDefinitions()) {
			System.out.println(x);

			// x.getConstructTemplate().
			VarExprList vel = x.getVarExprList();
			System.out.println(vel);
		}*/

	}

	public static Iterator<List<String>> getCsvIterator(File file,
			String columnSeparator) throws FileNotFoundException {
		// BufferedReader reader = new BufferedReader(new InputStreamReader(new
		// FileInputStream(file)));

		Iterator<List<String>> result = new CsvRowIterator(file);

		return result;
	}

	public static Iterator<List<String>> getXlsCsvIterator(File file,
			int sheetIndex) throws BiffException, IOException {
		WorkbookSettings ws = new WorkbookSettings();
		ws.setLocale(new Locale("en", "EN"));
		Workbook w = Workbook.getWorkbook(file, ws);

		// File f = new File("/tmp/new.csv");
		// OutputStream os = (OutputStream) new FileOutputStream(f);
		// String encoding = "UTF8";
		// OutputStreamWriter osw = new OutputStreamWriter(os, encoding);
		// BufferedWriter bw = new BufferedWriter(osw);

		if (sheetIndex >= w.getNumberOfSheets()) {
			throw new IndexOutOfBoundsException("Attemp to access sheet "
					+ sheetIndex + "/" + w.getNumberOfSheets());
		}

		Sheet s = w.getSheet(sheetIndex);

		Iterator<List<String>> result = new XlsRowIterator<String>(s, 0,
				CellToStringTransformer.getInstance(), w);

		return result;
	}

}
