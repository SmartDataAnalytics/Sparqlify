package org.aksw.sparqlify.core.interfaces;

import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;


public interface CandidateViewSelector {
	void addView(ViewDefinition viewDefinition);
	
	Op getApplicableViews(Query query);
	
	// List the views registered with the system
	Collection<ViewDefinition> getViews();
}
