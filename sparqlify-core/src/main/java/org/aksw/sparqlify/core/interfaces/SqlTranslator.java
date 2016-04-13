package org.aksw.sparqlify.core.interfaces;

import java.util.Map;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.ExprSqlRewrite;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;


/**
 * Interface for translating a SPARQL expression to as SQL expressions
 * via a single binding.
 * 
 * @author raven
 *
 */
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
	
	
	ExprSqlRewrite translate(Expr sparqlExpr, Map<Var, Expr> binding, Map<String, TypeToken> typeMap);
	
	//ExprSqlRewrite translate(Expr sparqlExpr, )
}
