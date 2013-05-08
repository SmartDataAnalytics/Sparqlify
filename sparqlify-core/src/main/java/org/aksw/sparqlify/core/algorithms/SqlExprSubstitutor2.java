package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.commons.factory.Factory1;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;

import com.google.common.collect.Iterables;

public class SqlExprSubstitutor2 {
	
	public static List<Collection<SqlExpr>> substitute(List<Collection<SqlExpr>> nf, Factory1<SqlExpr> postTraversalTransformer) {
		List<Collection<SqlExpr>> result = new ArrayList<Collection<SqlExpr>>();
		for(Collection<SqlExpr> clause : nf) {
			Collection<SqlExpr> newClause = substitute(clause, postTraversalTransformer);
			
			result.add(newClause);
		}
		
		return result;
	}
	
	public static List<SqlExpr> substitute(Collection<SqlExpr> exprs, Factory1<SqlExpr> postTraversalTransformer) {
		List<SqlExpr> result = new ArrayList<SqlExpr>(exprs.size());
		
		for(SqlExpr expr : exprs) {
			SqlExpr newExpr = substitute(expr, postTraversalTransformer);
			
			result.add(newExpr);
		}
		
		return result;
	}

	public static SqlExpr substitute(SqlExpr expr, Factory1<SqlExpr> postTraversalTransformer) {

		if(expr == null) {
			System.out.println("Null expr");
		}
		
		assert expr != null : "Expr must not be null";
		assert expr.getType() != null : "Type of exprs must not be null";
		
		SqlExpr result;
		switch(expr.getType()) {
		case Constant: {
			//result = expr;
			result = postTraversalTransformer.create(expr);
			break;
		}
		case Function: {
			SqlExprFunction fn = expr.asFunction();

			List<SqlExpr> args = fn.getArgs();
			
			assert !Iterables.contains(args, null) : "Null argument in expr: " + fn;
			
			List<SqlExpr> newArgs = substitute(args, postTraversalTransformer);
			SqlExpr tmp = fn.copy(newArgs);
			
			result = postTraversalTransformer.create(tmp);
			break;
		}
		case Variable: {
			//S_ColumnRef columnRef = (S_ColumnRef)expr;
			//SqlExprVar var = expr.asVariable();
			//String name = var.getVarName();
			
			result = postTraversalTransformer.create(expr);//map.get(name);
			
			/*
			if(substitute != null) {
				result = substitute;
			} else {
				result = expr;
			}
			)*/
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

}