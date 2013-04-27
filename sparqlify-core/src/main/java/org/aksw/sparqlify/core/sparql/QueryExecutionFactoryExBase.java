package org.aksw.sparqlify.core.sparql;

import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;

import com.hp.hpl.jena.query.Query;

public abstract class QueryExecutionFactoryExBase
	implements QueryExecutionFactoryEx
{

	@Override
	public String getId() {
		return null;
	}

	@Override
	public String getState() {
		return null;
	}

	@Override
	public QueryExecutionStreaming createQueryExecution(String queryString) {

		QueryEx qe = QueryFactoryEx.create(queryString);
		
		QueryExecutionStreaming result = createQueryExecution(qe);

		return result;
	}

	@Override
	public QueryExecutionStreaming createQueryExecution(Query query) {
		QueryEx qe = new QueryEx(query);

		QueryExecutionStreaming result = createQueryExecution(qe);
		return result;
	}

	//public abstract QueryExecutionStreaming createQueryExecution(QueryEx query);

	/*
	 * public QueryExecutionStreaming createQueryExecution(QueryEx query) { //
	 * TODO Auto-generated method stub return null; }
	 */
}
