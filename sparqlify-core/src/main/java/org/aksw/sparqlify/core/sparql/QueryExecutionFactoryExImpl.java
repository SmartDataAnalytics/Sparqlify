package org.aksw.sparqlify.core.sparql;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;


public class QueryExecutionFactoryExImpl
    extends QueryExecutionFactoryExBase
{
    private QueryExecutionFactory qefDefault;
    private QueryExecutionFactory qefExplain;


    public QueryExecutionFactoryExImpl(QueryExecutionFactory qefDefault, QueryExecutionFactory qefExplain) {
        this.qefDefault = qefDefault;
        this.qefExplain = qefExplain;
    }

    public QueryExecutionFactory getDefaultQef() {
        return qefDefault;
    }

    public QueryExecutionFactory getExplainQef() {
        return qefExplain;
    }


    @Override
    public QueryExecution createQueryExecution(QueryEx queryEx) {
        QueryExecution result;

        Query query = queryEx.getQuery();

        if(queryEx.isExplain()) {
            result = qefExplain.createQueryExecution(query);
        } else {
            result = qefDefault.createQueryExecution(query);
        }

        return result;
    }

}


