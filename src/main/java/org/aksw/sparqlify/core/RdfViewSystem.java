package org.aksw.sparqlify.core;

import java.util.Collection;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;


public interface RdfViewSystem {
	void addView(RdfView view);
	
	Op getApplicableViews(Query query);
	
	// List the views registered with the system
	Collection<RdfView> getViews();
}
