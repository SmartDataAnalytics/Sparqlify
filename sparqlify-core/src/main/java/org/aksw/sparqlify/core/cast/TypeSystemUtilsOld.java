package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.datatypes.XClass;
import org.aksw.sparqlify.type_system.DirectSuperTypeProvider;

public class TypeSystemUtilsOld {
	public static List<XClass> getDirectSuperClasses(String name, DirectSuperTypeProvider typeHierarchy, TypeResolver typeResolver) {
		Collection<String> typeNames = typeHierarchy.getDirectSuperTypes(name);
		
		List<XClass> result = new ArrayList<XClass>(typeNames.size());
		for(String typeName : typeNames) {
			XClass type = typeResolver.resolve(typeName);
			result.add(type);
		}
		
		return result;
	}

}