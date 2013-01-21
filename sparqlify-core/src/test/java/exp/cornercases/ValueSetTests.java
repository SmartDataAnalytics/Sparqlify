package exp.cornercases;

import org.aksw.sparqlify.restriction.ValueSet;
import org.junit.Assert;
import org.junit.Test;

public class ValueSetTests {

	@Test
	public void simpleTests() {
		
		ValueSet<Integer> pa = ValueSet.create(true, 1, 2, 3);
		ValueSet<Integer> na = ValueSet.create(false, 1, 2, 3);

		ValueSet<Integer> pb = ValueSet.create(true, 3, 4, 5);
		ValueSet<Integer> nb = ValueSet.create(false, 3, 4, 5);
		
		
		/*
		 * Intersection
		 */
		{
			ValueSet<Integer> test = pa.intersect(pb);	
			Assert.assertEquals(ValueSet.create(true, 3), test);
		}

		{
			ValueSet<Integer> test = pa.intersect(nb);
			Assert.assertEquals(ValueSet.create(true, 1, 2), test);
		}

		{
			ValueSet<Integer> test = nb.intersect(pa);
			Assert.assertEquals(ValueSet.create(true, 1, 2), test);
		}

		{
			ValueSet<Integer> test = na.intersect(nb);
			Assert.assertEquals(ValueSet.create(false, 1, 2, 3, 4, 5), test);
		}

		
		/*
		 * Union
		 */
		{
			ValueSet<Integer> test = pa.union(pb);	
			Assert.assertEquals(ValueSet.create(true, 1, 2, 3, 4, 5), test);
		}

		{
			ValueSet<Integer> test = pa.union(nb);	
			Assert.assertEquals(ValueSet.create(false, 4, 5), test);
		}

		{
			ValueSet<Integer> test = nb.union(pa);	
			Assert.assertEquals(ValueSet.create(false, 4, 5), test);
		}

		{
			ValueSet<Integer> test = na.union(nb);	
			Assert.assertEquals(ValueSet.create(true, 3), test);
		}
	}
}
