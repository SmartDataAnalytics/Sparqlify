package org.aksw.sparqlify.core.cast;

import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;

public interface ExprBindingSubstitutor {
	Expr substitute(Expr expr, Map<Var, Expr> binding);
}
