package org.aksw.sparqlify.restriction;

import org.aksw.sparqlify.config.lang.PrefixSet;

public class RestrictionPrefix
	implements Restriction
{
	private PrefixSet prefixes;
	boolean isInverted = false; // true if the prefixes are blacklisted
	//private PrefixSet negative;
	
	//boolean isUnsatisfiable;
	
	public RestrictionPrefix() {
		//this.positive = null;
		//this.negative = null;
		//this.isUnsatisfiable = isUnsatisfiable;
	}
	
	public RestrictionPrefix(PrefixSet prefixes, boolean isInverted) {
		this.prefixes = prefixes;
		this.isInverted = isInverted;
	}

	public PrefixSet getPrefixes() {
		return prefixes;
	}
	
	public boolean isInverted() {
		return isInverted;
	}
	/*
	public PrefixSet getNegative() {
		return negative;
	}
	*/
	
	
	
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
	 *        not{owl} and {owla} -> {} (owl namespace was excluded)
	 *        not{owla} and {owl} -> 
	 * 
	 * case: negative - negative
	 *     Create the union:
	 *         not{owl} and not{rdf} -> not{rdf, owl}
	 *         not(owl} and not{owla} -> not{owl} (owla subsumed by the shorter prefix)
	 * 
	 */
	@Override
	public Restriction and(Restriction other) {
		RestrictionPrefix o = (RestrictionPrefix)other;

//		o.getPositive().getPrefixesOf(s, inclusive)

		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Restriction or(Restriction other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Restriction not(Restriction other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUnsatisfiable() {
		// TODO Auto-generated method stub
		return false;
	}

}
