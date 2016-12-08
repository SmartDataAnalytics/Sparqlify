package org.aksw.sparqlify.core.interfaces;

import org.apache.jena.sparql.expr.Expr;

public interface OptimizerSparqlExpr {
	Expr optimize(Expr expr);
}
