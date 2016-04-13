package org.aksw.sparqlify.core.interfaces;

import java.util.Collection;

import org.aksw.sparqlify.core.domain.input.ViewDefinition;

import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.Op;


public interface CandidateViewSelectorOld
	//extends CandidateViewSelectorGeneric<ViewDefinition>
{
	void addView(ViewDefinition viewDefinition);
	
	Op getApplicableViews(Query query);
	
	// List the views registered with the system
	Collection<ViewDefinition> getViews();
}
