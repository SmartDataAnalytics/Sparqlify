package org.aksw.sparqlify.expr.util;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;

// TODO Duplicate name: There is transform.SqlExprUtils
public class SqlExprUtils {
	public static List<SqlExprColumn> getColumnsMentioned(SqlExpr expr) {
		List<SqlExprColumn> result = new ArrayList<SqlExprColumn>();
		getColumnsMentioned(expr, result);
		return result;
	}
	
	public static void getColumnsMentioned(SqlExpr expr, List<SqlExprColumn> list) {
		for(SqlExpr arg : expr.getArgs()) {
			if(arg instanceof SqlExprColumn) {
				if(!list.contains(arg)) {
					list.add((SqlExprColumn)arg);
				}
			} else {
				getColumnsMentioned(arg, list);
			}
		}
	}

}
