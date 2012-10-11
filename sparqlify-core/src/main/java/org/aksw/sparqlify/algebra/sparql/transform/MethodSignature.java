package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.List;

/**
 * TODO Rename to MethodTypeSignature
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 * @param <T>
 */
public class MethodSignature<T>
{
	private T returnType;
	private List<T> parameterTypes;
	private T varargType; 

	// FIXME Better store are vararg-type (such as in Java: String ...varargs)
	private boolean isVararg;
	
	public MethodSignature(T returnType,
			boolean isVararg, List<T> parameterTypes) {
		super();
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
		this.isVararg = isVararg;
	}

	public T getReturnType() {
		return returnType;
	}

	public List<T> getParameterTypes() {
		return parameterTypes;
	}
 
	public boolean isVararg() {
		//return isVararg;
		return varargType != null;
	}
	
	public static <T> MethodSignature<T> create(T returnType, List<T> parameterTypes) {
		return new MethodSignature<T>(returnType, false, parameterTypes);
	}

	@Override
	public String toString() {
		return "MethodSignature [returnType=" + returnType
				+ ", parameterTypes=" + parameterTypes + ", isVararg="
				+ isVararg + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isVararg ? 1231 : 1237);
		result = prime * result
				+ ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
		result = prime * result
				+ ((returnType == null) ? 0 : returnType.hashCode());
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
		MethodSignature<?> other = (MethodSignature<?>) obj;
		if (isVararg != other.isVararg)
			return false;
		if (parameterTypes == null) {
			if (other.parameterTypes != null)
				return false;
		} else if (!parameterTypes.equals(other.parameterTypes))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}
}