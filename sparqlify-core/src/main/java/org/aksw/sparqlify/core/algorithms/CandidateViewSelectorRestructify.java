package org.aksw.sparqlify.core.algorithms;

import java.util.List;

import org.aksw.sparqlify.core.OpQuadPattern2;
import org.aksw.sparqlify.sparqlview.OpSparqlViewPattern;
import org.aksw.sparqlify.sparqlview.SparqlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpDisjunction;

public class CandidateViewSelectorRestructify
	extends CandidateViewSelectorBase<SparqlView, Void>
{
	private static final Logger logger = LoggerFactory.getLogger(CandidateViewSelectorRestructify.class);
	
	
	public CandidateViewSelectorRestructify() {
	}

	@Override
	public Op createOp(OpQuadPattern2 qpQuadPattern, List<RecursionResult<SparqlView, Void>> conjunctions) {
		
		//ViewInstanceJoin<SparqlView> conjunctions = item.get
		
		OpDisjunction result = OpDisjunction.create();
		
		for(RecursionResult<SparqlView, Void> entry : conjunctions) {
			ViewInstanceJoin<SparqlView> item = entry.getViewInstances(); 
			Op tmp = new OpSparqlViewPattern(item);
			result.add(tmp);
		}
		
		return result;
	}
	
}