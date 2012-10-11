package org.aksw.sparqlify.core.datatypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MultiMaps;
import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.factory.Factory1;
import org.aksw.commons.util.reflect.Caster;
import org.aksw.commons.util.reflect.ClassUtils;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;


public class DatatypeSystemCustom
	implements DatatypeSystem
{
	private static final Logger logger = LoggerFactory.getLogger(DatatypeSystemCustom.class);
	
	private Map<TypeToken, XClass> nameToType;
	private IBiSetMultimap<TypeToken, TypeToken> typeHierarchy;
	
	private transient Map<Class<?>, TypeToken> classToToken;

	
	private CoercionSystem coercionSystem;

	
	
	//private List<XMethod> userFunctions = new ArrayList<>
	private Multimap<String, XMethod> userFunctions = HashMultimap.create();
	
	public void register(XMethod method) {
		userFunctions.put(method.getName(), method);
	}
	
	

    public static <T> Map<XMethod, Integer[]> findMethodCandidates(Collection<XMethod> candidates, XClass ...typeSignature)
    {
        Map<XMethod, Integer[]> bestMatches = new HashMap<XMethod, Integer[]>();
        for(XMethod m : candidates) {

            Integer[] d = XClassUtils.getDistance(typeSignature, m.getSignature().getParameterTypes().toArray(new XClass[0]));
            if(d == null || Arrays.asList(d).contains(null)) {
                continue;
            }


            // All matches that are worse than current candidate are removed
            // The candidate is only added, if it is not worse than any of the
            // other candidates
            boolean canBeAdded = true;
            for(Iterator<Entry<XMethod, Integer[]>> it = bestMatches.entrySet().iterator(); it.hasNext();) {
                Entry<XMethod, Integer[]> entry = it.next();

                int rel = ClassUtils.getRelation(d, entry.getValue());

                if(rel == -1) {
                    it.remove();
                } else if(rel > 0) {
                    canBeAdded = false;
                }
            }

            if(canBeAdded) {
                bestMatches.put(m, d);
            }
        }

        return bestMatches;

    }

	
	
	public XMethod lookupMethod(String name, List<TypeToken> argTypes) {
		Collection<XMethod> candidates = userFunctions.get(name);
		
		List<XClass> resolved = XClassUtils.resolve(this, argTypes);
		XClass[] tmp = resolved.toArray(new XClass[0]);
		
		
        Map<XMethod, Integer[]> bestMatches = findMethodCandidates(candidates, tmp);

		if(bestMatches.size() == 0) {
			throw new RuntimeException("No method found: " + name + " " + argTypes);
		} else if(bestMatches.size() > 1) {
			throw new RuntimeException("Multiple matches: " + bestMatches);
		}

		return bestMatches.entrySet().iterator().next().getKey();
	}
	
	public List<TypeToken> getDirectSuperClasses(TypeToken typeToken) {
		Collection<TypeToken> superClasses = typeHierarchy.get(typeToken);
		List<TypeToken> result = new ArrayList<TypeToken>(superClasses);
		
		return result;
	}
	
	
	/**
	 * 
	 * 
	 * @param nameToType
	 * @param typeHierarchy A mapping from sub to super type.
	 */
	public DatatypeSystemCustom(Map<TypeToken, XClass> nameToType, IBiSetMultimap<TypeToken, TypeToken> typeHierarchy) {
		
		
		this.coercionSystem = new CoercionSystemImpl();
		
		
		this.nameToType = nameToType;
		this.typeHierarchy = typeHierarchy;

		
		// TODO Only use the most generic type:
		// usigned short -> integer
		// byte -> integer
		// --> integer -> unsigned short
		// 
		this.classToToken = new HashMap<Class<?>, TypeToken>();
		for(Entry<TypeToken, XClass> entry : nameToType.entrySet()) {
			
			TypeToken typeName = entry.getKey();
			XClass datatype = entry.getValue();
			Class<?> clazz = datatype.getCorrespondingClass();
			if(clazz == null) {
				continue;
			}
			
			TypeToken oldMapping = classToToken.get(clazz);
			
			if(oldMapping == null || isSuperClassOf(typeName, oldMapping)) {
				classToToken.put(clazz, typeName);
			}

			/*
			if(oldMapping != null) {
				logger.warn("Remapping " + clazz.getName() + " to " + datatype + " was: " + oldMapping);
			}
			*/
		}

		for(Entry<Class<?>, TypeToken> entry : classToToken.entrySet()) {
			System.out.println(entry);
		}
	}

	@Override
	public TypeToken getTokenForClass(Class<?> clazz) {
		TypeToken result = classToToken.get(clazz);
		return result;
	}


	/*
	public boolean isMoreSpecific(TypeToken a, TypeToken b) {
		Set<TypeToken> commonParents = MultiMaps.getCommonParent(typeHierarchy.asMap(), a, b);
		
		
	}
	*/


	@Override
	public XClass getByName(TypeToken name) {
		return nameToType.get(name);
	}

	
	@Override
	public XClass getByName(String name) {
		return nameToType.get(TypeToken.alloc(name));
	}


	public XClass getByClass(Class<?> clazz) {
		
		//org.springframework.util.ClassUtils.getAllInterfacesAsSet(instance)
		
		TypeToken typeName = classToToken.get(clazz);
		XClass result = nameToType.get(typeName);
		return result;
	}
	
	
	@Override
	public XClass requireByName(String name) {
		XClass result = nameToType.get(name);
		if(result == null) {
			throw new RuntimeException("No registered datatype found with name '" + name + "'");
		}
		
		return result;
	}


	@Override
	public Object cast(Object value, TypeToken to) {
		XClass targetType = getByName(to);
		
		Class<?> targetClazz = targetType.getCorrespondingClass();
		if(targetClazz == null) {
			//throw new RuntimeException("No class corresponding to '" + to + "' found.");
			logger.warn("No class corresponding to '" + to + "' found.");
			return null;
		}
		return Caster.tryCast(value, targetClazz);
	}


	@Override
	public Factory1<SqlExpr> cast(TypeToken from, TypeToken to) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public TypeToken mostGenericDatatype(TypeToken from, TypeToken to) {
		throw new RuntimeException("This method does not make sense. We coulde always return object");
	}


	@Override
	public Set<TypeToken> supremumDatatypes(TypeToken from, TypeToken to) {
		return MultiMaps.getCommonParent(typeHierarchy.asMap(), from, to);
	}


	@Override
	public Integer compare(TypeToken a, TypeToken b) {
		// TODO Auto-generated method stub
		return null;
	}


	
	public static DatatypeSystemCustom create(Map<String, String> typeToClass, Map<String, String> typeToUri, Map<String, String> typeHierarchy, Logger logger) {
		Set<String> all = new HashSet<String>();
		all.addAll(typeToClass.keySet());
		all.addAll(typeToUri.keySet());
		all.addAll(typeHierarchy.keySet());
		all.addAll(typeHierarchy.values());
		
		Map<TypeToken, XClass> nameToType = new HashMap<TypeToken, XClass>();
		
		for(String typeName : all) {
			TypeToken typeToken = TypeToken.alloc(typeName);
			
			String className = typeToClass.get(typeName);
			Class<?> clazz = null;

			if(className != null) {
				try {
					clazz = Class.forName(className);
				} catch (ClassNotFoundException e) {
					logger.error("Class '" + className + "' not found");
				}
			}
			
			String uri = typeToUri.get(typeName);
			Node node = null; 
			if(uri != null) {
				node = Node.createURI(uri);
			}
			
			XClass datatype = new XClassImpl(null, typeToken, node, clazz);
			
			TypeToken token = TypeToken.alloc(typeName);
			nameToType.put(token, datatype);
		}
		
		IBiSetMultimap<TypeToken, TypeToken> subToSuperType = new BiHashMultimap<TypeToken, TypeToken>();
		
		for(Entry<String, String> entry : typeHierarchy.entrySet()) {
			
			TypeToken subType = TypeToken.alloc(entry.getKey());
			TypeToken superType = TypeToken.alloc(entry.getValue()); 
			//XClass subType = nameToType.get(entry.getKey());
			//XClass superType = nameToType.get(entry.getValue());

			subToSuperType.put(subType, superType);
		}
		

		DatatypeSystemCustom result = new DatatypeSystemCustom(nameToType, subToSuperType);
		return result;
	}

	@Override
	public boolean isSuperClassOf(TypeToken a, TypeToken b) {
		
		Collection<TypeToken> superClasses = MultiMaps.transitiveGet(this.typeHierarchy.asMap(), a);
		boolean result = superClasses.contains(b);
		
		return result;
	}


}
