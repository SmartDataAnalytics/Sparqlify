package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlValue;

public class S_Cast
	extends SqlExpr2
{
	public S_Cast(TypeToken type, SqlExpr expr, SqlExpr typeExpr) {
		super(type, "cast", expr, typeExpr);
	}

	@Override
	public S_Cast copy(SqlExpr expr, SqlExpr typeExpr) {
		return new S_Cast(datatype, expr, typeExpr);
	}	

	public static S_Cast create(TypeToken type, SqlExpr expr) {
		S_Constant typeExpr = S_Constant.create(new SqlValue(TypeToken.String, type.getName()));
		
		S_Cast result = new S_Cast(type, expr, typeExpr);
		return result;
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
