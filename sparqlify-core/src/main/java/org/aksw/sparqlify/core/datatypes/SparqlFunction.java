package org.aksw.sparqlify.core.datatypes;

import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;

import com.hp.hpl.jena.sparql.expr.ExprFunction;

public interface SparqlFunction {
	
	/**
	 *  
	 * @return The name of the function
	 */
	String getName();
	
	/**
	 * 
	 * 
	 * @return An object that can evaluate the function for given arguments. NULL if no evaluator exists.
	 */
	ExprFunction getEvaluator();
	
	/**
	 * 
	 * 
	 * @return The set of SQL methods registered for this Sparql Function
	 */
	Collection<XMethod> getSqlMethods();
	
	
	/**
	 * 
	 * 
	 * @param argTypes
	 * @return The SQL function registered with this SPARQL function that matches the given arguments best.
	 */
	SqlMethodCandidate lookup(List<TypeToken> argTypes);
}
