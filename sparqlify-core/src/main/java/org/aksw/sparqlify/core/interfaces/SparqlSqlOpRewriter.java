package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.core.domain.input.SparqlSqlOpRewrite;

import com.hp.hpl.jena.query.Query;

public interface SparqlSqlOpRewriter {
	SparqlSqlOpRewrite rewrite(Query query);
}
