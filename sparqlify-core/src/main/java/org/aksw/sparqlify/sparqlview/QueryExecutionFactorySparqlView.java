package org.aksw.sparqlify.sparqlview;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionFactoryBackQuery;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;

public class QueryExecutionFactorySparqlView
	extends QueryExecutionFactoryBackQuery
{
	private static Logger logger = LoggerFactory.getLogger(QueryExecutionFactorySparqlView.class);
	
	private QueryExecutionFactory factory;
	private SparqlViewSystem system;
	private Dialect dialect;
	
	public QueryExecutionFactorySparqlView(QueryExecutionFactory factory, SparqlViewSystem system, Dialect dialect) {
		this.factory = factory;
		this.system = system;
		this.dialect = dialect;
	}
	
	@Override
	public String getId() {
		return factory.getId() + "-" + hashCode();
	}

	@Override
	public String getState() {
		return null;
	}

	@Override
	public QueryExecutionStreaming createQueryExecution(Query query) {
		Query rewritten = SparqlViewSystem.rewrite(query, system, dialect);
		//logger.trace("Rewritten query: " + rewritten);
		//System.out.println("Rewritten query: " + rewritten);
		QueryExecutionStreaming result = factory.createQueryExecution(rewritten);

		return result;
	}	
}