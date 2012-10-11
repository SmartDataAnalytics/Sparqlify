package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.Collections;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;

public abstract class SqlExpr0
	extends SqlExprBase
{
	public SqlExpr0(TypeToken datatype) {
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
