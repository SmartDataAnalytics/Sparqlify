package org.aksw.sparqlify.core.sparql;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactoryDecorator;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;


public class QueryExecutionFactoryExWrapper
    extends QueryExecutionFactoryDecorator
    implements QueryExecutionFactoryEx
{
    // TODO The super class needs to declare the decorator as protected - however its private and in a different project
    QueryExecutionFactory hack;

    public QueryExecutionFactoryExWrapper(QueryExecutionFactory decoratee) {
        super(decoratee);
        this.hack = decoratee;
    }

    @Override
    public QueryExecution createQueryExecution(QueryEx queryEx) {
        if(queryEx.isExplain()) {
            throw new RuntimeException("EXPLAIN not supported - query: " + queryEx);
        }

        Query query = queryEx.getQuery();

        QueryExecution result = hack.createQueryExecution(query);
        return result;
    }

    public static QueryExecutionFactoryEx wrap(QueryExecutionFactory qef) {
        QueryExecutionFactoryEx result = new QueryExecutionFactoryExWrapper(qef);
        return result;
    }
}
