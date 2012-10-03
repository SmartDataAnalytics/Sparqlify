package org.aksw.sparqlify.core.algorithms;

import java.util.Map;

import org.aksw.sparqlify.core.SqlDatatype;

import com.hp.hpl.jena.sparql.expr.Expr;

public interface DatatypeAssigner {
	SqlDatatype assign(Expr expr, Map<String, SqlDatatype> typeMap);
}
