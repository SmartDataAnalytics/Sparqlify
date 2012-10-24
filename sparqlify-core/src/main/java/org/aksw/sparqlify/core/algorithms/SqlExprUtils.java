package org.aksw.sparqlify.core.algorithms;

import org.aksw.commons.factory.Factory2;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.expr.util.ExprUtils;

public class SqlExprUtils
{
	public static SqlExpr orifyBalanced(Iterable<SqlExpr> exprs) {
		return ExprUtils.opifyBalanced(exprs, new Factory2<SqlExpr>() {
			@Override
			public SqlExpr create(SqlExpr a, SqlExpr b)
			{
				return new S_LogicalOr(a, b);
			}
		});		
	}	
	
	public static SqlExpr andifyBalanced(Iterable<SqlExpr> exprs) {
		return ExprUtils.opifyBalanced(exprs, new Factory2<SqlExpr>() {
			@Override
			public SqlExpr create(SqlExpr a, SqlExpr b)
			{
				return new S_LogicalAnd(a, b);
			}
		});		
	}	
	
	public static boolean containsFalse(Iterable<SqlExpr> exprs, boolean includeTypeErrors) {
		for(SqlExpr expr : exprs) {
			if(S_Constant.FALSE.equals(expr) || (includeTypeErrors && S_Constant.TYPE_ERROR.equals(expr))) {
				return true;
			}
		}
		
		return false;
	}

}
