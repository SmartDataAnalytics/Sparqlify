package org.aksw.sparqlify.restriction;

import java.util.Map;

public class PrefixSet2 {
	private String prefix;
	private boolean isPositive;
	private boolean isConstant; // Whether the prefix is actually the constant itself (i.e. a single element set)
	
	/*
	 * if prefix is  
	 * 
	 */
	private Map<String, PrefixSet2> suffixToExceptions; 
	
	//PrefixMap
	
	public boolean isPrefixOf(String value) {
		boolean result = value.startsWith(prefix);
		return result;
	}
	
	
	/**
	 * How to deal with exceptions:
	 * - This is not owl, except for owl:Class
	 * - This is rdf, except for rdf:type
	 * 
	 * 
	 * case: positive - positive
	 *     Create the intersection:
	 *        {owl} and {rdf} -> {}
	 *        {owl} and {owla} -> {owla} (owla is more specific)  
	 * 
	 * case: positive - negative
	 *        {owl} and not{rdf} -> {owl} (positive prevails)
	 *        {owl} and not{owla} -> {owl[except owla]} (exception)}
	 *        not{owla} and {owl} -> same as above 
	 *        not{owl} and {owla} -> {} (owl namespace was excluded)
	 * 
	 * case: negative - negative
	 *     Create the union:
	 *         not{owl} and not{rdf} -> not{rdf, owl}
	 *         not(owl} and not{owla} -> not{owl} (owla subsumed by the shorter prefix)
	 * 
	 */
	public PrefixSet2 intersects(PrefixSet2 that) {
		String shorter;
		String longer;
		
		// Determine the shorter and loger prefix
		if(prefix.length() < that.prefix.length()) {
			shorter = this.prefix;
			longer = that.prefix;
		} else {
			shorter = that.prefix;
			longer = this.prefix;
		}
		
		if(longer.startsWith(shorter)) {
			
		}
		
		
		
		return that;
		
	}


	public PrefixSet2 union(PrefixSet2 that) {
		return that;
		
	}

	
	public PrefixSet2 negate() {
		return null;
		
	}


}
