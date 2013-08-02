package org.aksw.sparqlify.core.sparql;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

import com.hp.hpl.jena.query.QueryExecution;

public interface QueryExecutionFactoryEx
	extends QueryExecutionFactory
{
	QueryExecution createQueryExecution(QueryEx query);	
}
