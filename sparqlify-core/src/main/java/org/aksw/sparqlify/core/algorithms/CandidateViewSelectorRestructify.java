package org.aksw.sparqlify.core.algorithms;

import java.util.List;

import org.aksw.sparqlify.sparqlview.SparqlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpDisjunction;

public class CandidateViewSelectorRestructify
	extends CandidateViewSelectorBase<SparqlView, Void>
{
	private static final Logger logger = LoggerFactory.getLogger(CandidateViewSelectorRestructify.class);
	
	
	public CandidateViewSelectorRestructify() {
	}

	@Override
	public Op createOp(List<ViewInstanceJoin<SparqlView>> conjunctions) {
		
		
		OpDisjunction result = OpDisjunction.create();
		
		for(ViewInstanceJoin<SparqlView> item : conjunctions) {
			Op tmp = new OpViewInstanceJoin(item);
			result.add(tmp);
		}
		
		return result;
	}
	
}