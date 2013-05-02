package org.aksw.sparqlify.database;

import org.aksw.sparqlify.restriction.RestrictionManagerImpl;

public class FilterSplit {
	private RestrictionManagerImpl leftClauses;
	private RestrictionManagerImpl nonPushable;
	
	public FilterSplit(RestrictionManagerImpl leftClauses, RestrictionManagerImpl nonPushable) {
		this.leftClauses = leftClauses;
		this.nonPushable = nonPushable;
	}

	public RestrictionManagerImpl getLeftClauses() {
		return leftClauses;
	}

	public RestrictionManagerImpl getNonPushable() {
		return nonPushable;
	}
}