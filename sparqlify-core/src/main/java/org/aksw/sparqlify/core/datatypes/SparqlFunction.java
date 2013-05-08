package org.aksw.sparqlify.core.datatypes;

import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.ExprSubstitutor;


/**
 * TODO Do we need type expressions? 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
interface TypeExpr {
	
}

class T_Primitive {
	private String uri;
}




public interface SparqlFunction {
	
	/**
	 *  
	 * @return The name of the function
	 */
	String getName();
	
	
	/**
	 * Sparql Functions can have a signature:
	 * object -> http://sparqlify.org/vocab/type/Object (anything)
	 * plainLiteral -> http://sparqlify.org/vocab/type/PlainLitera
	 * 
	 * Note this signature refers to RDF types!!! - NOT SQL ONES!
	 * 
	 * nonRdfTermObject -> http://sparqlify.org/vocab/type/NrtObject (excludes URIs, blanknodes and literals)
	 * 
	 * @return
	 */
	MethodSignature<TypeToken> getSignature();
	
	
	/**
	 * An object that may evaluate the result of a SPARQL function
	 * based on its (raw) arguments (Expr objects)
	 * 
	 * Used for reusing Jena's Expr.eval(...) methods.
	 * 
	 * @return An object that can evaluate the function for given arguments. NULL if no evaluator exists.
	 */
	SqlExprEvaluator getEvaluator();

	
	/**
	 * A substitutor may yield a new,
	 * rewritten, expression based on the given argument types.
	 * 
	 * Used to specify transformation to SQL functions.
	 * E.g. ogc:intersects(ogc:geometry a, ogc:geometry b, dist) ->
	 *     plainLiteral(ST_INTERSECTS(a, b 1000 * dist))
	 * 
	 * . If both arguments to ogc:intersects are constants, we could 
	 *   compute the result on the Java level (provided we registered such evaluator).
	 * . We could also register a transformation that eventually uses ST_INTERSECTS to archieve this goal.
	 * . ISSUE What is the role of the signature then?
	 * 
	 * Essentially I want to be able to state, that some (SPARQL?) function
	 * is capable of converting between e.g. integer and double.
	 * This could be done with
	 * typeSystem.registerCoercion("src", "dst", new CoercionSparqlFunction(typeSystem, "myFunc"));
	 *
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * If a function invocation (e.g. using E_Function or E_Call) cannot
	 * be evaluated, nor rewritten, it is assumed that the arguments
	 * are invalid, and a type error is raised.
	 * 
	 * 
	 * 
	 * @return
	 */
	ExprSubstitutor getSubstitutor();
	

	
	
	
	/**
	 * 
	 * 
	 * @return The set of SQL methods registered for this Sparql Function. Should be consistent with an evaluator.
	 */
	@Deprecated
	Collection<XMethod> getSqlMethods();
	
	
	/**
	 * 
	 * 
	 * @param argTypes
	 * @return The SQL function registered with this SPARQL function that matches the given arguments best.
	 */
	@Deprecated
	SqlMethodCandidate lookup(List<TypeToken> argTypes);
}
