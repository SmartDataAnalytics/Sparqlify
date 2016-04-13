package org.aksw.sparqlify.core;

import java.util.Collection;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;


public interface RdfViewSystem {
	void addView(RdfView view);
	
	Op getApplicableViews(Query query);
	
	// List the views registered with the system
	Collection<RdfView> getViews();
}
