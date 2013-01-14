package org.aksw.sparqlify.core.cast;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.SqlMethodCandidate;
import org.aksw.sparqlify.core.datatypes.XMethod;

public class SqlFunctionCollection {
	private TypeSystem typeSystem;
	private Collection<XMethod> functions = new HashSet<XMethod>();
	
	public void add(XMethod method) {
		this.functions.add(method);
	}
	
	public SqlMethodCandidate lookupMethod(List<TypeToken> argTypes) {
		SqlMethodCandidate result = SqlMethodCollectionUtils.lookupMethod(typeSystem, functions, argTypes);
		return result;
	}
	
	public Collection<XMethod> getFunctions() {
		return functions;
	}
}
