package org.aksw.sparqlify.algebra.sql.nodes;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;


public class SqlSortCondition{
	private SqlExpr expr;
	private int direction;

	public SqlSortCondition(SqlExpr expr, int direction) {
		super();
		this.expr = expr;
		this.direction = direction;
	}

	public SqlExpr getExpression() {
		return expr;
	}

	public int getDirection() {
		return direction;
	}
}
