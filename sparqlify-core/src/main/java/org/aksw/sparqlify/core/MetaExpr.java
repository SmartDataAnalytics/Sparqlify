package org.aksw.sparqlify.core;

import org.apache.jena.sparql.expr.Expr;

/**
 * An expr + metadata
 * 
 * @author raven
 *
 */
public class MetaExpr {
	private Expr expr;
	private RdfTermPattern regex;

	public MetaExpr(Expr expr, RdfTermPattern regex) {
		super();
		this.expr = expr;
		this.regex = regex;
	}

	public Expr getExpr() {
		return expr;
	}

	public RdfTermPattern getRegex() {
		return regex;
	}	
}

