package org.aksw.sparqlify.core.algorithms;

import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public interface ExprEvaluator {

	Expr eval(Expr expr, Map<Var, Expr> binding);//VarBinding binding);
}
