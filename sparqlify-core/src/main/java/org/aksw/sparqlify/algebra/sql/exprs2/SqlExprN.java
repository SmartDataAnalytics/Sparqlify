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