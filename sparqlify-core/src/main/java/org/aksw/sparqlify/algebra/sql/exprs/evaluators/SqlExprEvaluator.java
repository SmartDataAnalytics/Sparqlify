package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

/**
 * Implements the evaluation of function based on a list of arguments.
 * 
 * @author raven
 *
 */
public interface SqlExprEvaluator {
	SqlExpr eval(List<SqlExpr> args);
}
