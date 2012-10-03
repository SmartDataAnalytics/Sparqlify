package org.aksw.sparqlify.core.interfaces;

import java.util.Collection;

import org.aksw.sparqlify.core.RdfView;
import org.aksw.sparqlify.core.domain.ViewDefinition;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;


public interface ViewSystem {
	void addView(ViewDefinition viewDefinition);
	
	Op getApplicableViews(Query query);
	
	// List the views registered with the system
	Collection<ViewDefinition> getViews();
}
