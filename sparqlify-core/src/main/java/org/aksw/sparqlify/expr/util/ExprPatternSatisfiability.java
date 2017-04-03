package org.aksw.sparqlify.expr.util;

import java.util.Map;
import java.util.Set;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.core.RdfTermPattern;
import org.aksw.sparqlify.core.RdfTermPatternDerivation;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalNot;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;



public class ExprPatternSatisfiability {
	
	/**
	 * Given a set of views and a query such as:
	 * select * { ?s ?p ?o . Filter(?s = 'myconstant'). }
	 * 
	 * For each view instance matching the query-triple, it must be possible tha
	 * ?s equals the constant, hence pattern(?s).contains(myconstant).
	 * 
	 * 
	 */
	
	public static void isSatisfiable(Set<Set<Expr>> dnf, Map<Var, RdfTermPattern> varToPattern) {
		
		for(Set<Expr> clause : dnf) {
			for(Expr expr : clause) {
				
			}
		}
	}
	
	public static Boolean isSatisfiable(Expr expr) {
		return MultiMethod.invokeStatic(ExprPatternSatisfiability.class, "_isSatisfiable", expr);
	}
	
	public static Boolean _isSatisfiable(E_LogicalAnd expr) {
		Boolean a = isSatisfiable(expr.getArg1());
		if(a == false) {
			return false;
		}
		
		Boolean b = isSatisfiable(expr.getArg2());
		return b == true ? a : b;
		
	}
	
	public static Boolean _isSatisfiable(E_LogicalNot expr) {
		Boolean a = isSatisfiable(expr.getArg());

		return a == null ? null : !a;
	}
	
	public static Boolean _isSatisfiable(E_LogicalOr expr) {
		Boolean a = isSatisfiable(expr.getArg1());
		if(a == true) {
			return true;
		}
		
		Boolean b = isSatisfiable(expr.getArg2());
		return b == false ? a : b;
	}
	
	public static Boolean _isSatisfiable(E_Equals expr) {
		RdfTermPattern a = getPattern(expr.getArg1());
		RdfTermPattern b = getPattern(expr.getArg1());
		
		RdfTermPattern intersection = RdfTermPattern.intersect(a, b);
		return intersection.isSatisfiable(); 
	}
	
	
	public static RdfTermPattern getPattern(Expr expr) {
		return RdfTermPatternDerivation.deriveRegex(expr);
		//return MultiMethod.invokeStatic(ExprPatternSatisfiability.class, "_getPattern", expr);
	}
	
	/*
	public static RdfTermPattern _getPattern(ExprVar expr) {
		
	}
	
	
	public static RdfTermPattern _getPattern(NodeValue node) {
		RdfTermPatternDerivation.
	}
	*/
}
