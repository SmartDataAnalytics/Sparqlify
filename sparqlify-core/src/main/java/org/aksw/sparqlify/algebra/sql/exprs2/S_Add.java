package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_Add
	extends SqlExpr2
{
	public S_Add(SqlExpr left, SqlExpr right) {
		super(TypeToken.Int, "add", left, right);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print("add");
		writeArgs(writer);
	}

	@Override
	public S_Add copy(SqlExpr left, SqlExpr right) {
		S_Add result = new S_Add(left, right);
		return result;
	}

	
	public static S_Add create(SqlExpr a, SqlExpr b) {
		return new S_Add(a, b);
	}

	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
