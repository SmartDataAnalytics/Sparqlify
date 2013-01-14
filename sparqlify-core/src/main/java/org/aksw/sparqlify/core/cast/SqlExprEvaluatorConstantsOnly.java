package org.aksw.sparqlify.core.cast;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.expr.util.SqlExprUtils;

public abstract class SqlExprEvaluatorConstantsOnly
	implements SqlExprEvaluator
{
	public SqlExpr eval(List<SqlExpr> args) {
		boolean isConstantsOnly = SqlExprUtils.isConstantsOnly(args);
		
		SqlExpr result;
		if(isConstantsOnly) {
			result = _eval(args);
		} else {
			result = null;
		}
		
		return result;
	}
	
	protected abstract SqlExpr _eval(List<SqlExpr> args);
}