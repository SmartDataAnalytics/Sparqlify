package org.aksw.service_framework.core;

import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;

public interface SparqlService {
    Object getConfig();
    QueryExecutionFactory getSparqlService();
}