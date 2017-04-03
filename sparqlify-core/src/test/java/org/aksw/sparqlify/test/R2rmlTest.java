package org.aksw.sparqlify.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.test.TestBundle;
import org.aksw.sparqlify.core.test.TestBundleReader;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.Transform;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@RunWith(Parameterized.class)
public class R2rmlTest {

    /**
     *
     * TODO Make public in Jena ResultSetCompare
     */
    public static Transform<QuerySolution, Binding> qs2b = new Transform<QuerySolution, Binding> () {

        @Override
        public Binding apply(QuerySolution item)
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

        List<TestCase> testCases = TestBundlerConverter.collectTestCases(testBundles);
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





}
