package org.aksw.sparqlify.core.cast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Object for storing meta information about function symbols
 * 
 * 
 * @author raven
 *
 */
public class FunctionModelMeta {
	private Map<String, String> inverses = new HashMap<String, String>();
	private Set<String> comparators = new HashSet<String>();
	
	
	
	public Map<String, String> getInverses() {
		return inverses;
	}
	public Set<String> getComparators() {
		return comparators;
	}
}