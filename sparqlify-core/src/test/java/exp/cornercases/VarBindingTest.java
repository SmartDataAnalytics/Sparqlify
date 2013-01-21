package exp.cornercases;

import org.aksw.sparqlify.core.algorithms.VarBinding;
import org.junit.Test;

import com.hp.hpl.jena.sparql.core.Var;

public class VarBindingTest {

	/**
	 * A test whether computing the binding closure works as expected
	 * 
	 */
	@Test
	public void test() {

		Var s = Var.alloc("s");
		Var p = Var.alloc("p");
		Var o = Var.alloc("o");
		Var x = Var.alloc("x");
		Var y = Var.alloc("y");
		Var z = Var.alloc("z");

		VarBinding binding = new VarBinding();
		binding.put(s, x);
		binding.put(s, y);
		binding.put(p, x);
		binding.put(o, y);
		binding.put(o, z);

		System.out.println(binding);
		
		
		System.out.println("Query to Query:" + binding.getQueryVarToViewVars());
		System.out.println("View to Query:" + binding.getViewVarToQueryVars());
		
		//VarBinding closure = binding.computeClosure();

		VarBinding expected = new VarBinding();
		expected.put(s, x);
		expected.put(s, y);
		expected.put(s, y);
		expected.put(p, x);
		expected.put(p, y);
		expected.put(p, z);
		expected.put(o, x);
		expected.put(o, y);
		expected.put(o, z);
		
		System.out.println(binding);
		//System.out.println(closure);

		// TODO my bi-map does not implement equals yet... grrr
		//Assert.assertEquals(expected, closure);
	}
}
