package org.aksw.sparqlify.core.cast;

import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;

public interface FunctionModel<T> {
	//public void registerFunction(String id, String name, MethodSignature<T> signature) {
	MethodEntry<T> lookupById(String fnId);

	Collection<CandidateMethod<T>> lookup(Collection<MethodEntry<T>> candidates, List<T> argTypes);
	Collection<CandidateMethod<T>> lookupByName(String functionName, List<T> argTypes);
	void registerFunction(String id, String name, MethodSignature<T> signature);
	void registerCoercion(String id, String name, MethodSignature<T> signature);
	
	
}