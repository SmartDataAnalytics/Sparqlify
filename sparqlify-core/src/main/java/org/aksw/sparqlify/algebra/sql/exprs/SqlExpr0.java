package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.Collections;
import java.util.List;

import org.aksw.sparqlify.core.SqlDatatype;

public abstract class SqlExpr0
	extends SqlExprBase
{
	public SqlExpr0(SqlDatatype datatype) {
		super(datatype);
	}

	public List<SqlExpr> getArgs() {
		return Collections.emptyList();
	}

	
	/*
	@Override
	public String toString() {
		return SqlExprBase.asString(this.getClass().getSimpleName());
	}
	*/
}
