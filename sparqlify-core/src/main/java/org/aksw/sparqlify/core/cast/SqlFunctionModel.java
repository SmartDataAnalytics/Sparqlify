package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


/**
 * T is the type
 * I some implementation of a function
 * 
 * @author raven
 *
 * @param <T>
 * @param <I>
 */
public class SqlFunctionModel<T, I> {

	private static final Logger logger = LoggerFactory.getLogger(SqlFunctionModel.class);
	
	private Multimap<String, MethodEntry<T, I>> nameToMethodEntry = ArrayListMultimap.create();
	private DirectSuperTypeProvider<T> typeHierarchyProvider; // = new TypeHierarchyProviderImpl(typeHierarchy);
	
	private Multimap<T, MethodEntry<T, I>> sourceToTargets = ArrayListMultimap.create();
	
	
	
	
	public void lookupCoercionRec(T argType, T targetType, int depth, Set<CandidateMethod<T, I>> result) {
		
		
		Collection<MethodEntry<T, I>> targets = sourceToTargets.get(argType);
		if(targets != null) {
			
			
			boolean found = false;
			for(MethodEntry<T, I> target : targets) {
				T tt = target.getSignature().getReturnType();
				
				Integer distance = TypeHierarchyUtils.getDistance(targetType, tt, typeHierarchyProvider);
				if(distance != null) {
					
					MethodDistance md = new MethodDistance(distance, depth);
					CandidateMethod<T, I> candidate = new CandidateMethod<T, I>(target, null, md);
					
					result.add(candidate);
					found = true;
				}
				
			}
			
			if(found) {
				return;
			}			
		}
		

		Collection<T> superTypes = typeHierarchyProvider.getDirectSuperTypes(argType);
		for(T superType : superTypes) {
			lookupCoercionRec(superType, targetType, depth + 1, result);
		}
		
		
	}
	
	public Set<CandidateMethod<T, I>> lookupCoercions(T argType, T targetType) {

		Set<CandidateMethod<T, I>> result = new HashSet<CandidateMethod<T, I>>();
		

		lookupCoercionRec(argType, targetType, 0, result);

		return result;
	}
	
	public CandidateMethod<T, I> lookupCoercion(T argType, T targetType) {
		Set<CandidateMethod<T, I>> tmp = lookupCoercions(argType, targetType);
		
		CandidateMethod<T, I> result;
		
		if(tmp.size() != 1) {
			result = null;
		} else {
			result = tmp.iterator().next();
		}
		
		return result;
	}
	

	public void registerFunction(String name, MethodSignature<T> signature, I impl) {
		Collection<MethodEntry<T, I>> signatures = nameToMethodEntry.get(name);
		
		// TODO More thorough checks on the type hierarchy...
		if(signatures.contains(name)) {
			throw new RuntimeException("Function " + name + " with signature " + signature + " already registered");
		}
		
		MethodEntry<T, I> entry = new MethodEntry<T, I>(name, signature, impl);
		
		signatures.add(entry);
	}
	
	
	public void registerCoercion(String name, MethodSignature<T> signature, I impl) {
		
	}
	
	
		
	
	public Collection<CandidateMethod<T, I>> lookup(String functionName, List<T> argTypes) {

		
		Collection<MethodEntry<T, I>> signatures = nameToMethodEntry.get(functionName);

		
		Collection<CandidateMethod<T, I>> result = lookup(signatures, argTypes);
		
		return result;
	}
	
	public Collection<CandidateMethod<T, I>> lookup(Collection<MethodEntry<T, I>> candidates, List<T> argTypes) {
		
		
		// Check if there is an appropriate signature registered
		List<CandidateMethod<T, I>> result = new ArrayList<CandidateMethod<T, I>>(); 

		for(MethodEntry<T, I> candidate : candidates) {	
			 
			MethodSignature<T> signature = candidate.getSignature();
			
			if(signature.getParameterTypes().size() > argTypes.size()) {
				continue; // Not enough arguments provided
			}
			
			
			if(!signature.isVararg() && signature.getParameterTypes().size() < argTypes.size()) {
				continue; // Too many arguments provided
			}
			
			int n = Math.min(argTypes.size(), signature.getParameterTypes().size());
			
			boolean isCandidate = true;
			
			List<ParamDistance> distances = new ArrayList<ParamDistance>(argTypes.size());
			List<MethodEntry<T, I>> coercions = new ArrayList<MethodEntry<T, I>>();
			
			for(int i = 0; i < n ; ++i) {
				T argType = argTypes.get(i);
				T paramType = signature.getParameterTypes().get(i);
				
				
				boolean usesCoercion = false;
				Integer distance = TypeHierarchyUtils.getDistance(argType, paramType, typeHierarchyProvider);

				
				// Try with coercion instead
				if(distance == null) {
					CandidateMethod<T, I> coercion = lookupCoercion(argType, paramType);
					
					if(coercion == null) {
						isCandidate = false;
					}
					
					distance = coercion.getDistance().getArgTypeDistances().get(0).getDistance();
					usesCoercion = true;
				}
				
				ParamDistance dist = new ParamDistance(distance, usesCoercion);
				distances.add(dist);
				
				if(!isCandidate) {
					break;
				}
			}
			
			if(isCandidate) {

				MethodDistance distance = new MethodDistance(new ParamDistance(0, false), distances);
				CandidateMethod<T, I> tmp = new CandidateMethod<T, I>(candidate, coercions, distance);
				
				result.add(tmp);
			}
		}
		
		return result;
	}
	
	
/*
	public ExprFunction pickCandidate(Collection<RegisteredFunction> candidates, List<Expr> args) {
		switch(candidates.size()) {
		case 0: {
			logger.warn("Returning false; although it should be type-error");
			//return new SqlExprValue(false);
			return null;
		}
		case 1: {
			RegisteredFunction regFn = candidates.iterator().next(); 
			ExprFunction result = new E_SqlFunctionRegistered(regFn, args);
			return result;
			//return new SqlStringTransformerRegisteredFunction(resultFn);
			//return new S_Function(this.sqlFunctionName, args, pair.getKey().getReturnType(), pair.getValue());	
		}
		default: {
			logger.warn("Multiple overloads matched: " + candidates);
			logger.warn("Returning false; although it should be type-error");
			//return new SqlExprValue(false);
			return null;
		}
		}
	}
*/
}
