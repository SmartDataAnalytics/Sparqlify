package org.aksw.sparqlify.test;

import java.util.concurrent.Callable;

import org.aksw.sparqlify.core.test.TestHelper;
import org.apache.jena.query.ResultSet;
import org.junit.Assert;


public class TestCaseQuerySelect
	implements Runnable
{
	private Callable<ResultSet> task;
	private ResultSet expected;
	
	public TestCaseQuerySelect(Callable<ResultSet> task, ResultSet expected) {		
		super();
		this.task = task;
		this.expected = expected;
	}

	public Callable<ResultSet> getTask() {
		return task;
	}

	public ResultSet getExpected() {
		return expected;
	}

	@Override
	public String toString() {
		return "TestCaseQuerySelect [task=" + task + ", expected=" + expected
				+ "]";
	}

	@Override
	public void run() {
		ResultSet actual;
		try {
			actual = task.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		boolean isEqual = TestHelper.isEqual(actual, expected);		
		Assert.assertTrue(isEqual);
	}
}