package org.aksw.sparqlify.restriction.experiment;

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
	
	
	
	@Override
	public Restriction intersect(Restriction other) {
		RestrictionPrefix o = (RestrictionPrefix)other;

//		o.getPositive().getPrefixesOf(s, inclusive)

		
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Restriction union(Restriction other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Restriction negate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isUnsatisfiable() {
		// TODO Auto-generated method stub
		return false;
	}

}
