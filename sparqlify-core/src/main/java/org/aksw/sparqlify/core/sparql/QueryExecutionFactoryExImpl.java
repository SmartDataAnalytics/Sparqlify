package org.aksw.sparqlify.core.sparql;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;

import com.hp.hpl.jena.query.Query;


public class QueryExecutionFactoryExImpl
	extends QueryExecutionFactoryExBase
{
	private QueryExecutionFactory qefDefault;
	private QueryExecutionFactory qefExplain;
	
	
	public QueryExecutionFactoryExImpl(QueryExecutionFactory qefDefault, QueryExecutionFactory qefExplain) {
		this.qefDefault = qefDefault;
		this.qefExplain = qefExplain;
	}
	
	@Override
	public QueryExecutionStreaming createQueryExecution(QueryEx queryEx) {
		QueryExecutionStreaming result;
		
		Query query = queryEx.getQuery();
		
		if(queryEx.isExplain()) {
			result = qefExplain.createQueryExecution(query);
		} else {
			result = qefDefault.createQueryExecution(query);
		}
		
		return result;
	}

}


