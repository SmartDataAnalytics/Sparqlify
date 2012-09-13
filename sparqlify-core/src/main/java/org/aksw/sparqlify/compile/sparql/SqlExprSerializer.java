package org.aksw.sparqlify.compile.sparql;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;

public interface SqlExprSerializer
{
	String serialize(SqlExpr expr);
}