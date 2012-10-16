package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.TypeToken;


public abstract class SqlExprVarBase
	extends SqlExpr0
	implements SqlExprVar
{
	public SqlExprVarBase(TypeToken datatype) {
		super(datatype);
	}

	@Override
	public SqlExprType getType() {
		return SqlExprType.Variable;
	}	
}
