package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;


public abstract class SqlExprConstantBase
	extends SqlExpr0
	implements SqlExprConstant
{
	public SqlExprConstantBase(TypeToken datatype) {
		super(datatype);
	}
	
	@Override
	public SqlExprType getType() {
		return SqlExprType.Constant;
	}
	
	public SqlExprConstant asConstant() {
		return this;
	}
}
