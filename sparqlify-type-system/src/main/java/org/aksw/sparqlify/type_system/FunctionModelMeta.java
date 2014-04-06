package org.aksw.sparqlify.type_system;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Object for storing meta information about function symbols
 * 
 * TODO Maybe this should be approached by tagging functions - something like function.getTagMap().contains("isa", "comparator"); 
 * 
 * @author raven
 *
 */
public class FunctionModelMeta {
	private Map<String, String> inverses = new HashMap<String, String>();
	private Set<String> comparators = new HashSet<String>();
	
	private Set<String> logicalAnds = new HashSet<String>();
	private Set<String> logicalOrs = new HashSet<String>();
	private Set<String> logicalNots = new HashSet<String>();

	
	public Set<String> getLogicalAnds() {
		return this.logicalAnds;
	}

	public Set<String> getLogicalOrs() {
		return this.logicalOrs;
	}
	
	public Set<String> getLogicalNots() {
		return this.logicalNots;
	}

	
	public Map<String, String> getInverses() {
		return inverses;
	}
	public Set<String> getComparators() {
		return comparators;
	}
}