package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.core.domain.input.SparqlSqlRewrite;

import com.hp.hpl.jena.query.Query;

public interface SparqlSqlRewriter {
	SparqlSqlRewrite rewrite(Query query);
}
