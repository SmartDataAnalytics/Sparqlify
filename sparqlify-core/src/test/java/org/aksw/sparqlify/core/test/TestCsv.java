package org.aksw.sparqlify.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.sparqlify.config.syntax.NamedViewTemplateDefinition;
import org.aksw.sparqlify.config.syntax.TemplateConfig;
import org.aksw.sparqlify.config.syntax.ViewTemplateDefinition;
import org.aksw.sparqlify.csv.CsvMapperCliMain;
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
				result = ' ';
			} else {				
				result = cellDelimiterStr.charAt(0);
			}
		}
		
		return result;
	}
	
	@Test
	public void runTest() throws IOException, RecognitionException {
		
		Resource configRes = testBundle.getConfig();
		
		// FIXME Add viewName to config
		Character cellDelimiter = null;
		Character quoteCharacter = null;
		
		if(configRes != null) {
			InputStream in = configRes.getInputStream();
			Properties config = new Properties();
			config.load(in);
						
			cellDelimiter = readChar(config, "cellDelimiter");
			quoteCharacter = readChar(config, "quoteCharacter");
		}
		
		
		InputStream csvIn = testBundle.getCsv().getInputStream();
		Reader reader = new InputStreamReader(csvIn);
		ResultSet rs = CsvMapperCliMain.createResultSetFromCsv(reader, cellDelimiter, quoteCharacter);
		
		
		InputStream mappingIn = testBundle.getMapping().getInputStream();
		LoggerCount loggerCount = new LoggerCount(logger);
		TemplateConfig tc = CsvMapperCliMain.readTemplateConfig(mappingIn, loggerCount);
		
		Map<String, NamedViewTemplateDefinition> viewIndex = CsvMapperCliMain.indexViews(tc.getDefinitions(), loggerCount);
		ViewTemplateDefinition view = CsvMapperCliMain.pickView(viewIndex, null);
		
		 
		TripleIteratorTracking it = CsvMapperCliMain.createTripleIterator(rs, view);
		SparqlFormatterUtils.writeText(System.out, it);
		
		//CsvMapperCliMain. convertCsvToRdf(rs, view);
		
		System.out.println("Running " + testBundle);
	}
}
