package org.aksw.sparqlify.core.cast;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;

public interface FunctionModel<T> {
	//public void registerFunction(String id, String name, MethodSignature<T> signature) {
	MethodEntry<T> lookupById(String fnId);

	Collection<CandidateMethod<T>> lookup(Collection<MethodEntry<T>> candidates, List<T> argTypes);
	Collection<CandidateMethod<T>> lookupByName(String functionName, List<T> argTypes);
	
	Collection<String> getIdsByName(String name); 
	
	void registerFunction(String id, String name, MethodSignature<T> signature);
	void registerCoercion(String id, String name, MethodSignature<T> signature);
	
	Map<String, String> getInverses();
	//Map<String, Map<String, String>> getTags();
}