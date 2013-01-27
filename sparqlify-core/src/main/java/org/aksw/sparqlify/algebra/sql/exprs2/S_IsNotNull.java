package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public class S_IsNotNull
	extends SqlExpr1
{
	public S_IsNotNull(SqlExpr arg) {
		super(TypeToken.Boolean, "isNotNull", arg);
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print("isNotNull");
		writeArgs(writer);
	}

	@Override
	public S_IsNotNull copy(SqlExpr arg) {
		S_IsNotNull result = new S_IsNotNull(arg);
		return result;
	}
	
	
	
	
	public static S_IsNotNull create(SqlExpr a) {
		return new S_IsNotNull(a);
	}

	
	
}
