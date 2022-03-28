package org.aksw.service_framework.core;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;

public class SparqlServiceImpl<T>
    implements SparqlService {

    private T config;
    private QueryExecutionFactory sparqlService;

    public SparqlServiceImpl(T config, QueryExecutionFactory sparqlService) {
        super();
        this.config = config;
        this.sparqlService = sparqlService;
    }

    @Override
    public T getConfig() {
        return config;
    }

    @Override
    public QueryExecutionFactory getSparqlService() {
        return sparqlService;
    }

}