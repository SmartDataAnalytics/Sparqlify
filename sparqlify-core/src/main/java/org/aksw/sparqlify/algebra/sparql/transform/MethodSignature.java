package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.Arrays;
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
	private List<T> paramTypes;
	private T varArgType; 

	public MethodSignature(T returnType, List<T> paramTypes, T varArgType) {
		super();
		this.returnType = returnType;
		this.paramTypes = paramTypes;
		this.varArgType = varArgType;
	}

	public T getReturnType() {
		return returnType;
	}

	public List<T> getParameterTypes() {
		return paramTypes;
	}
	
	public T getVarArgType() {
		return varArgType;
	}
 
	public boolean isVararg() {
		//return isVararg;
		return varArgType != null;
	}
	
	public static <T> MethodSignature<T> create(T returnType, List<T> parameterTypes, T varArgType) {
		return new MethodSignature<T>(returnType, parameterTypes, null);
	}

	/*
	public static <T> MethodSignature<T> create(T returnType, T... parameterTypes) {
		return new MethodSignature<T>(returnType, Arrays.asList(parameterTypes), null);
	}
	*/

	/**
	 * 
	 * @param returnType
	 * @param isVarArg If true, the last paramType becomes the vararg type
	 * @param paramTypes
	 * @return
	 */
	public static <T> MethodSignature<T> create(boolean isVarArg, T returnType, T... paramTypes) {
		
		T varArgType = null;
		List<T> fixedArgTypes = Arrays.asList(paramTypes);

		if(isVarArg) {
			if(paramTypes.length == 0) {
				throw new RuntimeException("Need a type for varArgs");
			}
			
			int lastIndex = paramTypes.length - 1;
			varArgType = paramTypes[lastIndex];
			fixedArgTypes = fixedArgTypes.subList(0, lastIndex);
		}	

		MethodSignature<T> result = create(returnType, fixedArgTypes, varArgType);
		return result;
		//return new MethodSignature<T>(returnType, fixedArgTypes, varArgType);
	}

	
	
	@Override
	public String toString() {
		return "MethodSignature [returnType=" + returnType + ", paramTypes="
				+ paramTypes + ", varArgType=" + varArgType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((paramTypes == null) ? 0 : paramTypes.hashCode());
		result = prime * result
				+ ((returnType == null) ? 0 : returnType.hashCode());
		result = prime * result
				+ ((varArgType == null) ? 0 : varArgType.hashCode());
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
		if (paramTypes == null) {
			if (other.paramTypes != null)
				return false;
		} else if (!paramTypes.equals(other.paramTypes))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		if (varArgType == null) {
			if (other.varArgType != null)
				return false;
		} else if (!varArgType.equals(other.varArgType))
			return false;
		return true;
	}
}