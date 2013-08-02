package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.apache.jena.atlas.io.IndentedWriter;


public class S_Concat
	extends SqlExprN
{	
	public S_Concat(List<SqlExpr> args) {
		super(TypeToken.String, "concat", args);
	}

	public static S_Concat create(SqlExpr... args) {
		return create(Arrays.asList(args));
	}
	
	public static S_Concat create(List<SqlExpr> args) {
		return new S_Concat(args);
	}

	@Override
	public S_Concat copy(List<SqlExpr> args) {
		S_Concat result = new S_Concat(args);
		return result;
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print("Concat");
		writeArgs(writer);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
