package org.aksw.sparqlify.core.sparql;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.query.QueryExecution;

public interface QueryExecutionFactoryEx
    extends QueryExecutionFactory
{
    QueryExecution createQueryExecution(QueryEx query);
}
