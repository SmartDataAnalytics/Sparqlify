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

	@Override
	public SqlExprFunction copy(List<SqlExpr> args) {
		if(args.size() != 1) {
			throw new RuntimeException("Exactly 1 argument expected, got: " + args);
		}
		
		SqlExprFunction result = copy(args.get(0));
		return result;
	}
	
	public abstract SqlExprFunction copy(SqlExpr arg);

	public void writeArgs(IndentedWriter writer) {
		writer.incIndent();
		expr.asString(writer);
		writer.decIndent();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SqlExpr1 other = (SqlExpr1) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}

	/*
	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName(), expr);
	}*/
}
