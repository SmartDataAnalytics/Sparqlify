package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.core.SqlDatatype;

public abstract class SqlExpr2
	extends SqlExprBase
{
	private SqlExpr left;
	private SqlExpr right;

	public SqlExpr2(SqlExpr left, SqlExpr right, SqlDatatype datatype) {
		super(datatype);
		this.left = left;
		this.right = right;
	}

	public SqlExpr getLeft() {
		return left;
	}
	public SqlExpr getRight() {
		return right;
	}
	
	public List<SqlExpr> getArgs() {
		return Arrays.asList(left, right);
	}
	
	/*
	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName(), left, right);
	}*/
}
