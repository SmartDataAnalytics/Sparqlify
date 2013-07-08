package org.aksw.sparqlify.expr.util;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.core.TypeToken;

// TODO Duplicate name: There is transform.SqlExprUtils
public class SqlExprUtils {

	public static List<TypeToken> getTypes(List<SqlExpr> args) {
		List<TypeToken> argTypes = new ArrayList<TypeToken>(args.size());
		for(SqlExpr newArg : args) {
			argTypes.add(newArg.getDatatype());
		}
		
		return argTypes;
	}

	
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

	
	public static boolean isConstantsOnly(Iterable<SqlExpr> exprs) {
		for(SqlExpr expr : exprs) {
			if(!expr.isConstant()) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isConstantArgsOnly(SqlExprFunction fn) {
		
		boolean result = isConstantsOnly(fn.getArgs());

		return result;
	}

}
