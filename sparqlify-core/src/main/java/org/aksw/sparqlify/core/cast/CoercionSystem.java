package org.aksw.sparqlify.core.cast;

interface CoercionSystem<T, M>  {
	M lookup(T sourceType, T targetType);
}