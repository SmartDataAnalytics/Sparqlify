package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

public interface SqlExprSerializer {
	String serialize(SqlExpr expr);
}
