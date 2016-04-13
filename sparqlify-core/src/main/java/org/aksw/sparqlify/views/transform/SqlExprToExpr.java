package org.aksw.sparqlify.views.transform;

import java.util.Arrays;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Equals;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs.S_Regex;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.aksw.sparqlify.core.MakeNodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_Regex;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * WARNING: This method is used in satisfiablity checks.
 * It does not translate SqlExprs correctly back the Exprs.
 * 
 * For instance, SqlExprs which cannot be easily evaluated
 * (such as the spatial 'intersects')
 * are currently simply transformed to "true",
 * which means that it is assumed that it is satisfiable.
 * 
 * Convert an SqlExpr to a sparql expr.
 * This is done in order to reuse the Expr stuff
 * 
 * 
 * 
 * 
 * @author raven
 *
 */
public class SqlExprToExpr {
	
	public static final NodeValue UNKNOWN = NodeValue.makeString("//maybe//");
	
	private static final Logger logger = LoggerFactory.getLogger(SqlExprToExpr.class);
	
	public static Expr convert(SqlExpr expr) {
		Expr result = (Expr)MultiMethod.invokeStatic(SqlExprToExpr.class, "_convert", expr);
		
		// Check for unknown values
		if(checkUnknown(result)) {
			return UNKNOWN;
		}
		
		return result;
	}

	public static boolean checkUnknown(Expr expr) {
		if(expr.equals(UNKNOWN)) {
			return true;
		}
		else if(expr.isFunction()) {
			ExprFunction func = expr.getFunction();
			for(Expr arg : func.getArgs()) {
				if(arg.equals(UNKNOWN)) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static Expr _convert(SqlExpr expr) {

		//throw new RuntimeException("Implement me " + expr.getClass());
		logger.warn("Unknown satisfiability: " + expr.getClass());
		
		return UNKNOWN;
		
		// NOTE Can't assume true, because it may yield expressions such as
		// // x = "my string"
		// true = "my string" -> might falsely yield false at the end of the day
		
		//logger.warn("No handling for '" + expr.getClass() + "' - assuming 'true'");
		
		
		//return NodeValue.TRUE;
	}
	
	// TODO Convert comparative expressions
	/*
	public static Expr _convert(S_LessThan expr) {
		return new E_LessThan(convert(expr.getLeft()), convert(expr.getRight()));
	}*/
	
	
	@SuppressWarnings("unchecked")
	public static ExprList evalArgs(SqlExpr ...args) {
		return evalArgs(Arrays.asList(args));
	}
	
	public static ExprList evalArgs(Iterable<SqlExpr> args) {
		ExprList result = new ExprList();
		for(SqlExpr arg : args) {
			Expr expr = convert(arg);
			result.add(expr);
		}
		return result;
	}
	
	
	public static Expr _convert(S_Equals expr) {
		return new E_Equals(convert(expr.getLeft()), convert(expr.getRight()));
	}
	
	public static Expr _convert(S_Concat expr) {
		return new E_StrConcatPermissive(evalArgs(expr.getArgs()));
	}
	
	
	public int exprToStrength(Expr e) {
		if(NodeValue.TRUE.equals(e)) {
			return 0;
		} else if (UNKNOWN.equals(e)) {
			return 1;
		} else if (NodeValue.FALSE.equals(e)) {
			return 2;
		} else { // Treat as unknown
			return 1;
		}
	}
	
	public static Expr _convert(S_LogicalAnd expr) {
		// Strength (weakest-strongest): true, unkown, false 
		Expr a = convert(expr.getLeft());
		Expr b = convert(expr.getRight());
		
		// TODO do three valued logic...
		
		return new E_LogicalAnd(a, b);
	}

	public static Expr _convert(S_LogicalOr expr) {
		return new E_LogicalOr(convert(expr.getLeft()), convert(expr.getRight()));
	}

	public static Expr _convert(S_LogicalNot expr) {
		return new E_LogicalNot(convert(expr.getExpr()));
	}
	
	public static Expr _convert(S_Regex expr) {
		return new E_Regex(convert(expr.getExpr()), expr.getPattern(), expr.getFlags());
	}
	
	public static Expr _convert(SqlExprValue expr) {
		return MakeNodeValue.makeNodeValue(expr.getObject());
	}
	
	
	public static Expr _convert(SqlExprColumn expr) {
		// TODO Encode the expr into a magic variable name
		Var var = Var.alloc("sql" + expr.getFullColumnName()); 
		return new ExprVar(var);
	}

}
