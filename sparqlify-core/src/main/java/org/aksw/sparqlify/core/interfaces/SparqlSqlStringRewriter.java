package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.core.domain.input.SparqlSqlStringRewrite;
import org.apache.jena.query.Query;

public interface SparqlSqlStringRewriter {
	SparqlSqlStringRewrite rewrite(Query query);
}
