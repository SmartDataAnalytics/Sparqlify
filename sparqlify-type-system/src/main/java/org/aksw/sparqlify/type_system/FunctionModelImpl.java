package org.aksw.sparqlify.type_system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Return the set of SQL function declarations for a given sparql function id
 * 
 * @author raven
 *
 * @param <T>
 * @param <I>
 */
//interface SparqlSqlFunctionMap<T> {
//	Collection<MethodEntry<T>> getSqlImpls(String id);
//}



/**
 * T is the type
 * I some implementation of a function
 * 
 * @author raven
 *
 * @param <T>
 * @param <I>
 */
public class FunctionModelImpl<T>
	implements FunctionModel<T>
{

	//private static final Logger logger = LoggerFactory.getLogger(FunctionModel.class);
	
	//private Multimap<String, MethodEntry<T>> nameToMethodEntry = ArrayListMultimap.create();
	private Map<String, MethodEntry<T>> idToMethodEntry = new HashMap<String, MethodEntry<T>>();

	private Multimap<String, String> nameToIds = HashMultimap.create();
	
	private DirectSuperTypeProvider<T> typeHierarchyProvider; // = new TypeHierarchyProviderImpl(typeHierarchy);
	
	
	// This is the map for coercion functions
	private Multimap<T, MethodEntry<T>> sourceToTargets = ArrayListMultimap.create();

	
	// This maps symbols to inverse functions
	private Map<String, String> inverses = new HashMap<String, String>(); 
	
	public Map<String, String> getInverses() {
		return inverses;
	}
	
	
	public static <K, V> K getFirstKey(Multimap<K, V> mmap, Object key) {
		K result = null;

		for(Entry<K, V> entry : mmap.entries()) {
			V k = entry.getValue();
			K v = entry.getKey();
			if(key.equals(k)) {
				result = v;
				break;
			}
		}
		
		return result;
		
	}

	@Override
	public String getNameById(String id) {
		// TODO Optimize this lookup!
		String result = getFirstKey(nameToIds, id);
		
		return result;
	}

	
	public Collection<MethodEntry<T>> getMethodEntries() {
		return idToMethodEntry.values();
	}

//	Map<String, Map<String, String>> tags = new HashMap<String, Map<String, String>>();
//	
//	public Map<String, Map<String, String>> getTags() {
//		return tags;
//	}
	
	
	
	public FunctionModelImpl(DirectSuperTypeProvider<T> typeHierarchyProvider) {
		this.typeHierarchyProvider = typeHierarchyProvider;
	}

	@Override
	public Collection<String> getIdsByName(String name) 
	{
		Collection<String> result = nameToIds.get(name);
		return result;
	}

//	public static void main(String[] args) throws IOException {
//		
//		Map<String, String> typeHierarchy = MapReader
//				.readFromResource("/type-hierarchy.default.tsv");
//
//		Map<String, String> typeMap = MapReader
//				.readFromResource("/type-map.h2.tsv");
//
//		// TODO HACK Do not add types programmatically 
//		typeMap.put("INTEGER", "int");
//		
//		typeHierarchy.putAll(typeMap);
//		
//		
//	
//		IBiSetMultimap<TypeToken, TypeToken> h = TypeSystemImpl.createHierarchyMap(typeHierarchy);
//		TypeHierarchyProviderImpl thp = new TypeHierarchyProviderImpl(h);
//
//		FunctionModel<TypeToken> model = new FunctionModelImpl<TypeToken>(thp);
//		
//		model.registerFunction("plus_float", "+", MethodSignature.create(false, TypeToken.Float, TypeToken.Float, TypeToken.Float));
//		//model.registerCoercion("to_int", "to_int", MethodSignature.create(false, TypeToken.Double, TypeToken.Int));
//		model.registerCoercion("to_float", "to_float", MethodSignature.create(false, TypeToken.Float, TypeToken.Int));
//
//		
//		TypeToken Geometry = TypeToken.alloc("geometry");
//		TypeToken Geography = TypeToken.alloc("geography");
//
//		model.registerFunction("st_intersects_geometry", "st_intersects", MethodSignature.create(false, TypeToken.Boolean, Geometry, Geometry));
//		model.registerFunction("st_intersects_geography", "st_intersects", MethodSignature.create(false, TypeToken.Boolean, Geography, Geography));
//
//		{
//			Collection<CandidateMethod<TypeToken>> cands = model.lookupByName("st_intersects", Arrays.asList(Geometry, Geometry));
//			System.out.println("Number of candidates: " + cands.size());
//			System.out.println(cands);
//		}
//
//		{
//			Collection<CandidateMethod<TypeToken>> cands = model.lookupByName("+", Arrays.asList(TypeToken.Int, TypeToken.Int));
//			System.out.println("Number of candidates: " + cands.size());
//			System.out.println(cands);
//		}
//		
//
//		
//	}
	
	
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
	

	@Deprecated
	public MethodEntry<T> registerFunction(String id, String name, MethodSignature<T> signature) {
		//Collection<MethodEntry<T>> signatures = nameToMethodEntry.get(name);
		
		
		// TODO More thorough checks on the type hierarchy...
//		if(signatures.contains(name)) {
//			throw new RuntimeException("Function " + name + " with signature " + signature + " already registered");
//		}
		
		MethodEntry<T> entry = new MethodEntry<T>(id, MethodDeclaration.create(name, signature));

		idToMethodEntry.put(id,  entry);
		
		//signatures.add(entry);
		
		nameToIds.put(name, id);
		
		return entry;
	}
	
	
	@Deprecated
	public void registerCoercion(String id, String name, MethodSignature<T> signature) {
		List<T> paramTypes = signature.getParameterTypes();
		if(paramTypes.size() != 1) {
			throw new RuntimeException("Coercions must only have 1 paramater");
		}
		
		T sourceType = paramTypes.get(0);
		Collection<MethodEntry<T>> targets = sourceToTargets.get(sourceType);

		MethodEntry<T> entry = new MethodEntry<T>(id, MethodDeclaration.create(name, signature));
		
		targets.add(entry);
		
		idToMethodEntry.put(id, entry);
	}
	
	
	public MethodEntry<T> lookupById(String id) {
		MethodEntry<T> result = idToMethodEntry.get(id);
		return result;
	}
	

	//public Collection<CandidateMethod<T>> lookupByName(String functionName, T ... argType) {
	
	public Collection<MethodEntry<T>> lookupByName(String name) {
		List<MethodEntry<T>> result = new ArrayList<MethodEntry<T>>();
		Collection<String> ids = nameToIds.get(name);
		
		for(String id : ids) {
			MethodEntry<T> method = idToMethodEntry.get(id);
			
			if(method != null) {
				result.add(method);
			}
		}
		
		return result;
	}
	
	public Collection<CandidateMethod<T>> lookupByName(String functionName, List<T> argTypes) {

		
		Collection<MethodEntry<T>> signatures = lookupByName(functionName);

		
		Collection<CandidateMethod<T>> result = lookup(signatures, argTypes);
		
		return result;
	}
	
	public Collection<CandidateMethod<T>> lookup(Collection<MethodEntry<T>> candidates, List<T> argTypes) {
		
		
		// Check if there is an appropriate signature registered
		List<CandidateMethod<T>> result = new ArrayList<CandidateMethod<T>>(); 

		for(MethodEntry<T> candidate : candidates) {	
			 
			MethodSignature<T> signature = candidate.getSignature();
			
			List<T> paramTypes = signature.getParameterTypes(); 
			
			if(paramTypes.size() > argTypes.size()) {
				continue; // Not enough arguments provided
			}
			
			
			if(!signature.isVararg() && paramTypes.size() < argTypes.size()) {
				continue; // Too many arguments provided
			}
			
			int n = argTypes.size(); //Math.min(argTypes.size(), signature.getParameterTypes().size());
			int m = paramTypes.size();
			
			boolean isCandidate = true;
			
			List<ParamDistance> distances = new ArrayList<ParamDistance>(argTypes.size());
			List<CandidateMethod<T>> coercions = new ArrayList<CandidateMethod<T>>();
			
			for(int i = 0; i < n ; ++i) {
				T paramType;
				if(i < m) {
					paramType = paramTypes.get(i);
				} else {
					paramType = signature.getVarArgType();
				}
				
				T argType = argTypes.get(i);
				
				
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

				tryInsertCandidate(result, tmp);
				//result.add(tmp);
			}
		}
		
		return result;
	}


	/**
	 * Inserts a method candidate if it is not subsumed by another one (based on the distance)
	 * 
	 * @param result
	 * @param candidate
	 */
	public static <T> void tryInsertCandidate(List<CandidateMethod<T>> result, CandidateMethod<T> candidate) {
		
		MethodDistance a = candidate.getDistance();
		
		Iterator<CandidateMethod<T>> it = result.iterator();
		boolean isSubsumed = false;
		while(it.hasNext()) {
			CandidateMethod<T> item = it.next();
			MethodDistance b = item.getDistance();
			
			Integer d = a.compare(b);
			if(d != null) {
				if(d < 0) {
					it.remove();
				} else {
					isSubsumed = true;
				}
			}
		}
		
		if(!isSubsumed) {
			result.add(candidate);
		}
	}

	@Override
	public MethodEntry<T> registerFunction(MethodDeclaration<T> declaration) {
		MethodEntry<T> result = this.registerFunction(declaration.toString(), declaration.getName(), declaration.getSignature());
		return result;
	}

	@Override
	public void registerCoercion(MethodDeclaration<T> declaration) {
		this.registerCoercion(declaration.toString(), declaration.getName(), declaration.getSignature());
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
