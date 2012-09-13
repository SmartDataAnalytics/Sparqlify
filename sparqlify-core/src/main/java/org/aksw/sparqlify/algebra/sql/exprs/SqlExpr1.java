package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;

public abstract class SqlExpr1
	extends SqlExprBase
{
	private SqlExpr expr;
	
	public SqlExpr1(SqlExpr expr, SqlDatatype datatype) {
		super(datatype);
		this.expr = expr;
	}

	public SqlExpr getExpr() {
		return expr;
	}
	
	public List<SqlExpr> getArgs() {
		return Arrays.asList(expr);
	}
	
	/*
	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName(), expr);
	}*/
}
