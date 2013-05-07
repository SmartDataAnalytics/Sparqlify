package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;

public class S_Cast
	extends SqlExpr1
{
	public S_Cast(TypeToken type, SqlExpr expr) {
		super(type, "cast", expr);
	}

	@Override
	public SqlExprFunction copy(SqlExpr arg) {
		return new S_Cast(datatype, arg);
	}	

	public static S_Cast create(TypeToken type, SqlExpr expr) {
		S_Cast result = new S_Cast(type, expr);
		return result;
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
