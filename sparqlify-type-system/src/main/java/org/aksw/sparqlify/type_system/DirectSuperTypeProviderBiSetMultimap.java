package org.aksw.sparqlify.type_system;

import java.util.Collection;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;

public class DirectSuperTypeProviderBiSetMultimap<T>
	implements DirectSuperTypeProvider<T>
{
	private IBiSetMultimap<T, T> typeHierarchy;
	
	public DirectSuperTypeProviderBiSetMultimap(IBiSetMultimap<T, T> typeHierarchy) {
		this.typeHierarchy = typeHierarchy;
	}
	
	@Override
	public Collection<T> getDirectSuperTypes(Object name) {
		Collection<T> result = typeHierarchy.get(name);
		return result;
	}	
}