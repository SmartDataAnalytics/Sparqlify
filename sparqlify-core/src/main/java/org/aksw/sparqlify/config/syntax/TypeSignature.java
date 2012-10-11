package org.aksw.sparqlify.config.syntax;

import java.util.List;

import org.aksw.sparqlify.core.TypeToken;


/**
 * The type signature of a function.
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 * Use MethodSignature instead
 */
@Deprecated
public class TypeSignature {
	private TypeToken returnType;
	private List<TypeToken> argTypes;
	private TypeToken varargType; // null if no varargs
	
	public TypeSignature(TypeToken returnType, List<TypeToken> argTypes,
			TypeToken varargType) {
		super();
		this.returnType = returnType;
		this.argTypes = argTypes;
		this.varargType = varargType;
	}

	public TypeToken getReturnType() {
		return returnType;
	}
	public List<TypeToken> getArgTypes() {
		return argTypes;
	}
	public TypeToken getVarargType() {
		return varargType;
	}
	
	boolean isVararg() {
		return varargType != null;
	}
	
	@Override
	public String toString() {
		return "TypeSignature [returnType=" + returnType + ", argTypes="
				+ argTypes + ", varargType=" + varargType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((argTypes == null) ? 0 : argTypes.hashCode());
		result = prime * result
				+ ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result
				+ ((varargType == null) ? 0 : varargType.hashCode());
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
		TypeSignature other = (TypeSignature) obj;
		if (argTypes == null) {
			if (other.argTypes != null)
				return false;
		} else if (!argTypes.equals(other.argTypes))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		if (varargType == null) {
			if (other.varargType != null)
				return false;
		} else if (!varargType.equals(other.varargType))
			return false;
		return true;
	}
}

