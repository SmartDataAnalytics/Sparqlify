package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;


public abstract class SqlExprFunctionBase
	extends SqlExprBase
	implements SqlExprFunction
{
	private String name;
	
	public SqlExprFunctionBase(TypeToken datatype, String name) {
		super(datatype);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public SqlExprType getType() {
		return SqlExprType.Function;
	}
	
	public SqlExprFunction asFunction() {
		return this;
	}
}