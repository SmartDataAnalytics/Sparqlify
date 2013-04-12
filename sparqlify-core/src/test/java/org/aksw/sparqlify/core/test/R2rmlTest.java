package org.aksw.sparqlify.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.util.StreamUtils;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangNQuads;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Quad;



@RunWith(Parameterized.class)
public class R2rmlTest {

	private static final Logger logger = LoggerFactory.getLogger(R2rmlTest.class);
	
	//private Comparator<Resource> resourceComparator = new ResourceComparator();	
	//private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	private String name;
	private TestBundle testBundle;
	
	
	public R2rmlTest(String name, TestBundle testBundle) {
		this.name = name;
		this.testBundle = testBundle;
	}
	
	public String getName() {
		return name;
	}
	
	@Parameters(name = "Sparqlify R2RML Test {index}: {0}")
	public static Collection<Object[]> data()
			throws IOException
	{
		TestBundleReader testBundleReader = new TestBundleReader();
		List<TestBundle> testBundles = testBundleReader.getTestBundles();
		
		logger.debug(testBundles.size() + " test cases detected.");
		
		for(int i = 0; i < testBundles.size(); ++i) {
			TestBundle testBundle = testBundles.get(i);
			logger.trace("Test Case #" + i + ": " + testBundle);
		}
		
		Object data[][] = new Object[testBundles.size()][2];
		
		
		for(int i = 0; i < testBundles.size(); ++i) {
			TestBundle testBundle = testBundles.get(i);
			data[i][0] = testBundle.getName();
			data[i][1] = testBundles.get(i);
		}

		Collection<Object[]> result = Arrays.asList(data); 
		
		return result;
	}

//	@Test
//	public void runTests() throws Exception {
//
//		List<TestBundle> bundles = process();
//		
//		System.out.println("Final: " + bundles);
//		
//		
//		runBundles(bundles);
//	}
//
//	
//	public void runBundles(List<TestBundle> bundles)
//			throws Exception
//	{
//		for(TestBundle bundle : bundles) {
//			runBundle(bundle);
//		}
//	}

	
	// This test is more about experimenting with Jena in order to find out how to canonicalize values
//	@Test
//	public void runCanonicalizerTest() {
//		RDFDatatype dt = TypeMapper.getInstance().getTypeByName(XSD.xdouble.toString());
//		
//		Node a = Node.createLiteral("20.0e0", dt);
//		Node b = Node.createLiteral("2.0e1", dt);
//		
//		CanonicalizeLiteral canon = CanonicalizeLiteral.get();
//
//		a = canon.convert(a);
//		b = canon.convert(b);
//		
//		Assert.assertEquals(a, b);
//	}

	@Test
	public void runBundle()
			throws Exception
	{		
		runBundle(testBundle);
	}

	public Set<Quad> readNQuads(InputStream in) {

		SinkQuadsToSet quadSink = new SinkQuadsToSet();
		LangNQuads parser = RiotReader.createParserNQuads(in, quadSink);
		parser.parse();

		Set<Quad> result = quadSink.getQuads();
		return result;
	}

	
	public void runQueryBundle(TestBundle testBundle, QueryBundle queryBundle) {
		
	}
	
	
	/**
	 * - Create database from resource
	 * - Create SparqlSqlRewriterSparqlify from resource 
	 * - Load nquads file from resource (how?)
	 * 
	 * 
	 * - Run dump on the rewriter (just create a set of quads)
	 * - Compare the results
	 * 
	 * @param bundle
	 * @throws IOException 
	 */
	public void runBundle(TestBundle bundle)
			throws Exception
	{
		System.out.println("---------------------------------");
		System.out.println("Start of Bundle: " + bundle);

		Set<Quad> expected = readNQuads(bundle.getExpected().getInputStream());
		Config config = SparqlifyUtils.readConfig(bundle.getMapping().getInputStream());
		DataSource ds = SparqlifyUtils.createDefaultDatabase("test", bundle.getSql().getInputStream());
		
		QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(ds, config, null, null);

		/**
		 *  Run the test queries on the test database.
		 *  TODO Factor each query test out into its own test!
		 */
		try {
			for(QueryBundle queryBundle : bundle.getQueryBundles()) {
				String queryString = StreamUtils.toString(queryBundle.getQuery().getInputStream()).trim();
				
				if(queryString.isEmpty()) {
					throw new RuntimeException("Empty querystring");
					//continue;
				}
				
				String queryResult = StreamUtils.toString(queryBundle.getQuery().getInputStream());
				
				Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
	
				QueryExecution qe = qef.createQueryExecution(query);
				if(query.isSelectType()) {
					ResultSet rs = qe.execSelect();
					logger.debug(ResultSetFormatter.asText(rs));
				}
				
			}
		} catch (Exception e) {
			SparqlifyUtils.shutdownH2(ds);
			throw e;
		}
				
		Set<Quad> actual = SparqlifyUtils.createDumpNQuads(qef);
		SparqlifyUtils.shutdownH2(ds);

		Set<Quad> alignedActual = CompareUtils.alignActualQuads(expected, actual);
		

		Set<Quad> excessive = Sets.difference(alignedActual, expected);
		Set<Quad> missing = Sets.difference(expected, alignedActual);

		System.out.println("Expected : " + expected);
		System.out.println("Actual   : " + alignedActual);

		System.out.println("Excessive: " + excessive);
		System.out.println("Missing  : " + missing);
		
		
		Assert.assertEquals(expected, alignedActual);

		System.out.println();
		System.out.println();
		System.out.println();
		//String StreamUtils.toStringSafe();
		//ConfigP
		
		
	}
}
