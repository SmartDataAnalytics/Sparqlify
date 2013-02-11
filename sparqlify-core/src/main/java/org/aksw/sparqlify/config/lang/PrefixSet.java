package org.aksw.sparqlify.config.lang;

import java.util.Collection;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;

import org.aksw.commons.util.strings.StringUtils;

/**
 * TODO Switch to a trie data structure
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class PrefixSet
{
	private NavigableSet<String> prefixes;
	
	public PrefixSet() {
		this.prefixes = new TreeSet<String>();
	}
	
	public PrefixSet(String ... strings) {
		this.prefixes = new TreeSet<String>();
		
		for(String string : strings) {
			prefixes.add(string);
		}
	}
	
	public PrefixSet(NavigableSet<String> prefixes)
	{
		this.prefixes = prefixes;
	}
	
	public PrefixSet(PrefixSet uriPrefixes) {
		this(new TreeSet<String>(uriPrefixes.prefixes));
	}

	public void addAll(Collection<String> prefixes) {
		this.prefixes.addAll(prefixes);
	}
	
	public void addAll(PrefixSet other) {
		addAll(other.getSet());
	}
	
	public NavigableSet<String> getSet() {
		return prefixes;
	}
	

	public boolean isEmpty() {
		return prefixes.isEmpty(); 
	}

	@Override
	public String toString() {
		return "PrefixSet [prefixes=" + prefixes + "]";
	}
	
	/**
	 * Tests whether the set constains a prefix for the given argument 
	 * 
	 * @param value
	 * @return
	 */
	public boolean containsPrefixOf(String value) {
		return StringUtils.longestPrefixLookup(value, prefixes) != null;
	}
	
	/**
	 * Tests whether the argument is a prefix of one of the items
	 * 
	 * @param value
	 * @return
	 */
	public boolean isPrefixForItem(String prefix) {
        return getShortestMatch(prefix) != null;
	}
	
	/*
	public Set<String> getShortestMatches(String prefix) {
		
	}*/
	
	public String getShortestMatch(String prefix) {
        return StringUtils.shortestMatchLookup(prefix, true, prefixes);
	}
	
	public static void main(String[] args) {
		PrefixSet x = new PrefixSet();
		
		x.getSet().add("aaa");
		x.getSet().add("bbb");
		x.getSet().add("b");
		x.getSet().add("ccc");
		x.getSet().add("cccd");
		x.getSet().add("cccde");

		
		//TODO Creata JUNIT Test case
		//Assert.assertTrue(x.containsPrefixOf("cccd"));
		//Assert.assertTrue(x.isPrefixForItem("bb"));
	}

	public void removeAll(Collection<String> ps) {
		prefixes.removeAll(ps);
	}

	public void add(String s) {
		prefixes.add(s);
	}

	public Set<String> getPrefixesOf(String s) {
		return StringUtils.getAllPrefixes(s, true, prefixes);
	}

	public Set<String> getPrefixesOf(String s, boolean inclusive) {
		return StringUtils.getAllPrefixes(s, inclusive, prefixes);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((prefixes == null) ? 0 : prefixes.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrefixSet other = (PrefixSet) obj;
		if (prefixes == null) {
			if (other.prefixes != null)
				return false;
		} else if (!prefixes.equals(other.prefixes))
			return false;
		return true;
	}
}
