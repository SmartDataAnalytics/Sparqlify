package org.aksw.sparqlify.core.interfaces;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;

import com.hp.hpl.jena.sparql.expr.Expr;


public interface TranslatorSql {
	SqlExpr translateSql(Expr sparqlExpr);
}
