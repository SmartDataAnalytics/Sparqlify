package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.views.ExprCopy;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;




public class SqlExprCopy {
	
	private static SqlExprCopy instance;
	
	public static SqlExprCopy getInstance()
	{
		if(instance == null) {
			instance = new SqlExprCopy();
		}
		return instance;
	}

	
	/**
	 * Creates a copy of an expression, with different arguments.
	 * 
	 * 
	 * @param proto
	 * @param args
	 */
	public SqlExpr _copy(SqlExpr proto, List<SqlExpr> args)
	{
		return (SqlExpr)MultiMethod.invokeStatic(ExprCopy.class, "copy", proto, args);
	}
	
	/*
    public static SqlExprList _copyList(Iterable<SqlExpr> exprs) {
    	SqlExprList result = new SqlExprList();

    	for(SqlExpr expr : exprs) {
    		result.add(copy(expr));
    	}
    
    	return result;
    }*/

    public S_Equals copy(S_Equals sqlExpr, List<SqlExpr> args) {
    	return new S_Equals(args.get(0), args.get(1));
    }

    public S_Concat copy(S_Concat sqlExpr, List<SqlExpr> args) {
    	return new S_Concat(args);
    }

    public SqlExprColumn copy(SqlExprColumn sqlExpr, List<SqlExpr> args) {
    	return new SqlExprColumn(sqlExpr.getTableName(), sqlExpr.getColumnName(), sqlExpr.getDatatype());
    }

}