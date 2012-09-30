package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.List;

import org.aksw.sparqlify.core.SqlDatatype;


public class S_Function extends SqlExprN {
	private String funcName;

	// Serializes the function as an sql string
	private SqlStringTransformer transformer = null;
	
	
	public S_Function(String funcName, List<SqlExpr> exprs, SqlDatatype datatype, SqlStringTransformer transformer) {
		super(exprs, datatype);
		this.funcName = funcName;
		this.transformer = transformer;
	}
	
	public S_Function(String funcName, List<SqlExpr> exprs, SqlDatatype datatype) {
		super(exprs, datatype);
		this.funcName = funcName;
	}

	public String getFuncName() {
		return funcName;
	}

	public SqlStringTransformer getTransformer() {
		return transformer;
	}
	
	/*
	@Override
	public String asSQL() {
		return funcName + "("
				+ Joiner.on(", ").join(SqlExprN.toSqlStrings(getExprs())) + ")";
	}*/

	/*
	@Override
	public String toString() {
		return asSQL();
	}*/
}