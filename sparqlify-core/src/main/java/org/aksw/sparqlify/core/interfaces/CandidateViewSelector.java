package org.aksw.sparqlify.core.interfaces;

import java.util.Collection;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;


public interface CandidateViewSelector<T extends IViewDef> {
	void addView(T viewDefinition);
	
	Op getApplicableViews(Query query);
	
	// List the views registered with the system
	Collection<T> getViews();
}
