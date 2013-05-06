package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_LogicalAnd
	extends SqlExpr2
{
	public S_LogicalAnd(SqlExpr left, SqlExpr right) {
		super(TypeToken.Boolean, "logicalAnd", left, right);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print("LogicalAnd");
		writeArgs(writer);
	}

	@Override
	public S_LogicalAnd copy(SqlExpr left, SqlExpr right) {
		S_LogicalAnd result = new S_LogicalAnd(left, right);
		return result;
	}

	
	public static S_LogicalAnd create(SqlExpr a, SqlExpr b) {
		return new S_LogicalAnd(a, b);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
