package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.openjena.atlas.io.IndentedWriter;




public abstract class SqlExprN
	extends SqlExprFunctionBase
{
	protected List<SqlExpr> args;

	public SqlExprN(TypeToken datatype, String name, List<SqlExpr> exprs) {
		super(datatype, name);
		this.args = exprs;
	}

	@Override
	public List<SqlExpr> getArgs() {
		return args;
	}
	
	public SqlExprType getType() {
		return SqlExprType.Function;
	}

	//@Override
	public void writeArgs(IndentedWriter writer) {
		writer.print(" (");
		writer.incIndent();
		boolean isFirst = true;
		for(SqlExpr arg : args) {
			if(isFirst) {
				isFirst = false;
				writer.println();
			} else {
				writer.println(", ");
			}
			
			arg.asString(writer);
		}
		writer.println();
		writer.decIndent();
		writer.print(")");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((args == null) ? 0 : args.hashCode());
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
		SqlExprN other = (SqlExprN) obj;
		if (args == null) {
			if (other.args != null)
				return false;
		} else if (!args.equals(other.args))
			return false;
		return true;
	}


	/*
	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName(), exprs);
	}*/

	/*
	public static List<String> toSqlStrings(Iterable<SqlExpr> exprs) {
		List<String> result = new ArrayList<String>();
		for (SqlExpr expr : exprs) {
			result.add(expr.asSQL());
		}
		return result;
	}*/
	
	/*
	public void visit(SqlExprVisitor visitor) {
		// visitor.visit(this) ;
	}*/
	
	
}