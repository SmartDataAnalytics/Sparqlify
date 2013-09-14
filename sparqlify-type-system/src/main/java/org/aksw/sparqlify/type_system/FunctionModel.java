package org.aksw.sparqlify.type_system;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FunctionModel<T> {
	//public void registerFunction(String id, String name, MethodSignature<T> signature) {
	MethodEntry<T> lookupById(String fnId);

	Collection<CandidateMethod<T>> lookup(Collection<MethodEntry<T>> candidates, List<T> argTypes);
	Collection<CandidateMethod<T>> lookupByName(String functionName, List<T> argTypes);
	
	Collection<String> getIdsByName(String name); 
	String getNameById(String id);
	

	Collection<MethodEntry<T>> getMethodEntries();
	
	MethodEntry<T> registerFunction(MethodDeclaration<T> declaration);
	void registerCoercion(MethodDeclaration<T> declaration);
	
	
	// TODO Potentially deprecate the following methods - the MethodDeclaration class reduces duplication and may thus be better suited
	// But let's first collect some experience and see how this turns out
	@Deprecated
	MethodEntry<T> registerFunction(String id, String name, MethodSignature<T> signature);
	
	@Deprecated
	void registerCoercion(String id, String name, MethodSignature<T> signature);
	
	Map<String, String> getInverses();
	//Map<String, Map<String, String>> getTags();
}