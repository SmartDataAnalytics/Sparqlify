package org.aksw.sparqlify.type_system;

import java.util.Collection;

public interface DirectSuperTypeProvider<T> {
	//Map<String, String> getTypeHierarchy();
	Collection<T> getDirectSuperTypes(T name);
}
