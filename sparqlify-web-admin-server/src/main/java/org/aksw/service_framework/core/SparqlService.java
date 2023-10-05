package org.aksw.service_framework.core;

import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;

public interface SparqlService {
    Object getConfig();
    QueryExecutionFactory getSparqlService();
}
