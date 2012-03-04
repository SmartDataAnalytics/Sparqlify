package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;

import com.google.common.base.Joiner;

public class S_Concat extends SqlExprN {

	public S_Concat(List<SqlExpr> exprs) {
		super(exprs, DatatypeSystemDefault._STRING);
	}
	
	@Override
	public String toString() {
		return "concat(" + Joiner.on(", ").join(getArgs()) + ")";
	}
	
	/*
	@Override
	public String asSQL()
	{
		List<String> args = SqlExprN.toSqlStrings(super.getExprs());
		return "concat(" + Joiner.on(", ").join(args) + ")";
	}*/
	
	/*
	@Override
	public toString()
	{
		return asSQL();
	}*/
}