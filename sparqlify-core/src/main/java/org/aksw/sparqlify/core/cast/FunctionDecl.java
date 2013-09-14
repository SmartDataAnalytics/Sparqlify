package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.datatypes.Invocable;
import org.aksw.sparqlify.type_system.MethodSignature;

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
