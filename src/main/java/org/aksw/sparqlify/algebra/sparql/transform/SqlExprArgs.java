package org.aksw.sparqlify.algebra.sparql.transform;

import mapping.ExprCopy;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr1;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr2;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprList;


public class SqlExprArgs {

	public static SqlExprList getArgs(SqlExpr sqlExpr)
	{
		return (SqlExprList)MultiMethod.invokeStatic(ExprCopy.class, "_getArgs", sqlExpr);
	}
	
	public static SqlExprList createList(Iterable<SqlExpr> args) {
		SqlExprList result = new SqlExprList();
		for(SqlExpr arg : args) {
			result.add(arg);
		}
		
		return result;
	}

	public static SqlExprList create(SqlExpr... args) {
		SqlExprList result = new SqlExprList();
		for(SqlExpr arg : args) {
			result.add(arg);
		}
		
		return result;
	}

    public static SqlExprList _getArgs(SqlExprColumn sqlExpr) {
    	return create();
    }

    public static SqlExprList _getArgs(SqlExpr1 sqlExpr) {
    	return create(sqlExpr.getExpr());
    }

    public static SqlExprList _getArgs(SqlExpr2 sqlExpr) {
    	return create(sqlExpr.getLeft(), sqlExpr.getRight());
    }

    public static SqlExprList _getArgs(S_Concat sqlExpr) {
    	return createList(sqlExpr.getArgs());
    }
}
