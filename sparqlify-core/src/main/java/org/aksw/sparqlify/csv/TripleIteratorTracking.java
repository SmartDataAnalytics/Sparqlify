package org.aksw.sparqlify.csv;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.aksw.commons.collections.SinglePrefetchIterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;

public class TripleIteratorTracking
	extends SinglePrefetchIterator<Triple>
{
	private Iterator<Triple> it;

	private int totalTripleCount = 0;
	private int tripleGenCount = 0;
    private Map<Var, Integer> varCountMap = new HashMap<Var, Integer>();

	
	public TripleIteratorTracking(Iterator<Triple> it) {
		this.it = it;
	}

	@Override
	protected Triple prefetch()
		throws Exception 
	{
		while(it.hasNext()) {
			
	    	++totalTripleCount;
	    	Triple t = it.next();
	    	
	    	CsvMapperCliMain.countVariable(t.getSubject(), varCountMap);
	    	CsvMapperCliMain.countVariable(t.getPredicate(), varCountMap);
	    	CsvMapperCliMain.countVariable(t.getObject(), varCountMap);
	
	    	//logger.trace("Triple: " + t);
	    	
	    	if(CsvMapperCliMain.containsNullOrVar(t)) {
	//        		logger.warn("Omitting null statement, triple was: " + t);
	    		continue;
	    	}
	    	++tripleGenCount;
	    	
	    	return t;
		}
		
		return finish();
	}


	public TripleIteratorState getState() {
		TripleIteratorState result = new TripleIteratorState(totalTripleCount, tripleGenCount, varCountMap);
		return result;
	}
	
}