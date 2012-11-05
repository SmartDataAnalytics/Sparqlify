package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprVar;

public class SqlExprSubstitutor {

	private Map<String, ? extends SqlExpr> map;

	public SqlExprSubstitutor(Map<String, ? extends SqlExpr> map) {
		this.map = map;
	}

	
	public List<SqlExpr> substitute(List<SqlExpr> exprs) {
		List<SqlExpr> result = new ArrayList<SqlExpr>(exprs.size());
		
		for(SqlExpr expr : exprs) {
			SqlExpr newExpr = substitute(expr);
			
			result.add(newExpr);
		}
		
		return result;
	}
	
	public SqlExpr substitute(SqlExpr expr) {

		if(expr == null) {
			System.out.println("Null expr");
		}
		
		assert expr != null : "Expr must not be null";
		assert expr.getType() != null : "Type of exprs must not be null";
		
		SqlExpr result;
		switch(expr.getType()) {
		case Constant: {
			result = expr;
			break;
		}
		case Function: {
			SqlExprFunction fn = expr.asFunction();
			
			List<SqlExpr> newArgs = substitute(fn.getArgs());
			result = fn.copy(newArgs);
			break;
		}
		case Variable: {
			//S_ColumnRef columnRef = (S_ColumnRef)expr;
			SqlExprVar var = expr.asVariable();
			String name = var.getVarName();
			
			SqlExpr substitute = map.get(name);
			if(substitute != null) {
				result = substitute;
			} else {
				result = expr;
			}
			
			//columnRef.ge
			
			// The datatype of the substituted expression must be an equal-or-sub-type of the current one. 	
			//result = null;
			//if(true) { throw new RuntimeException("todo"); }
			break;
		}
		default: {
			throw new RuntimeException("Should not happen");
		}
		}
		
		return result;
	}

	public SqlExpr trySubstitute(S_ColumnRef sqlExpr) {
		SqlExpr substitute = map.get(sqlExpr);

		return (substitute == null) ? sqlExpr : substitute;
	}

	
	public static SqlExprSubstitutor create(Map<String, SqlExpr> map) {
		SqlExprSubstitutor result = new SqlExprSubstitutor(map);
		return result;
	}
}
