package org.aksw.sparqlify.core.interfaces;

import com.hp.hpl.jena.sparql.expr.Expr;


public interface TranslatorSql {
	/**
	 * We use the same Expr object for translating expressions.
	 * The difference is, that variables are assumed to correspond
	 * to column names.
	 * This means, that the expr object has to be evaluated with different semantics.
	 * 
	 * @param sparqlExpr
	 * @return
	 */
	Expr translateSql(Expr sparqlExpr);
}
