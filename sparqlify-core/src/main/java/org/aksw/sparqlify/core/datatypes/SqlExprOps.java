package org.aksw.sparqlify.core.datatypes;

import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;


public class SqlExprOps {
	
	public static int exprToTruthValue(SqlExpr a) {
		int result;
		if(!a.isConstant()) {
			result = 4;
		} else if(a.equals(S_Constant.FALSE)) {
			result = 0;
		} else if(a.equals(S_Constant.TRUE)) {
			result = 1;
		} else if(a.equals(S_Constant.TYPE_ERROR)) {
			result = 2;
		} else if(a.asConstant().getValue() == null) {
			result = 3;
		} else {
			// Non boolean constant; treat as type error
			result = 2;
		}
		
		return result;
	}
	
	public static final SqlExpr O = S_Constant.FALSE;
	public static final SqlExpr l = S_Constant.TRUE;
	public static final SqlExpr e = S_Constant.TYPE_ERROR;
	public static final SqlExpr n = new S_Constant(TypeToken.Boolean, null);
	
	//public static final SqlExpr d = null; // "Don't know"; used if an argument is not a constant
	public static final SqlExpr a = new S_Constant(TypeToken.Special, "firstArg");
	public static final SqlExpr b = new S_Constant(TypeToken.Special, "secondArg");
	public static final SqlExpr c = new S_Constant(TypeToken.Special, "originalArgs");

	
	public static final SqlExpr[][] tableLogicalAnd = new SqlExpr[][] {
		/*     0  1  e  n  d*/
		/*0*/ {O, O, O, O, O},
		/*1*/ {O, l, e, n, b},
		/*e*/ {O, e, e, e, c},
		/*n*/ {O, n, e, n, c},
		/*d*/ {O, a, c, c, c}
	};

	public static final SqlExpr[][] tableLogicalOr = new SqlExpr[][] {
		/*     0  1  e  n  d*/
		/*0*/ {O, l, e, n, b},
		/*1*/ {l, l, l, l, l},
		/*e*/ {e, l, e, e, c},
		/*n*/ {n, l, e, n, c},
		/*d*/ {a, l, c, c, c}
	};
	
	public static final SqlExpr[] tableLogicalNot = new SqlExpr[] {
		/*0*/ l, 
		/*1*/ O,
		/*e*/ e,
		/*n*/ n,
		/*d*/ a
	};
	
	
	public static SqlExpr interpretResult(SqlExpr tmp, SqlExpr x, SqlExpr y) {
		SqlExpr result;
		if(tmp == a) {
			result = x;
		} else if(tmp == b) {
			return y;
		} else if(tmp == c) {
			return null;
		} else {
			result = tmp;
		}
		return result;
	}
	
	/**
     *
     *   0 1 e
     * 0 0 0 0
     * 1 0 1 e
     * e 0 e e
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public static SqlExpr logicalAnd(SqlExpr x, SqlExpr y) {

		int ix = exprToTruthValue(x);
		int iy = exprToTruthValue(y);
		
		SqlExpr tmp = tableLogicalAnd[ix][iy];
		
		SqlExpr result = interpretResult(tmp, x, y);		
		return result;
	}
	
	
	public static SqlExpr logicalOr(SqlExpr x, SqlExpr y) {
		int ix = exprToTruthValue(x);
		int iy = exprToTruthValue(y);
		
		SqlExpr tmp = tableLogicalOr[ix][iy];

		SqlExpr result = interpretResult(tmp, x, y);		
		return result;
	}
	

	
	public static SqlExpr logicalNot(SqlExpr x) {
		int ix = exprToTruthValue(x);
		SqlExpr tmp = tableLogicalNot[ix];

		SqlExpr result;
		// If the original arg is returned, we need to negate it!		
		if(tmp == a) {
			//result = S_LogicalNot.create(x);
			return null;
		} else {
			result = interpretResult(tmp, x, null);			
		}
		
		return result;
	}	
}
