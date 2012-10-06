package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.config.syntax.FunctionDeclaration;
import org.aksw.sparqlify.core.SqlDatatype;

public class RegisteredFunction
{
	private FunctionDeclaration declaration;
	private MethodSignature<SqlDatatype> typeSignature;
	
	
	
	public RegisteredFunction(FunctionDeclaration declaration,
			MethodSignature<SqlDatatype> typeSignature) {
		super();
		this.declaration = declaration;
		this.typeSignature = typeSignature;
	}
	
	public FunctionDeclaration getDeclaration() {
		return declaration;
	}
	public MethodSignature<SqlDatatype> getTypeSignature() {
		return typeSignature;
	}

	@Override
	public String toString() {
		return "RegisteredFunction [declaration=" + declaration
				+ ", typeSignature=" + typeSignature + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((declaration == null) ? 0 : declaration.hashCode());
		result = prime * result
				+ ((typeSignature == null) ? 0 : typeSignature.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegisteredFunction other = (RegisteredFunction) obj;
		if (declaration == null) {
			if (other.declaration != null)
				return false;
		} else if (!declaration.equals(other.declaration))
			return false;
		if (typeSignature == null) {
			if (other.typeSignature != null)
				return false;
		} else if (!typeSignature.equals(other.typeSignature))
			return false;
		return true;
	}
}