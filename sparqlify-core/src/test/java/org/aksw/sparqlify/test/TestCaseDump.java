package org.aksw.sparqlify.test;

import java.util.Set;
import java.util.concurrent.Callable;

import org.aksw.sparqlify.core.test.TestHelper;
import org.junit.Assert;

import org.apache.jena.sparql.core.Quad;

// TODO: We could distinguish between a task (a callable) and a testcase, that validates the output of the task
public class TestCaseDump
	implements Runnable
{
	private Callable<Set<Quad>> task;
	private Set<Quad> expected;
	
	public TestCaseDump(Callable<Set<Quad>> task, Set<Quad> expected)
	{ 
		this.task = task;
		this.expected = expected;
	}

	@Override
	public void run() {
		Set<Quad> actual;
		try {
			actual = task.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		//SparqlifyUtils.shutdownH2(ds);

		boolean isEqual = TestHelper.isEqual(actual, expected);		
		Assert.assertTrue(isEqual);
	}	
}