package org.aksw.sparqlify.algebra.sql.datatype;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr0;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr1;

public class SqlDatatypeUtil {
	
	public static void fill(SqlExpr expr) 
	{
		MultiMethod.invokeStatic(SqlDatatypeUtil.class, "_fill", expr);
		//SqlDatatypeEvaluator.eval(expr);
	}
	
	public static void _fill(SqlExpr0 expr) {
		// Nothing to do
	}
	
	public static void _fill(SqlExpr1 expr) {
		fill(expr.getExpr());
		
	}
}
