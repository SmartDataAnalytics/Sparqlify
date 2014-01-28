package org.aksw.service_framework.core;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;

public interface SparqlService {
    Object getConfig();
    QueryExecutionFactory getSparqlService();
}