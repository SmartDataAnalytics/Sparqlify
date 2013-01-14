package org.aksw.sparqlify.core.cast;

import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.SqlMethodCandidate;
import org.aksw.sparqlify.core.datatypes.XMethod;


public class SqlMethodCollectionUtils {
	public SqlMethodCandidate lookupMethod(String sparqlFunctionName, List<TypeToken> argTypes) {
		return null;
	}
	
	public static SqlMethodCandidate lookupMethod(TypeSystem typeSystem, Collection<XMethod> candidates, List<TypeToken> argTypes) {
		return null;
	}
}

//	
//	private static final Logger logger = LoggerFactory.getLogger(SqlMethodCollectionUtils.class);
//	
//	private DirectSuperTypeProvider<String,> directSuperTypeProvider;
//
//	public static Map<XMethod, Integer[]> findMethodCandidates(Collection<XMethod> candidates, String ...typeSignature)
//    {
//        Map<XMethod, Integer[]> bestMatches = new HashMap<XMethod, Integer[]>();
//        for(XMethod m : candidates) {
//
//            Integer[] d = TypeHierarchyUtils.getDistance(typeSignature, m.getSignature().getParameterTypes().toArray(new XClass[0]));
//            if(d == null || Arrays.asList(d).contains(null)) {
//                continue;
//            }
//
//
//            // All matches that are worse than current candidate are removed
//            // The candidate is only added, if it is not worse than any of the
//            // other candidates
//            boolean canBeAdded = true;
//            for(Iterator<Entry<XMethod, Integer[]>> it = bestMatches.entrySet().iterator(); it.hasNext();) {
//                Entry<XMethod, Integer[]> entry = it.next();
//
//                int rel = ClassUtils.getRelation(d, entry.getValue());
//
//                if(rel == -1) {
//                    it.remove();
//                } else if(rel > 0) {
//                    canBeAdded = false;
//                }
//            }
//
//            if(canBeAdded) {
//                bestMatches.put(m, d);
//            }
//        }
//
//        return bestMatches;
//
//    }
//
//	
////    public SqlMethodCandidate lookupMethod(String sparqlFunctionName, List<TypeToken> argTypes) {
////    	SparqlFunction fn = sparqlFunctions.get(sparqlFunctionName);
////    	if(fn == null) {
////    		return null;
////    	}
////    	Collection<XMethod> candidates = fn.getSqlMethods();
////    	
////    	SqlMethodCandidate result = lookupMethod(candidates, argTypes);
////    	
////    	return result;
////    }
//
//	
//	public static SqlMethodCandidate lookupMethod(TypeSystem typeSystem, Collection<XMethod> candidates, List<TypeToken> argTypes) {
//		//Collection<XMethod> candidates = sqlFunctions.get(name);
//		
//		List<XClass> resolved = TypeHierarchyUtils.resolve(typeSystem, argTypes);
//		XClass[] tmp = resolved.toArray(new XClass[0]);
//		
//		
//        Map<XMethod, TypeDistance[]> bestMatches = findMethodCandidates(candidates, coercionSystem, tmp);
//
//		if(bestMatches.size() == 0) {
//			//throw new RuntimeException("No method found: " + name + " " + argTypes);
//			//throw new RuntimeException("No method found: " + " " + argTypes);
//			logger.debug("No method found: " + " " + argTypes);
//			return null;
//		} else if(bestMatches.size() > 1) {
//			throw new RuntimeException("Multiple matches: " + bestMatches);
//		}
//
//		
//		Entry<XMethod, TypeDistance[]> entry = bestMatches.entrySet().iterator().next();
//		XMethod method = entry.getKey();
//		
//		TypeDistance[] typeDistances = entry.getValue();
//		List<XMethod> argCoercions = new ArrayList<XMethod>(typeDistances.length);
//		for(TypeDistance item : typeDistances) {
//			argCoercions.add(item.getCoercion());
//		}
//		
//		
//		SqlMethodCandidate result = new SqlMethodCandidate(method, argCoercions);
//		
//		return result;
//	}
//
//    public static  Map<XMethod, TypeDistance[]> findMethodCandidates(Collection<XMethod> candidates, CoercionSystemOld coercions, XClass ...typeSignature) {
//        Map<XMethod, TypeDistance[]> bestMatches = new HashMap<XMethod, TypeDistance[]>();
//        for(XMethod m : candidates) {
//
//            TypeDistance[] d = TypeHierarchyUtils.getTypeDistance(typeSignature, m.getSignature().getParameterTypes().toArray(new XClass[0]), coercions);
//            if(d == null || Arrays.asList(d).contains(null)) {
//                continue;
//            }
//
//
//            // All matches that are worse than current candidate are removed
//            // The candidate is only added, if it is not worse than any of the
//            // other candidates
//            boolean canBeAdded = true;
//            for(Iterator<Entry<XMethod, TypeDistance[]>> it = bestMatches.entrySet().iterator(); it.hasNext();) {
//                Entry<XMethod, TypeDistance[]> entry = it.next();
//
//                int rel = TypeHierarchyUtils.getRelation(d, entry.getValue());
//
//                if(rel == -1) {
//                    it.remove();
//                } else if(rel > 0) {
//                    canBeAdded = false;
//                }
//            }
//
//            if(canBeAdded) {
//                bestMatches.put(m, d);
//            }
//        }
//
//        return bestMatches;
//    	
//    	
//    }
//}

