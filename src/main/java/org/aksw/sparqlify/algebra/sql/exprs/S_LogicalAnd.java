package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;

public class S_LogicalAnd
	extends SqlExpr2
{
	public S_LogicalAnd(SqlExpr left, SqlExpr right) {
		super(left, right, DatatypeSystemDefault._BOOLEAN);
	}
}
