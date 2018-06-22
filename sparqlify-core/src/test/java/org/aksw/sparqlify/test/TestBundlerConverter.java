package org.aksw.sparqlify.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.aksw.commons.util.StreamUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.test.MappingBundle;
import org.aksw.sparqlify.core.test.QueryBundle;
import org.aksw.sparqlify.core.test.TaskDump;
import org.aksw.sparqlify.core.test.TestBundle;
import org.aksw.sparqlify.core.test.TestHelper;
import org.aksw.sparqlify.util.NQuadUtils;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.antlr.runtime.RecognitionException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.sparql.core.Quad;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;


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

        List<TestCase> result = new ArrayList<TestCase>();

        for(MappingBundle mappingBundle : bundle.getMappingBundles()) {
            collectTestCases(bundle, mappingBundle, result);
        }

        return result;
    }


    public static final Query dumpQuery = QueryFactory.create("Select ?g ?s ?p ?o { Graph ?g { ?s ?p ?o } }", Syntax.syntaxSPARQL_11);

    public static QueryExecutionFactory createQef(String baseName, TestBundle testBundle, MappingBundle bundle) throws SQLException, IOException, RecognitionException {
        DataSource ds = SparqlifyUtils.createDefaultDatabase(baseName, testBundle.getSql().getInputStream());

        Config config = SparqlifyUtils.readConfig(bundle.getMapping().getInputStream());
        QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(ds, config, null, null);

        return qef;
    }

    public static void collectTestCases(TestBundle testBundle, MappingBundle bundle, List<TestCase> result)
        throws Exception
    {
        String testName = testBundle.getName();


        String mappingName = bundle.getName();
        String baseName = testName + "/" + mappingName + "/";

        // If there is an expected result for the complete mapping, create a query for it
        if(bundle.getExpected() != null) {
            //ResultSet resultSet = NQuadsToResultSet.createResultSet(expected);


            String name = baseName + "dump";

            TestCase testCase = new TestCaseImpl(name, () -> {
            	boolean isEqual;
            	try {
                    Set<Quad> expected = NQuadUtils.readNQuads(bundle.getExpected().getInputStream());

                    try(QueryExecutionFactory qef = createQef(name, testBundle, bundle)) {
                        //Callable<Set<Quad>> task = new TaskDump(qef);
                        //TestCaseDump verify = new TestCaseDump(task, expected);
                        Set<Quad> actual = QueryExecutionUtils.createDumpNQuads(qef);
                        isEqual = TestHelper.isEqual(actual, expected);
                    }
                } catch(Exception e){
                    throw new RuntimeException(e);
                }
                Assert.assertTrue(isEqual);
            });

            result.add(testCase);
        }

        for(QueryBundle queryBundle : bundle.getQueryBundles()) {
            String name = baseName + queryBundle.getName();

            Runnable task = () -> {
                try {
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


                    try(QueryExecutionFactory qef = createQef(name, testBundle, bundle)) {
                        QueryExecution qe = qef.createQueryExecution(query);
                        ResultSet actual = qe.execSelect();

                    //Callable<ResultSet> task = new TaskQuerySelect(qef, query);

                        boolean isEqual = TestHelper.isEqual(actual, expected);
                        Assert.assertTrue(isEqual);
                    }

                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
                //TestCaseQuerySelect verify = new TestCaseQuerySelect(task, expected);
            };

            TestCase testCase = new TestCaseImpl(name, task);


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