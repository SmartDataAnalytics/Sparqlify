package org.aksw.sparqlify.type_system;

public class TypeModelImpl<T>
	implements TypeModel<T>
{
	private DirectSuperTypeProvider<T> directSuperTypeProvider;
	
	public TypeModelImpl(DirectSuperTypeProvider<T> directSuperTypeProvider) {
		this.directSuperTypeProvider = directSuperTypeProvider;
	}


	@Override
	public boolean isSuperTypeOf(T superType, T subType) {
		boolean result = TypeHierarchyUtils.isSuperTypeOf(superType, subType, directSuperTypeProvider);

		return result;
	}	
}