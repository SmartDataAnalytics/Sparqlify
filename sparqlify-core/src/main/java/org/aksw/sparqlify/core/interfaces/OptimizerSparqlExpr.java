package org.aksw.sparqlify.core.interfaces;

import com.hp.hpl.jena.sparql.expr.Expr;

public interface OptimizerSparqlExpr {
	Expr optimize(Expr expr);
}
