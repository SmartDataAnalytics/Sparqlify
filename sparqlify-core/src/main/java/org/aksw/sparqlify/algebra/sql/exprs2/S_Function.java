package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.apache.jena.atlas.io.IndentedWriter;


public class S_Function
	extends SqlExprN
{	
	public S_Function(TypeToken typeName, String functionName, List<SqlExpr> args) {
		super(typeName, functionName, args);
	}

	public static S_Function create(TypeToken typeName, String functionName, SqlExpr... args) {
		return create(typeName, functionName, Arrays.asList(args));
	}
	
	public static S_Function create(TypeToken typeName, String functionName, List<SqlExpr> args) {
		return new S_Function(typeName, functionName, args);
	}

	@Override
	public S_Function copy(List<SqlExpr> args) {
		S_Function result = new S_Function(datatype, name, args);
		return result;
	}
	
	@Override
	public void asString(IndentedWriter writer) {
		writer.print(name); //"Concat");
		writeArgs(writer);
	}
	
	@Override
	public <T> T accept(SqlExprVisitor<T> visitor) {
		T result = visitor.visit(this);
		return result;
	}
}
