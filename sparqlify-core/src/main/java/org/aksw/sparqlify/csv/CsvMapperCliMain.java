package org.aksw.sparqlify.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
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
import org.aksw.commons.sparql.api.core.ResultSetClosable;
import org.aksw.sparqlify.algebra.sparql.transform.SparqlSubstitute;
import org.aksw.sparqlify.config.lang.TemplateConfigParser;
import org.aksw.sparqlify.config.syntax.NamedViewTemplateDefinition;
import org.aksw.sparqlify.config.syntax.TemplateConfig;
import org.aksw.sparqlify.config.syntax.ViewTemplateDefinition;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.ResultSetSparqlify;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.sparql.IteratorResultSetSparqlifyBinding;
import org.aksw.sparqlify.util.QuadPatternUtils;
import org.aksw.sparqlify.validation.LoggerCount;
import org.aksw.sparqlify.web.HttpSparqlEndpoint;
import org.aksw.sparqlify.web.SparqlFormatterUtils;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.lang.StringUtils;
import org.h2.tools.Csv;
import org.openjena.atlas.lib.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.core.VarExprList;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.syntax.Template;

/**
 * Wraps a opencsv CSVReader as a java Reader object.
 * User for passing the data into H2 CSV utils.
 * 
 * @author raven
 *
 */
