package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;




public class SqlExprN extends SqlExprBase {
	private List<SqlExpr> exprs;

	public SqlExprN(List<SqlExpr> exprs, SqlDatatype datatype) {
		super(datatype);
		this.exprs = exprs;
	}

	@Override
	public List<SqlExpr> getArgs() {
		return exprs;
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