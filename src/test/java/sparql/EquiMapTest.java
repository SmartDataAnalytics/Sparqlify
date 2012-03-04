package sparql;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import junit.framework.Assert;


public class EquiMapTest
{
	//@Test
	public void hashTest() {
		Set<Set<String>> x = new HashSet<Set<String>>();
		
		Set<String> set = new HashSet<String>();
		set.add("Hello");
		
		x.add(set);
		System.out.println(x.contains(set));
		
		System.out.println(set.hashCode());
		set.add("World");
		System.out.println(set.hashCode());
		
		
		System.out.println(x.contains(set));
	}
	                       
	
	//@Test
	public void test1() {
		EquiMap<String, Integer> a = new EquiMap<String, Integer>();		
		a.put("a", 1);
		a.put("b", 2);
		
		EquiMap<String, Integer> b = new EquiMap<String, Integer>();
		b.put("a", 1);
		b.put("b", 2);
		
		Assert.assertTrue(a.isCompatible(b));		
	}
	
	@Test
	public void test2() {
		EquiMap<String, Integer> a = new EquiMap<String, Integer>();		
		a.makeEqual("a", "b");
		a.put("a", 1);
		a.put("c", 2);
		
		EquiMap<String, Integer> b = new EquiMap<String, Integer>();
		b.makeEqual("b", "c");
		
		Assert.assertFalse(a.isCompatible(b));		
	}
	
	
}
