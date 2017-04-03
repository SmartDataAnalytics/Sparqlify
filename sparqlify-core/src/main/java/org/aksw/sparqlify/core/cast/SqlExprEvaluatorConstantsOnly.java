package org.aksw.sparqlify.core.cast;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.sparql.algebra.transform.SqlExprUtils;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator;

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