package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.DatatypeSystemDefault;

public class S_IsNotNull
	extends SqlExpr1
{

	public S_IsNotNull(SqlExpr expr) {
		super(expr, DatatypeSystemDefault._BOOLEAN);
	}

}
