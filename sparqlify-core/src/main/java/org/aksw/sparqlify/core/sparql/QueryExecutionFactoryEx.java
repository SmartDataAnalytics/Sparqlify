package org.aksw.sparqlify.core.sparql;

import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;

public interface QueryExecutionFactoryEx
	extends QueryExecutionFactory
{
	QueryExecutionStreaming createQueryExecution(QueryEx query);	
}
