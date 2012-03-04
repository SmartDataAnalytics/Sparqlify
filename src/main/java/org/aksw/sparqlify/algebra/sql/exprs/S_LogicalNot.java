package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;

public class S_LogicalNot
	extends SqlExpr1
{

	public S_LogicalNot(SqlExpr expr) {
		super(expr, DatatypeSystemDefault._BOOLEAN);
	}

}
