package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_Multiply
	extends SqlExpr2
{
	public S_Multiply(SqlExpr left, SqlExpr right) {
		super(TypeToken.Int, "multiply", left, right);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print(super.getName());
		writeArgs(writer);
	}

	@Override
	public S_Multiply copy(SqlExpr left, SqlExpr right) {
		S_Multiply result = new S_Multiply(left, right);
		return result;
	}

	
	public static S_Multiply create(SqlExpr a, SqlExpr b) {
		return new S_Multiply(a, b);
	}
}
