package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Arrays;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;

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

	@Override
	public SqlExprFunction copy(List<SqlExpr> args) {
		if(args.size() != 2) {
			throw new RuntimeException("Exactly 2 arguments expected, got: " + args);
		}
		
		SqlExprFunction result = copy(args.get(0), args.get(1));
		return result;
	}
	
	public abstract SqlExpr2 copy(SqlExpr left, SqlExpr right);
	
//	public void writeArgs(IndentedWriter writer) {
//		writer.println("(");
//		writer.incIndent();
//		left.asString(writer);
//		writer.println(", ");
//		right.asString(writer);
//		writer.decIndent();
//		writer.println(")");
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		SqlExpr2 other = (SqlExpr2) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

//	@Override
//	public void asString(IndentedWriter writer) {
//		writer.print(name);
//		writeArgs(writer);		
//	}

	/*
	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName(), left, right);
	}*/

}
