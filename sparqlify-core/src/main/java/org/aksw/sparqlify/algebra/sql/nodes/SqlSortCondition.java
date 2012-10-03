package org.aksw.sparqlify.algebra.sql.nodes;

import com.hp.hpl.jena.sparql.expr.Expr;


public class SqlSortCondition{
	private Expr expr;
	private int direction;

	public SqlSortCondition(Expr expr, int direction) {
		super();
		this.expr = expr;
		this.direction = direction;
	}

	public Expr getExpression() {
		return expr;
	}

	public int getDirection() {
		return direction;
	}
}
