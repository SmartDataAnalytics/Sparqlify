package org.aksw.sparqlify.core.interfaces;

import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;


public interface SqlTranslator {
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
	// TODO Once the new method is known to work, remove this method, and clean up all things that break because of this.
	//Expr translateSql(Expr sparqlExpr, Map<Var, Expr> binding);
	
	
	SqlExpr translate(Expr sparqlExpr, Map<Var, Expr> binding, Map<String, TypeToken> typeMap);
}
