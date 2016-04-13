package org.aksw.sparqlify.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.test.MappingBundle;
import org.aksw.sparqlify.core.test.QueryBundle;
import org.aksw.sparqlify.core.test.TaskDump;
import org.aksw.sparqlify.core.test.TaskQuerySelect;
import org.aksw.sparqlify.core.test.TestBundle;
import org.aksw.sparqlify.util.NQuadUtils;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Quad;


public class TestBundlerConverter {
	private static final Logger logger = LoggerFactory.getLogger(TestBundlerConverter.class);
	
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
			Set<Quad> expected = NQuadUtils.readNQuads(bundle.getExpected().getInputStream());
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