package org.aksw.sparqlify.core.test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.Transform;
import org.apache.jena.riot.RiotReader;
import org.apache.jena.riot.lang.LangNQuads;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFLib;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingUtils;



@RunWith(Parameterized.class)
public class R2rmlTest {

	/**
	 * 
	 * TODO Make public in Jena ResultSetCompare
	 */
    public static Transform<QuerySolution, Binding> qs2b = new Transform<QuerySolution, Binding> () {

        @Override
        public Binding convert(QuerySolution item)
        {
            return BindingUtils.asBinding(item) ;
        }
    } ;

    /**
     * 
     * TODO Make public in Jena ResultSetCompare
     */
    public static List<Binding> convert(ResultSet rs)
    {
        return Iter.iter(rs).map(qs2b).toList() ;
    }

	
	
	private static final Logger logger = LoggerFactory.getLogger(R2rmlTest.class);
	
	//private Comparator<Resource> resourceComparator = new ResourceComparator();	
	//private PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
	private String name;
	private Runnable task;
	
	
	public R2rmlTest(String name, Runnable task) {
		this.name = name;
		this.task = task;
	}
	
//	public TestCase getTask() {
//		return task;
//	}
	
	@Parameters(name = "Sparqlify R2RML Test {index}: {0}")
	public static Collection<Object[]> data()
			throws Exception
	{
		TestBundleReader testBundleReader = new TestBundleReader();
		List<TestBundle> testBundles = testBundleReader.getTestBundles();
		logger.debug(testBundles.size() + " test bundles detected.");
		
		List<TestCase> testCases = collectTestCases(testBundles);
		logger.debug(testCases.size() + " test cases derived.");
		
		for(int i = 0; i < testCases.size(); ++i) {
			TestCase testCase = testCases.get(i);
			logger.trace("Test Case #" + i + ": " + testCase);
		}
		
		Object data[][] = new Object[testCases.size()][2];
		
		
		for(int i = 0; i < testCases.size(); ++i) {
			TestCase testCase = testCases.get(i);
			data[i][0] = testCase.getName();
			data[i][1] = testCase;
		}

		Collection<Object[]> result = Arrays.asList(data); 
		
		return result;
	}


	@Test
	public void run()
			throws Exception
	{
		task.run();
		
		
//		QueryExecutionFactory qef = testBundle.getQueryExecutionFactory();
//		Query query = testBundle.getQuery();
//		
//		ResultSet rs = qef.createQueryExecution(query);
//		ResultSetCompare.equalsByValue();
		
		//runBundle(testBundle);
	}

