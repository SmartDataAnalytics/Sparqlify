package org.aksw.sparqlify.core.cast;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.XClass;
import org.aksw.sparqlify.type_system.DirectSuperTypeProvider;
import org.aksw.sparqlify.type_system.TypeHierarchyUtils;

public class XClassImpl2
	implements XClass
{
	private TypeResolver typeResolver;
	private DirectSuperTypeProvider hierarchyProvider;
	
	private String name;
	
	public XClassImpl2(TypeResolver typeResolver, DirectSuperTypeProvider hierarchyProvider) {
		this.typeResolver = typeResolver;
		this.hierarchyProvider = hierarchyProvider;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getCorrespondingClass() {
		throw new RuntimeException("Should not be called");
	}

	@Override
	public List<XClass> getDirectSuperClasses() {
		List<XClass> result = TypeSystemUtilsOld.getDirectSuperClasses(name, hierarchyProvider, typeResolver);
		return result;
	}

	@Override
	public boolean isAssignableFrom(XClass that) {
		if(!(that instanceof XClassImpl2)) {
			return false;
		}
		
		XClassImpl2 t = (XClassImpl2)that; 
		
		boolean result = TypeHierarchyUtils.isSuperTypeOf(this.name, t.name, hierarchyProvider);
		
		return result;
	}

	@Override
	@Deprecated
	public TypeToken getToken() {
		TypeToken result = TypeToken.alloc(name);
		return result;
	}
}
