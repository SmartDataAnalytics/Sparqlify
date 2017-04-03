package org.aksw.sparqlify.core;

import org.apache.jena.sparql.expr.Expr;

/**
 * Utility class that stores an (argument) index with an expression.
 * Would like to get rid of it this class though.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ArgExpr {
	private Expr expr;
	private int index;
	
	public ArgExpr(Expr expr, int index)
	{
		this.expr = expr;
		this.index = index;
	}
	
	public Expr getExpr() {
		return expr;
	}
	
	public int getIndex() {
		return index;
	}

	@Override
	public String toString() {
		return "ArgExpr [expr=" + expr + ", index=" + index + "]";
	}
}