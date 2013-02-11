package exp.cornercases;

import org.aksw.sparqlify.restriction.Prefix;
import org.aksw.sparqlify.restriction.experiment.PrefixSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class PrefixSetTest {

	/**
	 * In this test, adding exception ex2 should throw an error,
	 * because it is already excluded by ex1.
	 * 
	 */
	@Test(expected=Throwable.class)
	@Ignore
	public void test1() {
		PrefixSet set = new PrefixSet(new Prefix("http://"));
		Prefix ex1 = new Prefix("http://ex.org/foo");
		Prefix ex2 = new Prefix("http://ex.org/foo/bar");
		set.addException(ex1);
		set.addException(ex2);
	}

	@Test
	@Ignore
	public void test2() {
		PrefixSet set = new PrefixSet(new Prefix("http://"));
		Prefix ex1 = new Prefix("http://ex.org/foo");
		Prefix ex2 = new Prefix("http://ex.org/foo/bar");
	
		PrefixSet sss = set.addException(ex1);
		sss.addException(ex2);

		
		boolean a1 = set.contains(new Prefix("http://ex.org"));
		Assert.assertTrue(a1);

		boolean b1 = set.contains(new Prefix("http://ex.org/foo"));
		boolean b2 = set.contains(new Prefix("http://ex.org/foo/bar"));
		
		Assert.assertFalse(b1);
		Assert.assertTrue(b2);
	}

/*	
	@Test
	public void test() {
		PrefixSet2 set = new PrefixSet2(new Prefix("http://"));
		Prefix ex1 = new Prefix("http://ex.org/foo");
		Prefix ex2 = new Prefix("http://ex.org/foo/bar");
		set.addException(ex1);
		set.addException(ex2);
		
		
		boolean a1 = set.contains(new Prefix("http://ex.org"));
		Assert.assertTrue(a1);

		boolean b1 = set.contains(new Prefix("http://ex.org/foo"));
		boolean b2 = set.contains(new Prefix("http://ex.org/foo/bar"));
		
		Assert.assertFalse(b1);
		Assert.assertTrue(b2);
	}
*/
}
