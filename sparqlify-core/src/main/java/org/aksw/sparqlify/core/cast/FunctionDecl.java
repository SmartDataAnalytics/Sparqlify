package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.core.datatypes.Invocable;

public interface FunctionDecl {
	
	String getName();
	
	MethodSignature<String> getSignature();
	
	//String getSerializer();
	
	/**
	 * 
	 * 
	 * @return The object implementing the method. Null if there is no implementation.
	 */
	Invocable getInvocable();
}
