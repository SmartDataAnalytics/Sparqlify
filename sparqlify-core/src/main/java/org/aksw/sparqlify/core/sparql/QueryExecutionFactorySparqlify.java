package org.aksw.sparqlify.core.sparql;

import java.sql.Connection;

import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;

import com.hp.hpl.jena.query.Query;

public class QueryExecutionFactorySparqlify
	extends QueryExecutionFactoryBackQuery
{
	private SparqlSqlStringRewriter system;
	private Connection conn;

	public QueryExecutionFactorySparqlify(SparqlSqlStringRewriter system, Connection conn)
	{
		this.system = system;
		this.conn = conn;
	}
	
	@Override
	public QueryExecutionStreaming createQueryExecution(Query query) {
		//System.out.println(query);
		
		return new QueryExecutionSparqlify(system, conn, false, query, this);
	}

	@Override
	public String getId() {
		// TODO Implement
		throw new RuntimeException("Implement properly");
	}

	@Override
	public String getState() {
		// TODO Implement
		throw new RuntimeException("Implement properly");
	}
	
}
