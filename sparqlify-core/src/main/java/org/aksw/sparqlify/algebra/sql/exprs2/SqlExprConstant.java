package org.aksw.sparqlify.algebra.sql.exprs2;


public interface SqlExprConstant
	extends SqlExpr
{
	<T> T getValue();
}
