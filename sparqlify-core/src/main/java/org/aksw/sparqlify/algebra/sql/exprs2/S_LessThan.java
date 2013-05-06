package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_LessThan
	extends SqlExpr2
{
	public S_LessThan(SqlExpr left, SqlExpr right) {
		super(TypeToken.Boolean, "lessThan", left, right);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print(getName());
		writeArgs(writer);
	}

	@Override
	public S_LessThan copy(SqlExpr left, SqlExpr right) {
		S_LessThan result = new S_LessThan(left, right);
		return result;
	}
	
	public static S_LessThan create(SqlExpr a, SqlExpr b) {
		return new S_LessThan(a, b);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
