package org.aksw.sparqlify.core.algorithms;

import org.apache.jena.sparql.expr.ExprList;

public class ExprListUtils {
	enum Type {
		TRUE,
		FALSE,
		EXPR,
		EXPRLIST
	};

	/**
	 * a1 AND ... AND an
	 * 
	 * null: FALSE
	 * empty: TRUE
	 * expressions otherwise
	 * 
	 * @param exprs
	 * @return
	 */
	public static Type getConjunctionType(ExprList exprs) {
		if(exprs == null) {
			return Type.FALSE;
		} else if(exprs.isEmpty()) {
			return Type.TRUE;
		} else {
			return Type.EXPRLIST;
		}
	}	

	/**
	 * a1 AND ... AND an
	 * 
	 * null: TRUE
	 * empty: FALSE
	 * expressions otherwise
	 * 
	 * @param exprs
	 * @return
	 */
	public static Type getDisjunctionType(ExprList exprs) {
		if(exprs == null) {
			return Type.TRUE;
		} else if(exprs.isEmpty()) {
			return Type.FALSE;
		} else {
			return Type.EXPRLIST;
		}
	}	
}