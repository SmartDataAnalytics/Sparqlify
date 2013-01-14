package org.aksw.sparqlify.core.cast;

import java.util.Collection;

interface DirectSuperTypeProvider<T> {
	//Map<String, String> getTypeHierarchy();
	Collection<T> getDirectSuperTypes(T name);
}
