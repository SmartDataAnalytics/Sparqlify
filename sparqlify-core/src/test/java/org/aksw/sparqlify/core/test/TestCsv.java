package org.aksw.sparqlify.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.sparqlify.config.syntax.NamedViewTemplateDefinition;
import org.aksw.sparqlify.config.syntax.TemplateConfig;
import org.aksw.sparqlify.config.syntax.ViewTemplateDefinition;
import org.aksw.sparqlify.csv.CsvMapperCliMain;
import org.aksw.sparqlify.csv.CsvParserConfig;
import org.aksw.sparqlify.csv.InputSupplierCSVReader;
import org.aksw.sparqlify.csv.TripleIteratorTracking;
import org.aksw.sparqlify.validation.LoggerCount;
import org.aksw.sparqlify.web.SparqlFormatterUtils;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.io.InputSupplier;


class InputSupplierResourceStream
	implements InputSupplier<InputStream>
{
	private Resource resource;
	
	public InputSupplierResourceStream(Resource resource) {
		this.resource = resource;
	}

	@Override
	public InputStream getInput() throws IOException {
		return resource.getInputStream();
	}
}

class InputSupplierResourceReader
	implements InputSupplier<Reader>
{
	private Resource resource;
	
	public InputSupplierResourceReader(Resource resource) {
		this.resource = resource;
	}
	
	@Override
	public Reader getInput() throws IOException {
		InputStream in = resource.getInputStream();
		Reader result = new InputStreamReader(in);
		return result;
	}
}




@RunWith(value = Parameterized.class)
public class TestCsv {

	private static final Logger logger = LoggerFactory.getLogger(TestCsv.class);
	
	private TestBundleCsv testBundle;
	
	public TestCsv(TestBundleCsv testBundle) {
		this.testBundle = testBundle;
	}
	
	@Parameters
	public static Collection<Object[]> data()
			throws IOException
	{
		TestBundleReaderCsv testBundleReader = new TestBundleReaderCsv();
		List<TestBundleCsv> testBundles = testBundleReader.getTestBundles();
		
		logger.debug(testBundles.size() + " test cases detected.");
		
		for(int i = 0; i < testBundles.size(); ++i) {
			TestBundleCsv testBundle = testBundles.get(i);
			logger.trace("Test Case #" + i + ": " + testBundle);
		}
		
		Object data[][] = new Object[testBundles.size()][1];
		
		
		for(int i = 0; i < testBundles.size(); ++i) {
			data[i][0] = testBundles.get(i);
		}

		Collection<Object[]> result = Arrays.asList(data); 
		
		return result;
	}

	
	Character readChar(Properties properties, String key) {
		Character result;
		String cellDelimiterStr = properties.getProperty(key);
		if(cellDelimiterStr == null) {
			result = null;
		}
		else {
			cellDelimiterStr = cellDelimiterStr.trim();
			
			if(cellDelimiterStr.length() > 1) {
				throw new RuntimeException("Delimiter must only be 1 char");
			}
			else if(cellDelimiterStr.isEmpty()) {
				//result = ' ';
				result = null;
			} else {				
				result = cellDelimiterStr.charAt(0);
			}
		}
		
		return result;
	}
	
	public static Boolean tryParseBoolean(String str, Boolean def) {
		Boolean result = def;
		
		try {
			if(str != null) {
				result = Boolean.parseBoolean(str);
			}
		} catch(Exception e) {
			logger.warn("Defaulting to '" + result + "' because could not parse string as boolean: '" + str + "'");
		}

		return result;
	}
	
	@Test
	public void runTest() throws IOException, RecognitionException, SQLException {
		
		Resource configRes = testBundle.getConfig();
		
		// FIXME Add viewName to config
		
		CsvParserConfig csvConfig = new CsvParserConfig();
		boolean firstRowAsColumnHeaders = false;
		if(configRes != null) {
			InputStream in = configRes.getInputStream();
			Properties config = new Properties();
			config.load(in);
			
			csvConfig.setFieldDelimiter(readChar(config, "fieldDelimiter"));
			csvConfig.setFieldSeparator(readChar(config, "fieldSeparator"));
			csvConfig.setEscapeCharacter(readChar(config, "escapeCharacter"));
			
			firstRowAsColumnHeaders = tryParseBoolean(config.getProperty("headers"), false); 
		}
		
		InputSupplier<Reader> readerSupplier = new InputSupplierResourceReader(testBundle.getCsv()); 
		InputSupplier<CSVReader> csvReaderSupplier = new InputSupplierCSVReader(readerSupplier, csvConfig);
		ResultSet rs = CsvMapperCliMain.createResultSetFromCsv(csvReaderSupplier, firstRowAsColumnHeaders, 100);
		
		/*
		InputStream csvIn = testBundle.getCsv().getInputStream();
		Reader r = new InputStreamReader(csvIn);
		BufferedReader br = new BufferedReader(r);
		ReaderSkipEmptyLines reader = new ReaderSkipEmptyLines(br);
		
		/*
		BufferedReader x = new BufferedReader(reader);
		String sigh;
		while((sigh = x.readLine()) != null) {
			System.out.println("BR Line: " + sigh);
		}* /
		
		Csv csv = new Csv();
		csvConfig.configure(csv);

		ResultSet rs = csv.read(reader, null);
		*/
		/*
		ResultSetMetaData meta = rs.getMetaData();
		while(rs.next()) {
			rs.next();
			
			List<Object> fields = new ArrayList<Object>();
			for(int i = 0; i < meta.getColumnCount(); ++i) {
				fields.add(rs.getObject(i + 1));
			}
			
			String str = Joiner.on(", ").join(fields);
			System.out.println(str);
		}
		*/
		//ResultSet rs = CsvMapperCliMain.createResultSetFromCsv(reader, fieldDelimiter, quoteCharacter);
		
		
		InputStream mappingIn = testBundle.getMapping().getInputStream();
		LoggerCount loggerCount = new LoggerCount(logger);
		TemplateConfig tc = CsvMapperCliMain.readTemplateConfig(mappingIn, loggerCount);
		
		Map<String, NamedViewTemplateDefinition> viewIndex = CsvMapperCliMain.indexViews(tc.getDefinitions(), loggerCount);
		ViewTemplateDefinition view = CsvMapperCliMain.pickView(viewIndex, null);
		
		 
		TripleIteratorTracking it = CsvMapperCliMain.createTripleIterator(rs, view);
		SparqlFormatterUtils.writeText(System.out, it);
		
		//System.err.println("-------------------");
		//CsvMapperCliMain. convertCsvToRdf(rs, view);
		
		logger.info("Running " + testBundle);
	}
}
