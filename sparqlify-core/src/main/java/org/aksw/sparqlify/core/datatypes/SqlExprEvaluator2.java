package org.aksw.sparqlify.core.datatypes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

public abstract class SqlExprEvaluator2
	implements SqlExprEvaluator
{
	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		SqlExpr a = args.get(0);
		SqlExpr b = args.get(1);
		
		SqlExpr result = eval(a, b);
		return result;
	}
	
	public abstract SqlExpr eval(SqlExpr a, SqlExpr b);
}

