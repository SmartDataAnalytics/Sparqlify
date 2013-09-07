package org.aksw.sparqlify.core.test;

import java.util.concurrent.Callable;

import org.junit.Assert;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare;
import com.hp.hpl.jena.sparql.resultset.ResultSetRewindable;

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

		boolean isEqual = true;
		ResultSetRewindable act = null;
		ResultSetRewindable exp = null;
		if (expected != null) {
			// TODO File in JIRA issue: This impl is bugged (look at the code,
			// rs1 should be rs1a)
	        act = ResultSetFactory.makeRewindable(rs);
	        exp = ResultSetFactory.makeRewindable(expected) ;

			
			isEqual = ResultSetCompare.equalsByTerm(act, exp);
			
			act.reset();
			exp.reset();
		}

		if(!isEqual) {
			String eStr = ResultSetFormatter.asText(exp);
			System.out.println("Expected");
			System.out.println("------");
			System.out.println(eStr);
			
			String aStr = ResultSetFormatter.asText(act);
			System.out.println("Actual");
			System.out.println("------");
			System.out.println(aStr);
		}
		
		Assert.assertTrue(isEqual);
	}
}