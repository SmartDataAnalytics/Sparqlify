package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

/**
 * Implements the "evaluation" of function based on a list of arguments. 
 * The result of an evaluation is an SqlExpr object and may be an expression or constant value.
 * 
 * Note that implementations of these interface are only used to
 * OVERRIDE a TypeSystem's default behaviour.
 * 
 * Usually the default behaviour is to merely obtain a datatype for the expression in question.
 * This is done by looking up a function definition, take
 * its return type, and return a corresponding S_Function object.
 * 
 * 
 * @author raven
 *
 */
public interface SqlExprEvaluator {
	SqlExpr eval(List<SqlExpr> args);
}
