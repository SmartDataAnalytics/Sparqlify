package org.aksw.sparqlify.core.algorithms;

import java.util.Collection;

import org.aksw.sparqlify.core.domain.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.ViewSystem;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;

public class ViewSystemImpl
	implements ViewSystem
{

	@Override
	public void addView(ViewDefinition viewDefinition) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Op getApplicableViews(Query query) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ViewDefinition> getViews() {
		// TODO Auto-generated method stub
		return null;
	}

}
