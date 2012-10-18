package org.aksw.sparqlify.algebra.sql.exprs2;

import java.util.List;

public interface SqlExprFunction
	extends SqlExpr
{
	String getName();
	
	SqlExprFunction copy(List<SqlExpr> args);
}
