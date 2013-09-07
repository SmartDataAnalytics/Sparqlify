package org.aksw.sparqlify.core.test;

import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.Assert;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.sparql.core.Quad;

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

		Set<Quad> alignedActual = CompareUtils.alignActualQuads(expected, actual);
		

		Set<Quad> excessive = Sets.difference(alignedActual, expected);
		Set<Quad> missing = Sets.difference(expected, alignedActual);

		if(!excessive.isEmpty() || !missing.isEmpty()) {
		
			System.out.println("Expected : " + expected);
			System.out.println("Actual   : " + alignedActual);
	
			System.out.println("Excessive: " + excessive);
			System.out.println("Missing  : " + missing);
		}		
		
		Assert.assertEquals(expected, alignedActual);
	}	
}