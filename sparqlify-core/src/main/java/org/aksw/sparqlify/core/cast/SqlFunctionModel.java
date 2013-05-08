package org.aksw.sparqlify.core.cast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.core.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Return the set of SQL function declarations for a given sparql function id
 * 
 * @author raven
 *
 * @param <T>
 * @param <I>
 */
interface SparqlSqlFunctionMap<T> {
	Collection<MethodEntry<T>> getSqlImpls(String id);
}




/**
 * T is the type
 * I some implementation of a function
 * 
 * @author raven
 *
 * @param <T>
 * @param <I>
 */
public class SqlFunctionModel<T> {

	private static final Logger logger = LoggerFactory.getLogger(SqlFunctionModel.class);
	
	private Multimap<String, MethodEntry<T>> nameToMethodEntry = ArrayListMultimap.create();
	private Map<String, MethodEntry<T>> idToMethodEntry = new HashMap<String, MethodEntry<T>>();

	private DirectSuperTypeProvider<T> typeHierarchyProvider; // = new TypeHierarchyProviderImpl(typeHierarchy);
	
	private Multimap<T, MethodEntry<T>> sourceToTargets = ArrayListMultimap.create();
	
	
	public SqlFunctionModel(DirectSuperTypeProvider<T> typeHierarchyProvider) {
		this.typeHierarchyProvider = typeHierarchyProvider;
	}
	

	public static void main(String[] args) throws IOException {
		
		Map<String, String> typeHierarchy = MapReader
				.readFromResource("/type-hierarchy.default.tsv");

		Map<String, String> typeMap = MapReader
				.readFromResource("/type-map.h2.tsv");

		// TODO HACK Do not add types programmatically 
		typeMap.put("INTEGER", "int");
		
		typeHierarchy.putAll(typeMap);
		
		
	
		IBiSetMultimap<TypeToken, TypeToken> h = TypeSystemImpl.createHierarchyMap(typeHierarchy);
		TypeHierarchyProviderImpl thp = new TypeHierarchyProviderImpl(h);

		SqlFunctionModel<TypeToken> model = new SqlFunctionModel<TypeToken>(thp);
		
		model.registerFunction("plus_int", "+", MethodSignature.create(false, TypeToken.Int, TypeToken.Int, TypeToken.Int));
		//model.registerCoercion("to_int", "to_int", MethodSignature.create(false, TypeToken.Double, TypeToken.Int));
		model.registerCoercion("to_int", "to_int", MethodSignature.create(false, TypeToken.Int, TypeToken.Double));

		
		TypeToken Geometry = TypeToken.alloc("geometry");
		TypeToken Geography = TypeToken.alloc("geography");

		model.registerFunction("st_intersects_geometry", "st_intersects", MethodSignature.create(false, Geometry, Geometry));
		model.registerFunction("st_intersects_geography", "st_intersects", MethodSignature.create(false, Geography, Geography));

		{
			Collection<CandidateMethod<TypeToken>> cands = model.lookupByName("+", Arrays.asList(Geometry, Geometry));
			System.out.println("Number of candidates: " + cands.size());
			System.out.println(cands);
		}
		
		
	}
	
	
	public void lookupCoercionRec(T argType, T targetType, int depth, Set<CandidateMethod<T>> result) {
		
		
		Collection<MethodEntry<T>> targets = sourceToTargets.get(argType);
		if(targets != null) {
			
			
			boolean found = false;
			for(MethodEntry<T> target : targets) {
				T tt = target.getSignature().getReturnType();
				
				Integer distance = TypeHierarchyUtils.getDistance(targetType, tt, typeHierarchyProvider);
				if(distance != null) {
					
					MethodDistance md = new MethodDistance(distance, depth);
					CandidateMethod<T> candidate = new CandidateMethod<T>(target, null, md);
					
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
	
	public Set<CandidateMethod<T>> lookupCoercions(T argType, T targetType) {

		Set<CandidateMethod<T>> result = new HashSet<CandidateMethod<T>>();
		

		lookupCoercionRec(argType, targetType, 0, result);

		return result;
	}
	
	public CandidateMethod<T> lookupCoercion(T argType, T targetType) {
		Set<CandidateMethod<T>> tmp = lookupCoercions(argType, targetType);
		
		CandidateMethod<T> result;
		
		if(tmp.size() != 1) {
			result = null;
		} else {
			result = tmp.iterator().next();
		}
		
		return result;
	}
	

	public void registerFunction(String id, String name, MethodSignature<T> signature) {
		Collection<MethodEntry<T>> signatures = nameToMethodEntry.get(name);
		
		// TODO More thorough checks on the type hierarchy...
		if(signatures.contains(name)) {
			throw new RuntimeException("Function " + name + " with signature " + signature + " already registered");
		}
		
		MethodEntry<T> entry = new MethodEntry<T>(id, name, signature);
		
		signatures.add(entry);
	}
	
	
	public void registerCoercion(String id, String name, MethodSignature<T> signature) {
		List<T> paramTypes = signature.getParameterTypes();
		if(paramTypes.size() != 1) {
			throw new RuntimeException("Coercions must only have 1 paramater");
		}
		
		T sourceType = paramTypes.get(0);
		Collection<MethodEntry<T>> targets = sourceToTargets.get(sourceType);

		MethodEntry<T> entry = new MethodEntry<T>(id, name, signature);
		
		targets.add(entry);
	}
	
	
	public MethodEntry<T> lookupById(String id) {
		MethodEntry<T> result = idToMethodEntry.get(id);
		return result;
	}
	

	//public Collection<CandidateMethod<T>> lookupByName(String functionName, T ... argType) {
	
	
	public Collection<CandidateMethod<T>> lookupByName(String functionName, List<T> argTypes) {

		
		Collection<MethodEntry<T>> signatures = nameToMethodEntry.get(functionName);

		
		Collection<CandidateMethod<T>> result = lookup(signatures, argTypes);
		
		return result;
	}
	
	public Collection<CandidateMethod<T>> lookup(Collection<MethodEntry<T>> candidates, List<T> argTypes) {
		
		
		// Check if there is an appropriate signature registered
		List<CandidateMethod<T>> result = new ArrayList<CandidateMethod<T>>(); 

		for(MethodEntry<T> candidate : candidates) {	
			 
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
			List<CandidateMethod<T>> coercions = new ArrayList<CandidateMethod<T>>();
			
			for(int i = 0; i < n ; ++i) {
				T argType = argTypes.get(i);
				T paramType = signature.getParameterTypes().get(i);
				
				
				boolean usesCoercion = false;
				Integer distance = TypeHierarchyUtils.getDistance(argType, paramType, typeHierarchyProvider);

				
				// Try with coercion instead
				CandidateMethod<T> coercion = null;
				if(distance == null) {
					coercion = lookupCoercion(argType, paramType);
					
					if(coercion == null) {
						isCandidate = false;
					} else {
						distance = coercion.getDistance().getArgTypeDistances().get(0).getDistance();
						usesCoercion = true;
					}
				}

				coercions.add(coercion);
				
				ParamDistance dist = new ParamDistance(distance, usesCoercion);
				distances.add(dist);
				
				if(!isCandidate) {
					break;
				}
			}

			if(isCandidate) {

				MethodDistance distance = new MethodDistance(new ParamDistance(0, false), distances);
				CandidateMethod<T> tmp = new CandidateMethod<T>(candidate, coercions, distance);
				
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
