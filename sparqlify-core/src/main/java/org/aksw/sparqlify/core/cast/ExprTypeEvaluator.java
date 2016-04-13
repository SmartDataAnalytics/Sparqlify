package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;

import org.apache.jena.sparql.expr.Expr;

/**
 * Interface for obtaining the result types of expressions.
 * The result type is usually identified by a URI.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
interface ExprTypeEvaluator {
	TypeToken evaluateType(Expr expr);
}