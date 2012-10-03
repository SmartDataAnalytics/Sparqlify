package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.core.domain.VarBinding;

import com.hp.hpl.jena.sparql.expr.Expr;

public interface ExprEvaluator {

	Expr eval(Expr expr, VarBinding binding);
}
