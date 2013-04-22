package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_GreaterThanOrEqual
	extends SqlExpr2
{
	public S_GreaterThanOrEqual(SqlExpr left, SqlExpr right) {
		super(TypeToken.Boolean, "greaterThanOrEqual", left, right);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print(getName());
		writeArgs(writer);
	}

	@Override
	public S_GreaterThanOrEqual copy(SqlExpr left, SqlExpr right) {
		S_GreaterThanOrEqual result = new S_GreaterThanOrEqual(left, right);
		return result;
	}
	
	public static S_GreaterThanOrEqual create(SqlExpr a, SqlExpr b) {
		return new S_GreaterThanOrEqual(a, b);
	}
}
