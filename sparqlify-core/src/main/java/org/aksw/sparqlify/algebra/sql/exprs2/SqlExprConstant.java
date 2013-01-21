package org.aksw.sparqlify.algebra.sql.exprs2;

import org.aksw.sparqlify.core.cast.SqlValue;


public interface SqlExprConstant
	extends SqlExpr
{
	//NodeValue getValue();
	SqlValue getValue();
	//<T> T getValue();
}