	public static Set<Quad> readNQuads(InputStream in) {

		SinkQuadsToSet quadSink = new SinkQuadsToSet();
		StreamRDF streamRdf = StreamRDFLib.sinkQuads(quadSink);
		LangNQuads parser = RiotReader.createParserNQuads(in, streamRdf);
		parser.parse();

		Set<Quad> result = quadSink.getQuads();
		return result;
	}

		
	
	
	public static List<TestCase> collectTestCases(List<TestBundle> bundles) throws Exception {
		List<TestCase> result = new ArrayList<TestCase>();
		for(TestBundle bundle : bundles) {
			List<TestCase> tmp = collectTestCases(bundle);
			
			result.addAll(tmp);
		}
		
		return result;
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
	public static List<TestCase> collectTestCases(TestBundle bundle)
			throws Exception
	{
		//System.out.println("---------------------------------");
		logger.info("Collecting test cases from bundle: " + bundle.getName());

		String name = bundle.getName();
		DataSource ds = SparqlifyUtils.createDefaultDatabase(name, bundle.getSql().getInputStream());
	
		List<TestCase> result = new ArrayList<TestCase>();
		
		for(MappingBundle mappingBundle : bundle.getMappingBundles()) {
			collectTestCases(name, ds, mappingBundle, result);
		}
		
		return result;
	}

	
	public static final Query dumpQuery = QueryFactory.create("Select ?g ?s ?p ?o { Graph ?g { ?s ?p ?o } }", Syntax.syntaxSPARQL_11);

	public static void collectTestCases(String testName, DataSource ds, MappingBundle bundle, List<TestCase> result)
		throws Exception
	{
		Config config = SparqlifyUtils.readConfig(bundle.getMapping().getInputStream());
		QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(ds, config, null, null);

		
		String mappingName = bundle.getName();
		String baseName = testName + "/" + mappingName + "/";
		
		// If there is an expected result for the complete mapping, create a query for it
		if(bundle.getExpected() != null) {
			Set<Quad> expected = readNQuads(bundle.getExpected().getInputStream());
			//ResultSet resultSet = NQuadsToResultSet.createResultSet(expected);

			
			String name = baseName + "dump";
			
			Callable<Set<Quad>> task = new TaskDump(qef);
			TestCaseDump verify = new TestCaseDump(task, expected);			
			TestCase testCase = new TestCaseImpl(name, verify);
			
			result.add(testCase);
		}		

		for(QueryBundle queryBundle : bundle.getQueryBundles()) {
			
			//if(true) { continue; }
			
			String queryString = StreamUtils.toString(queryBundle.getQuery().getInputStream()).trim();
			
			Resource queryResultRes = queryBundle.getResult();
			
			// Note: If there is a query without expected result, then the query is tested for
			// whether it executed without raising an exception.
			ResultSet expected = null;
			if(queryResultRes.exists()) {
				String queryResult = StreamUtils.toString(queryResultRes.getInputStream());	
				//System.out.println(queryResult);
				expected = ResultSetFactory.fromXML(queryResult);
			}

			//Query query = new Query();
			Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);

			String name = baseName + queryBundle.getName();

			Callable<ResultSet> task = new TaskQuerySelect(qef, query);
			TestCaseQuerySelect verify = new TestCaseQuerySelect(task, expected);			
			TestCase testCase = new TestCaseImpl(name, verify);

			
			result.add(testCase);
		}
	}
	
	
//	public void createRunBundle(QueryExecutionFactory qef, QueryBundle queryBundle) {
//
//		/**
//		 *  Run the test queries on the test database.
//		 *  TODO Factor each query test out into its own test!
//		 */
//		try {
//			for(QueryBundle queryBundle : bundle.getQueryBundles()) {
//				String queryString = StreamUtils.toString(queryBundle.getQuery().getInputStream()).trim();
//				
//				if(queryString.isEmpty()) {
//					throw new RuntimeException("Empty querystring");
//					//continue;
//				}
//				
//				String queryResult = StreamUtils.toString(queryBundle.getQuery().getInputStream());
//				
//				Query query = QueryFactory.create(queryString, Syntax.syntaxSPARQL_11);
//	
//				QueryExecution qe = qef.createQueryExecution(query);
//				if(query.isSelectType()) {
//					ResultSet rs = qe.execSelect();
//					logger.debug(ResultSetFormatter.asText(rs));
//				}
//				
//			}
//		} catch (Exception e) {
//			SparqlifyUtils.shutdownH2(ds);
//			throw e;
//		}
//				
//		Set<Quad> actual = SparqlifyUtils.createDumpNQuads(qef);
//		SparqlifyUtils.shutdownH2(ds);
//
//		Set<Quad> alignedActual = CompareUtils.alignActualQuads(expected, actual);
//		
//
//		Set<Quad> excessive = Sets.difference(alignedActual, expected);
//		Set<Quad> missing = Sets.difference(expected, alignedActual);
//
//		System.out.println("Expected : " + expected);
//		System.out.println("Actual   : " + alignedActual);
//
//		System.out.println("Excessive: " + excessive);
//		System.out.println("Missing  : " + missing);
//		
//		
//		Assert.assertEquals(expected, alignedActual);
//
//		System.out.println();
//		System.out.println();
//		System.out.println();
//		//String StreamUtils.toStringSafe();
//		//ConfigP
//		
//		
//	}
}
