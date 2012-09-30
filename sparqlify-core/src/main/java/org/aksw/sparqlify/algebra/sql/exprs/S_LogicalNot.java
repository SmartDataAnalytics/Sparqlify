package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.core.DatatypeSystemDefault;

public class S_LogicalNot
	extends SqlExpr1
{

	public S_LogicalNot(SqlExpr expr) {
		super(expr, DatatypeSystemDefault._BOOLEAN);
	}

}
