package org.aksw.sparqlify.core.test;

import java.util.Set;
import java.util.concurrent.Callable;

import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.apache.jena.sparql.core.Quad;

public class TaskDump
    implements Callable<Set<Quad>>
{
    private QueryExecutionFactory qef;

    public TaskDump(QueryExecutionFactory qef)
    {
        this.qef = qef;
    }

    @Override
    public Set<Quad> call() throws Exception {
        Set<Quad> result = QueryExecutionUtils.createDumpNQuads(qef);
        return result;
    }

}