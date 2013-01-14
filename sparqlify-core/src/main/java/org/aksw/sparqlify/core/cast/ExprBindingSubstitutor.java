package org.aksw.sparqlify.core.cast;

import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

public interface ExprBindingSubstitutor {
	Expr substitute(Expr expr, Map<Var, Expr> binding);
}
