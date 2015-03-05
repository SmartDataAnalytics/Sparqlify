package org.aksw.sparqlify.core.sparql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;

public abstract class QueryExecutionFactoryExBase
    implements QueryExecutionFactoryEx
{

    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getState() {
        return null;
    }

    @Override
    public QueryExecution createQueryExecution(String queryString) {

        QueryEx qe = QueryFactoryEx.create(queryString);

        QueryExecution result = createQueryExecution(qe);

        return result;
    }

    @Override
    public QueryExecution createQueryExecution(Query query) {
        QueryEx qe = new QueryEx(query);

        QueryExecution result = createQueryExecution(qe);
        return result;
    }


    @Override
    public <T> T unwrap(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T result = getClass().isAssignableFrom(clazz) ? (T)this : null;
        return result;
    }

    @Override
    public void close() {

    }
    //public abstract QueryExecutionStreaming createQueryExecution(QueryEx query);

    /*
     * public QueryExecutionStreaming createQueryExecution(QueryEx query) { //
     * TODO Auto-generated method stub return null; }
     */
}
