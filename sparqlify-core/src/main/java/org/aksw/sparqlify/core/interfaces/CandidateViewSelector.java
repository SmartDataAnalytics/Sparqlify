package org.aksw.sparqlify.core.interfaces;

import java.util.Collection;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;


public interface CandidateViewSelector<T extends IViewDef> {
	void addView(T viewDefinition);
	
	Op getApplicableViews(Query query);
	
	// List the views registered with the system
	Collection<T> getViews();
}
