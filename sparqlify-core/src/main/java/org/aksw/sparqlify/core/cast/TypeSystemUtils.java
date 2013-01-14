package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.sparqlify.core.datatypes.XClass;

public class TypeSystemUtils {
	public static List<XClass> getDirectSuperClasses(String name, DirectSuperTypeProvider typeHierarchy, TypeResolver typeResolver) {
		Collection<String> typeNames = typeHierarchy.getDirectSuperTypes(name);
		
		List<XClass> result = new ArrayList<XClass>(typeNames.size());
		for(String typeName : typeNames) {
			XClass type = typeResolver.resolve(typeName);
			result.add(type);
		}
		
		return result;
	}

	
	public static <T> Set<T> getAllSuperTypes(T typeName, DirectSuperTypeProvider<T> typeHierarchyProvider) {
		Set<T> open = new HashSet<T>();
		Set<T> done = new HashSet<T>();
		
		open.add(typeName);

		while(!open.isEmpty()) {
			Iterator<T> it = open.iterator();
			T name = it.next();
			it.remove();
			
			done.add(name);
			
			Collection<T> superTypes = typeHierarchyProvider.getDirectSuperTypes(name);
			for(T superType : superTypes) {
				if(!done.contains(superType)) {
					open.add(superType);
				}
			}
		}
		
		return done;
	}
	
	public static <T> boolean isSuperClassOf(T a, T b, DirectSuperTypeProvider<T> typeHierarchyProvider) {
		Collection<T> superClasses = getAllSuperTypes(a, typeHierarchyProvider);
		boolean result = superClasses.contains(b);
		
		return result;
	}
}