class ReaderCSVReader
	extends ReaderStringBase
{
	private static final String newLine = "\n";
	private static final Joiner joiner = Joiner.on("\",\"");

	private CSVReader csvReader;
	//CSVWriter writer = new CSVWriter(


	public ReaderCSVReader(CSVReader csvReader) {
		this.csvReader = csvReader;
	}
	
	public static String encodeCell(String cell) {
		String result = cell.replace("\"", "\\\"");
		return result;
	}
	
	public static String[] encodeCells(String[] cells) {
		String[] result = new String[cells.length];
		for(int i = 0; i < cells.length; ++i) {
			String rawCell = cells[i];
			result[i] = encodeCell(rawCell);
		}
		
		return result;
	}
	
	public static String createLine(String[] cells) {
		String[] encodedCells = encodeCells(cells);
		String result = "\"" + joiner.join(encodedCells) + "\"" + newLine;
		return result;
	}
	
	@Override
	protected String nextString() {
		try {
			String[] strs = csvReader.readNext();
			
			String result;
			if(strs == null) {
				result = null;
			} else if(strs.length == 0) {
				result = newLine;
			} else if(strs.length == 1) {
				String str = strs[0];
				if(str.isEmpty()) {
					result = newLine;
				} else {
					result = createLine(strs); 
				}
			}
			else {
				result = createLine(strs);
			}

			System.out.println(result);
			return result;
		} catch(Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
	@Override
	public void close() throws IOException {
		csvReader.close();
	}
}


/**
 * AbstractBase class for turning anything that returns strings into a reader.
 * 
 * @author raven
 *
 */
abstract class ReaderStringBase
	extends Reader
{	
	private String line;
	private int offset = 0; // set to -1 when done
	
	@Override
	public int read(char[] cbuf, int off, int len)
			throws IOException
	{	
		if(offset < 0) {
			return -1;
		}
		
		int initOff = off;
		
		while(len > 0) {
			int lineLen = line == null ? 0 : line.length();
			int lineAvailLen = lineLen - offset;

			int readLen = Math.min(lineAvailLen, len);
			if(readLen > 0) {
				line.getChars(offset, offset + readLen, cbuf, off);
				off += readLen;
				len -= readLen;
				lineAvailLen -= readLen;
			}
			
			if(len > 0 && lineAvailLen <= 0) {
				line = nextString();
				if(line == null) {
					offset = -1;
					break;
				}
				offset = 0;
			}
		}
		
		int result = off - initOff;

		return result;
	}
	
	abstract protected String nextString();
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

	public static boolean isNullOrVar(Node node) {
		return node == null || node.isVariable();
	}
	
	public static boolean containsNullOrVar(Triple triple) {
		boolean s = isNullOrVar(triple.getSubject());
		boolean p = isNullOrVar(triple.getPredicate());
		boolean o = isNullOrVar(triple.getObject());
		
		boolean result = s || p || o; 
		return result;
	}
	
	public static void countVariable(Node node, Map<Var, Integer> countMap) {
		if(node == null) {
			MapUtils.increment(countMap, null);
		}
		else if(node.isVariable()) {
			MapUtils.increment(countMap, (Var)node);
		}
	}
	
	
	public static void show(Reader reader) {
		BufferedReader br = new BufferedReader(reader);
		String line;
		try {
			while((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static TemplateConfig readTemplateConfig(InputStream in, Logger loggerCount)
			throws IOException, RecognitionException
	{
		TemplateConfigParser parser = new TemplateConfigParser();

		TemplateConfig config;
		try {
			config = parser.parse(in, loggerCount);
		} finally {
			in.close();
		}
		
		return config;
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

		cliOptions.addOption("d", "config", true, "CSV cell delimiter (default is ',')");

		CommandLine commandLine = cliParser.parse(cliOptions, args);

		File configFile = extractFile(commandLine, "c");

		File dataFile = extractFile(commandLine, "f");

		String viewName = StringUtils.trim(commandLine.getOptionValue("v"));

		LoggerCount loggerCount = new LoggerCount(logger);
		

		Character cellDelimiter = null;
		String cellDelimiterStr = commandLine.getOptionValue("d", null);
		if(!StringUtils.isEmpty(cellDelimiterStr)) {
			if(cellDelimiterStr.length() > 1) {
				loggerCount.error("Cell Delimiter may only be a singe character. Given argument is: '" + cellDelimiterStr + "'");
				printHelpAndExit(1);
			}

			cellDelimiter = cellDelimiterStr.charAt(0);
		}

		InputStream in = new FileInputStream(configFile);
		TemplateConfig config;
		try {
			config = readTemplateConfig(in, loggerCount);
		} finally {
			in.close();
		}

		
		logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());

		if(loggerCount.getErrorCount() > 0) {
			throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
		}

		List<NamedViewTemplateDefinition> views = config.getDefinitions();
		
		if(views.isEmpty()) {
			logger.warn("No view definitions found");
		}
		
		
		// Index the views by name
		Map<String, NamedViewTemplateDefinition> viewIndex = indexViews(views, loggerCount);
		ViewTemplateDefinition view;
		
		view = pickView(viewIndex, viewName);
//		if(view == null) {
//			logger.error("View '" + viewName + "' not found in config file");
//			System.exit(1);
//		}
		
		Reader fileReader = new FileReader(dataFile);
		//convertCsvToRdf(fileReader, view);
		
		ResultSet resultSet = createResultSetFromCsv(fileReader, cellDelimiter, null);

		TripleIteratorTracking trackingIt = createTripleIterator(resultSet, view);
		

		//writeTriples(System.out, trackingIt);
		SparqlFormatterUtils.writeText(System.out, trackingIt);
		writeSummary(System.err, trackingIt.getState());
		
		
		//convertCsvToRdf(resultSet, view);
	}
	
	
	public static Map<String, NamedViewTemplateDefinition> indexViews(List<NamedViewTemplateDefinition> views, Logger loggerCount) {
		
		// Index the views by name
		Map<String, NamedViewTemplateDefinition> nameToView = new HashMap<String, NamedViewTemplateDefinition>();
		for(NamedViewTemplateDefinition view : views) {
			String name = view.getName();
			
			if(nameToView.containsKey(name)) {
				loggerCount.warn("Omitting duplicate view definition: " + name);
			}
			
			nameToView.put(name, view);
		}
		
		return nameToView;
	}
	
	public static ViewTemplateDefinition pickView(Map<String, NamedViewTemplateDefinition> index, String viewName)
	{
		ViewTemplateDefinition view = null;
		if(StringUtils.isEmpty(viewName)) {
			if(index.size() == 1) {
				view = index.values().iterator().next();
			} else {
				throw new RuntimeException("Multiple views exist. Please specify which one to use");
				//logger.error("Multiple views present in config file; please specify which to use");
				//printHelpAndExit(1);
			}
		} else {
			view = index.get(viewName);
			if(view == null) {
				throw new RuntimeException("View '" + viewName + "' not found");
//				//logger.error("View '" + viewName + "' not found in config file");
//				//System.exit(1);
//
			}
		}

		return view;
	}
	
	
	public static ResultSet createResultSetFromCsv(Reader reader, Character cellDelimiter, Character quoteCharacter)
			throws IOException
	{
		if(cellDelimiter != null || quoteCharacter != null) {
			char cellDelim = cellDelimiter == null ? CSVParser.DEFAULT_SEPARATOR : cellDelimiter;
			char quoteChar = quoteCharacter == null ? CSVParser.DEFAULT_QUOTE_CHARACTER : quoteCharacter;
			
			
			CSVReader csvReader = new CSVReader(reader, cellDelim, quoteChar);
			reader = new ReaderCSVReader(csvReader);	
		}
		
		Csv csv = new Csv();
		//csv.setEscapeCharacter('/');
		ResultSet result = csv.read(reader, null);
		return result;
	}
	
	public static TripleIteratorTracking createTripleIterator(ResultSet rs, ViewTemplateDefinition view) {

		
	
		//System.out.println("Test here");

		// TODO Move the method to a better place
		RdfViewSystemOld.initSparqlifyFunctions();
	
		//ResultSetMetaData meta = rs.getMetaData();

		/*
		for(int i = 1; i <= meta.getColumnCount(); ++i) {
			System.out.println(meta.getColumnName(i));
		}
		*/


		
		
		VarExprList varExprs = view.getVarExprList();
		
		List<String> vars = new ArrayList<String>();
		for(Var var : varExprs.getVars()) {
			vars.add(var.getName());
		}
		
		Multimap<Var, RestrictedExpr> sparqlVarMap = HashMultimap.create();
		for(Entry<Var, Expr> entry : varExprs.getExprs().entrySet()) {
			
			Expr e = SparqlSubstitute.substituteExpr(entry.getValue());
			//Expr e = FunctionExpander.transform(ex);
			//System.out.println(e);
			
			sparqlVarMap.put(entry.getKey(), new RestrictedExpr(e));
		}
		
		Iterator<Binding> itBinding = new IteratorResultSetSparqlifyBinding(rs, sparqlVarMap, 1, "rowId");
		ResultSetSparqlify rss = new ResultSetSparqlify(itBinding, vars, 0);
		

        // insertPrefixesInto(result) ;
        //Template template = view.getConstructTemplate();
        
		QuadPattern quadPattern = view.getConstructTemplate();
        
		BasicPattern bgp = QuadPatternUtils.toBasicPattern(quadPattern);
		Template template = new Template(bgp);
		
        
        //System.out.println(template.getTriples());
        
		ResultSetClosable closableRs = new ResultSetClosable(rss);
        Iterator<Triple> it = new ConstructIterator(template, closableRs);
        
        TripleIteratorTracking result = new TripleIteratorTracking(it);
        
        return result;
	}
    
	public static void writeSummary(PrintStream out, TripleIteratorState state) {
	
        int totalTripleCount = state.getTotalTripleCount();
        int tripleGenCount = state.getTripleGenCount();
        Map<Var, Integer> varCountMap = state.getVarCountMap();
        int omittedTripleCount = totalTripleCount - tripleGenCount;
        
        System.err.println("Varible\t#Unbound");
        for(Entry<Var, Integer> entry : varCountMap.entrySet()) {
        	Var var = entry.getKey();
        	Integer count = entry.getValue();
        	
        	System.err.println(var + ":\t" + count);
        }
        
        System.err.println("Triples generated:\t" + tripleGenCount);
        System.err.println("Potential triples omitted:\t" + omittedTripleCount);
        System.err.println("Triples total:\t" + totalTripleCount);
	}
	
//	public static void writeTriples(PrintStream out, Iterator<Triple> it) {
//        
//        while(it.hasNext()) {
//
//        	Triple t = it.next();
//        	String str = TripleUtils.toNTripleString(t);
//        	
//        	out.println(str);
//        }
		
        /*
		System.exit(0);
		
		
		for (ViewTemplateDefinition x : config.getDefinitions()) {
			System.out.println(x);

			// x.getConstructTemplate().
			VarExprList vel = x.getVarExprList();
			System.out.println(vel);
		}*/
//
//	}


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
