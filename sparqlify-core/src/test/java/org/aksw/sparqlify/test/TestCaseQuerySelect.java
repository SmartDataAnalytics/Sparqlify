package org.aksw.sparqlify.test;

import java.util.concurrent.Callable;

import com.hp.hpl.jena.query.ResultSet;


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
		ResultSet rs;
		try {
			rs = task.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}