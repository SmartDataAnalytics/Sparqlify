package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;


public class S_UserFunc
	extends SqlExprN
{
	public S_UserFunc(String name, List<SqlExpr> args) {
		super(TypeToken.String, name, args);
	}

	public static S_UserFunc create(String name, SqlExpr... args) {
		return create(name, Arrays.asList(args));
	}
	
	public static S_UserFunc create(String name, List<SqlExpr> args) {
		return new S_UserFunc(name, args);
	}

	@Override
	public void asString(IndentedWriter writer) {
		writer.print("UserFunc [" + getName() + "]");
		writeArgs(writer);
	}
}
