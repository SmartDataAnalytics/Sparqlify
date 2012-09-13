package org.aksw.sparqlify.algebra.sql.exprs;

import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;

public class S_LogicalOr
	extends SqlExpr2
{
	public S_LogicalOr(SqlExpr left, SqlExpr right) {
		super(left, right, DatatypeSystemDefault._BOOLEAN);
	}
}
