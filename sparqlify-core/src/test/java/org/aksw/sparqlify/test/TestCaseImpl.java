package org.aksw.sparqlify.test;

/**
 * @author raven
 *
 */
public class TestCaseImpl
	implements TestCase
{
	private String name;
	private Runnable runnable;

	public TestCaseImpl(String name, Runnable runnable) {
		this.name = name;
		this.runnable = runnable;
	}

	public String getName() {
		return name;
	}

	public Runnable getRunnable() {
		return runnable;
	}

	@Override
	public String toString() {
		return "TestCase [name=" + name + ", runnable=" + runnable + "]";
	}

	@Override
	public void run() {		
		runnable.run();
	}
}