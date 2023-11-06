package org.aksw.sparqlify.core.test;

import java.util.concurrent.Callable;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;

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
        qe.close();

        return result;
    }
}
