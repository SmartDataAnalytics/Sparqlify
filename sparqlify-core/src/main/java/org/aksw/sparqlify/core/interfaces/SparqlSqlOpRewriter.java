package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.core.domain.input.SparqlSqlOpRewrite;

import org.apache.jena.query.Query;

public interface SparqlSqlOpRewriter {
	SparqlSqlOpRewrite rewrite(Query query);
}
