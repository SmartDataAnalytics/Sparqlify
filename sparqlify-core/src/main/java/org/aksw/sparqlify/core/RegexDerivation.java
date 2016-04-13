package org.aksw.sparqlify.core;

import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;

import org.apache.jena.sparql.expr.E_StrConcat;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;


public class RegexDerivation {
	public static String deriveRegex(Expr expr) {
		return MultiMethod.invokeStatic(RegexDerivation.class, "_deriveRegex",
				expr);
	}

	public static String concatPatterns(List<Expr> exprs) {
		String result = "";
		for (Expr expr : exprs) {
			result += deriveRegex(expr);
		}

		return result;
	}

	public static String _deriveRegex(E_StrConcat expr) {
		return concatPatterns(expr.getArgs());
	}

	public static String _deriveRegex(E_StrConcatPermissive expr) {
		return concatPatterns(expr.getArgs());
	}


	/*
	 * public static String regexEscape(String str) { String result = "";
	 * 
	 * for(int i = 0; i < str.length(); ++i) { char c = str.charAt(i);
	 * if(Character.isLetterOrDigit(c)) { result += c; } else { result += "\\" +
	 * c; }
	 * 
	 * }
	 * 
	 * return result; }
	 */

	public static String _deriveRegex(NodeValue expr) {
		// return Pattern.quote(expr.asUnquotedString());
		return RegexUtils.escape(expr.asUnquotedString());
	}

	// Fallback: Allow anything
	public static String _deriveRegex(Expr var) {
		return ".*";
	}

	public static String _deriveRegex(ExprVar var) {
		// TODO Use metadata about the var to return a more restrictive pattern
		return ".*";
	}
}
