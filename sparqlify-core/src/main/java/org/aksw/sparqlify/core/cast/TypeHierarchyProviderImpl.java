package org.aksw.sparqlify.core.cast;

import java.util.Collection;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.sparqlify.core.TypeToken;

public class TypeHierarchyProviderImpl
	implements DirectSuperTypeProvider<TypeToken>
{
	private IBiSetMultimap<TypeToken, TypeToken> typeHierarchy;
	
	public TypeHierarchyProviderImpl(IBiSetMultimap<TypeToken, TypeToken> typeHierarchy) {
		this.typeHierarchy = typeHierarchy;
	}
	
	@Override
	public Collection<TypeToken> getDirectSuperTypes(TypeToken name) {
		Collection<TypeToken> result = typeHierarchy.get(name);
		return result;
	}
	
}