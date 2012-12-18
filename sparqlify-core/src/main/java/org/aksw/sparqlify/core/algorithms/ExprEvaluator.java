package org.aksw.sparqlify.core.algorithms;

import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * Evaluates an expression against a given binding (= variable assignment).
 * Unless all variables are bound, the result is not required to be a literal
 * value. In general, the result may be an expression that was derived from
 * the original one by means of transformations.
 * 
 * 
 * @author raven
 *
 */
public interface ExprEvaluator {

	Expr eval(Expr expr, Map<Var, Expr> binding);//VarBinding binding);
}
