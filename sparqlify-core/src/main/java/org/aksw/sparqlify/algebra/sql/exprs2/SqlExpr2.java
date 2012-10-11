package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public abstract class SqlExpr2
	extends SqlExprFunctionBase
{
	protected final SqlExpr left;
	protected final SqlExpr right;

	public SqlExpr2(TypeToken datatype, String name, SqlExpr left, SqlExpr right) {
		super(datatype, name);
		this.left = left;
		this.right = right;
	}

	public SqlExpr getLeft() {
		return left;
	}
	public SqlExpr getRight() {
		return right;
	}
	
	public List<SqlExpr> getArgs() {
		return Arrays.asList(left, right);
	}
	
	public SqlExprType getType() {
		return SqlExprType.Function;
	}

	public void writeArgs(IndentedWriter writer) {
		writer.println("(");
		writer.incIndent();
		left.asString(writer);
		writer.println(", ");
		right.asString(writer);
		writer.decIndent();
		writer.println(")");
	}

	/*
	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName(), left, right);
	}*/
}
