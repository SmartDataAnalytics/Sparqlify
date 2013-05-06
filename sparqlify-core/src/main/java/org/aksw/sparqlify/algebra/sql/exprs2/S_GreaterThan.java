package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_GreaterThan
	extends SqlExpr2
{
	public S_GreaterThan(SqlExpr left, SqlExpr right) {
		super(TypeToken.Boolean, "greaterThan", left, right);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print(getName());
		writeArgs(writer);
	}

	@Override
	public S_GreaterThan copy(SqlExpr left, SqlExpr right) {
		S_GreaterThan result = new S_GreaterThan(left, right);
		return result;
	}
	
	public static S_GreaterThan create(SqlExpr a, SqlExpr b) {
		return new S_GreaterThan(a, b);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
