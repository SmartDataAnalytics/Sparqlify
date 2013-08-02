package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.apache.jena.atlas.io.IndentedWriter;

public class S_Substract
	extends SqlExpr2
{
	public S_Substract(SqlExpr left, SqlExpr right) {
		super(TypeToken.Int, "substract", left, right);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print("substract");
		writeArgs(writer);
	}

	@Override
	public S_Substract copy(SqlExpr left, SqlExpr right) {
		S_Substract result = new S_Substract(left, right);
		return result;
	}

	
	public static S_Substract create(SqlExpr a, SqlExpr b) {
		return new S_Substract(a, b);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
