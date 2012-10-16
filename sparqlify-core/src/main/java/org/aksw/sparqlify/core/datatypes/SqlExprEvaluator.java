package org.aksw.sparqlify.core.datatypes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

public interface SqlExprEvaluator {
	SqlExpr eval(List<SqlExpr> args);
}
