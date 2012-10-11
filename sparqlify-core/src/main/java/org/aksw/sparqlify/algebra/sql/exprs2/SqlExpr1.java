package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;

public abstract class SqlExpr1
	extends SqlExprFunctionBase
{
	private SqlExpr expr;
	
	public SqlExpr1(TypeToken datatype, String name, SqlExpr expr) {
		super(datatype, name);
		this.expr = expr;
	}

	public SqlExpr getExpr() {
		return expr;
	}
	
	public List<SqlExpr> getArgs() {
		return Arrays.asList(expr);
	}
	
	public SqlExprType getType() {
		return SqlExprType.Function;
	}

	public void writeArgs(IndentedWriter writer) {
		writer.incIndent();
		expr.asString(writer);
		writer.decIndent();
	}
	/*
	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName(), expr);
	}*/
}
