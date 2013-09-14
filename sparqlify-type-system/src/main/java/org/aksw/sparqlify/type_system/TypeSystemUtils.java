package org.aksw.sparqlify.type_system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

public class TypeSystemUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(TypeSystemUtils.class);
	

	public static <T> CandidateMethod<T> lookupSqlCandidate(FunctionModel<T> functionModel, Multimap<String, String> nameToDecls, String sparqlFnName, List<T> argTypes) {
		//FunctionModel<T> functionModel = typeSystem.getSqlFunctionModel();
		
		Collection<String> sqlFnIds = nameToDecls.get(sparqlFnName);//typeSystem.getSparqlSqlImpls().get(sparqlFnName);
		if(sqlFnIds.isEmpty()) {
			logger.debug("No SQL function declarations found for: " + sparqlFnName);
		}
		
		Collection<MethodEntry<T>> sqlFns = new ArrayList<MethodEntry<T>>(sqlFnIds.size());
		for(String sqlFnId : sqlFnIds) {
			MethodEntry<T> sqlFn = functionModel.lookupById(sqlFnId);
			if(sqlFn != null) {
				sqlFns.add(sqlFn);
			}
		}
		

		CandidateMethod<T> result = lookupSqlCandidate(functionModel, sqlFns, argTypes, "SPARQL function " + sparqlFnName);
		return result;
	}

	public static <T> CandidateMethod<T> lookupSqlCandidate(FunctionModel<T> functionModel, MethodEntry<T> sqlFn, List<T> argTypes, String collectionLabel)
	{
		Collection<MethodEntry<T>> sqlFns = Collections.singleton(sqlFn);
		CandidateMethod<T> result = lookupSqlCandidate(functionModel, sqlFns, argTypes, collectionLabel);
		return result;
	}
	

	public static <T> CandidateMethod<T> lookupCandidate(FunctionModel<T> functionModel, String name, List<T> argTypes)
	{
		Collection<CandidateMethod<T>> candidates = functionModel.lookupByName(name, argTypes);
		
		CandidateMethod<T> result = rejectMultipleElements(candidates, "Multiple candidates for " + name + " with argument types " + argTypes);
		
		return result;
	}

	public static <T> T rejectMultipleElements(Collection<T> candidates, String exceptionMsg) {
		T result;
		if(candidates.size() > 1) {
			throw new RuntimeException(exceptionMsg + ": " + candidates);
		} else if(candidates.isEmpty()) {
			result = null;
		} else {
			result = candidates.iterator().next();
		}
		
		return result;
	}
	
	public static <T> CandidateMethod<T> lookupSqlCandidate(FunctionModel<T> functionModel, Collection<MethodEntry<T>> sqlFns, List<T> argTypes, String collectionLabel)
	{
		Collection<CandidateMethod<T>> candidates = functionModel.lookup(sqlFns, argTypes);
		
		CandidateMethod<T> result = rejectMultipleElements(candidates, "Multiple candidates for " + collectionLabel + " with argument types " + argTypes);
		
		return result;
	}

}
