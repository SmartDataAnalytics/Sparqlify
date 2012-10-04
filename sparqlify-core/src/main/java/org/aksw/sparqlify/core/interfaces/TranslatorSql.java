package org.aksw.sparqlify.core.interfaces;

import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;


public interface TranslatorSql {
	/**
	 * We use the same Expr object for translating expressions.
	 * The difference is, that variables are assumed to correspond
	 * to column names.
	 * This means, that the expr object has to be evaluated with different semantics.
	 * 
	 * @param sparqlExpr
	 * @param binding A set of variable-expr mappings. May be null.
	 * @return
	 */
	Expr translateSql(Expr sparqlExpr, Map<Var, Expr> binding);
}
