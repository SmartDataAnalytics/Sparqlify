package org.aksw.sparqlify.core.test;

import java.util.concurrent.Callable;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;

public class TaskQuerySelect
	implements Callable<ResultSet>
{
	private QueryExecutionFactory qef;
	private Query query;

	public TaskQuerySelect(QueryExecutionFactory qef, Query query)
	{
		this.qef = qef;
		this.query = query;
	}

	@Override
	public ResultSet call() throws Exception {
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet result = qe.execSelect();
		
		return result;
	}
}