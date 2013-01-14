package org.aksw.sparqlify.core.algorithms;

import java.util.Map;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;

/**
 * Implementations of this class transform expressions as to eliminate
 * all RDF term expressions.
 * 
 * TODO: This statement does not really hold: "At this stage datatypes do not have
 * to be considered." We still need to check at least on rdf term type level,
 * i.e. whether we are comparing e.g. uris to plain literal.
 * 
 * Example:
 * <http://ex.org> = 5^^xsd:integer
 * (1, http://ex.org, "", "") = (3, 5, "", "xsd:integer)
 * -&gt; 1 != 5 -&gt; type error 
 * 
 * 
 * TODO Below description is outdated
 * Evaluates an expression against a given binding (= variable assignment).
 * Unless all variables are bound, the result is not required to be a literal
 * value. In general, the result may be an expression that was derived from
 * the original one by means of transformations.
 * 
 * 
 * @author raven
 *
 */
public interface ExprEvaluator {

	Expr transform(Expr expr);
	
	@Deprecated
	Expr eval(Expr expr, Map<Var, Expr> binding);//VarBinding binding);
}
