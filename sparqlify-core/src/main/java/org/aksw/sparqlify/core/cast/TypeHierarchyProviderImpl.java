package org.aksw.sparqlify.core.cast;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.type_system.DirectSuperTypeProviderBiSetMultimap;


public class TypeHierarchyProviderImpl
	extends DirectSuperTypeProviderBiSetMultimap<TypeToken>
{
	private IBiSetMultimap<TypeToken, TypeToken> typeHierarchy;
	
	public TypeHierarchyProviderImpl(IBiSetMultimap<TypeToken, TypeToken> typeHierarchy) {
		super(typeHierarchy);
	}	
}