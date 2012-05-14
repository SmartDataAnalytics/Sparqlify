package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.Arrays;

import mapping.SparqlifyConstants;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;

import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * Macro Expansion, e.g. beef:uri - beef:term()
 * 
 * @author raven
 * 
 */
public class SparqlSubstitute {
	public static Expr substituteExpr(Expr expr) {
		return (Expr) MultiMethod.invokeStatic(SparqlSubstitute.class,
				"substitute", expr);
	}

	public static Expr substitute(Expr expr) {
		return expr;
	}

	@SuppressWarnings("unchecked")
	public static ExprList makeExprList(Expr... exprs) {
		return new ExprList(Arrays.asList(exprs));
	}

	public static Expr substitute(E_Function expr) {
		
		if(expr.getFunctionIRI().equals(SparqlifyConstants.rdfTermLabel)) {
			if(expr.getArgs().size() != 4) {
				throw new RuntimeException("RdfTerm requires 4 arguments, instead got: " + expr);
			}
			
			return new E_RdfTerm(
					expr.getArg(1), expr.getArg(2), expr.getArg(3), expr.getArg(4));			
		} else if (expr.getFunctionIRI().equals(SparqlifyConstants.uriLabel)) {
			return new E_RdfTerm(
					NodeValue.makeDecimal(1), expr.getArgs().get(0),
					NodeValue.makeString(""), NodeValue.makeString(""));
		} else if (expr.getFunctionIRI()
				.equals(SparqlifyConstants.plainLiteralLabel)) {
			// The second argument is optional
			// If it is null, "", or not present, it will be treated as ""
			Expr lang = NodeValue.makeString("");

			if (expr.getArgs().size() == 2) {
				Expr tmp = expr.getArgs().get(1);
				if (tmp != null) {
					lang = tmp;
				}
			}

			return new E_RdfTerm(
					NodeValue.makeDecimal(2), expr.getArgs().get(0), lang,
					NodeValue.makeString(""));
		} else if (expr.getFunctionIRI()
				.equals(SparqlifyConstants.typedLiteralLabel)) {
			return new E_RdfTerm(
					NodeValue.makeDecimal(3), expr.getArgs().get(0),
					NodeValue.makeString(""), expr.getArgs().get(1));
		}

		return expr;
	}
	/*
	public static Expr substitute(E_Function expr) {
		if (expr.getFunctionIRI().equals(SparqlifyConstants.uriLabel)) {
			return new E_Function(SparqlifyConstants.rdfTermLabel, makeExprList(
					NodeValue.makeDecimal(1), expr.getArgs().get(0),
					NodeValue.makeString(""), NodeValue.makeString("")));
		} else if (expr.getFunctionIRI()
				.equals(SparqlifyConstants.plainLiteralLabel)) {
			// The second argument is optional
			// If it is null, "", or not present, it will be treated as ""
			Expr lang = NodeValue.makeString("");

			if (expr.getArgs().size() == 2) {
				Expr tmp = expr.getArgs().get(1);
				if (tmp != null) {
					lang = tmp;
				}
			}

			return new E_Function(SparqlifyConstants.rdfTermLabel, makeExprList(
					NodeValue.makeDecimal(2), expr.getArgs().get(0), lang,
					NodeValue.makeString("")));
		} else if (expr.getFunctionIRI()
				.equals(SparqlifyConstants.typedLiteralLabel)) {
			return new E_Function(SparqlifyConstants.rdfTermLabel, makeExprList(
					NodeValue.makeDecimal(3), expr.getArgs().get(0),
					NodeValue.makeString(""), expr.getArgs().get(1)));
		}

		return expr;
	}*/
}