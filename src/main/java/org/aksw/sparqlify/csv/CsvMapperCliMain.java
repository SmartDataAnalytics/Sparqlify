package org.aksw.sparqlify.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.logging.LogManager;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.aksw.commons.sparql.api.core.ConstructIterator;
import org.aksw.sparqlify.algebra.sparql.transform.FunctionExpander;
import org.aksw.sparqlify.algebra.sparql.transform.SparqlSubstitute;
import org.aksw.sparqlify.algebra.sql.nodes.TermDef;
import org.aksw.sparqlify.config.lang.TemplateConfigParser;
import org.aksw.sparqlify.config.syntax.TemplateConfig;
import org.aksw.sparqlify.config.syntax.ViewTemplateDefinition;
import org.aksw.sparqlify.core.IteratorResultSetSparqlifyBinding;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.ResultSetSparqlify;
import org.aksw.sparqlify.rest.HttpSparqlEndpoint;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.PropertyConfigurator;
import org.h2.tools.Csv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.util.ModelUtils;

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
		PropertyConfigurator.configure("log4j.properties");
		LogManager.getLogManager().readConfiguration(
				new FileInputStream("jdklog.properties"));

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

		CommandLine commandLine = cliParser.parse(cliOptions, args);

		File configFile = extractFile(commandLine, "c");

		File dataFile = extractFile(commandLine, "f");

		// Iterator<List<String>> it = getCsvIterator(dataFile, "\t");

//		while (it.hasNext()) {
//			List<String> line = it.next();
//
//			System.out.println(line);
//
//		}

		TemplateConfigParser parser = new TemplateConfigParser();

		InputStream in = new FileInputStream(configFile);
		TemplateConfig config;
		try {
			config = parser.parse(in);
		} finally {
			in.close();
		}

	
		// TODO Move the method to a better place
		RdfViewSystemOld.initSparqlifyFunctions();
	
		ResultSet rs = new Csv().read(dataFile.getAbsolutePath(), null, null);
		//ResultSetMetaData meta = rs.getMetaData();

		/*
		for(int i = 1; i <= meta.getColumnCount(); ++i) {
			System.out.println(meta.getColumnName(i));
		}
		*/

		
		ViewTemplateDefinition view = config.getDefinitions().get(0);
		VarExprList varExprs = view.getVarExprList();
		
		List<String> vars = new ArrayList<String>();
		for(Var var : varExprs.getVars()) {
			vars.add(var.getName());
		}
		
		Multimap<Var, TermDef> sparqlVarMap = HashMultimap.create();
		for(Entry<Var, Expr> entry : varExprs.getExprs().entrySet()) {
			
			Expr e = SparqlSubstitute.substituteExpr(entry.getValue());
			//Expr e = FunctionExpander.transform(ex);
			System.out.println(e);
			
			sparqlVarMap.put(entry.getKey(), new TermDef(e));
		}
		
		Iterator<Binding> itBinding = new IteratorResultSetSparqlifyBinding(rs, sparqlVarMap);
		ResultSetSparqlify rss = new ResultSetSparqlify(itBinding, vars, 0);
		
		

        // insertPrefixesInto(result) ;
        Template template = view.getConstructTemplate();

        Iterator<Triple> it = new ConstructIterator(template, rss);
        
        Model result = ModelFactory.createDefaultModel();
        
        while(it.hasNext()) {
        	Triple t = it.next();
        	logger.debug("Triple: " + t);
        	        	
        	Statement stmt = ModelUtils.tripleToStatement(result, t);

        	if(stmt == null) {
        		logger.warn("Omitting null statement, triple was: " + t);
        		continue;
        	}

        	
        	result.add(stmt);
        }
        
        result.write(System.out, "N-TRIPLES");
		
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
