package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

public abstract class SqlExprEvaluator1
	implements SqlExprEvaluator
{
	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		SqlExpr a = args.get(0);
		
		SqlExpr result = eval(a);
		return result;
	}
	
	public abstract SqlExpr eval(SqlExpr a);
}

