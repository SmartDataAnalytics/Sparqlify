package org.aksw.sparqlify.core.cast;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

public interface ExprSubstitutor {

	/**
	 * In the extreme case, this method may base return different results
	 * based on the given arguments and their datatypes.
	 */
	SqlExpr create(List<SqlExpr> expr);
	//Expr create(Iterable<Expr> expr);
}