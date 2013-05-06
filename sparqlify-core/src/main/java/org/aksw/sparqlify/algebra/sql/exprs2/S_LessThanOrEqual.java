package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_LessThanOrEqual
	extends SqlExpr2
{
	public S_LessThanOrEqual(SqlExpr left, SqlExpr right) {
		super(TypeToken.Boolean, "lessThanOrEqual", left, right);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print(getName());
		writeArgs(writer);
	}

	@Override
	public S_LessThanOrEqual copy(SqlExpr left, SqlExpr right) {
		S_LessThanOrEqual result = new S_LessThanOrEqual(left, right);
		return result;
	}
	
	public static S_LessThanOrEqual create(SqlExpr a, SqlExpr b) {
		return new S_LessThanOrEqual(a, b);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
