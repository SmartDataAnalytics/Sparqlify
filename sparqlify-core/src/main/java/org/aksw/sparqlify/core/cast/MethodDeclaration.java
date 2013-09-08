package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;

import com.google.common.base.Joiner;

/**
 * Combines a name with a signature.
 * 
 * A method declaration corresponds to a single method - i.e. it does NOT account for overloads.
 * 
 * @author raven
 *
 * @param <T>
 */
public class MethodDeclaration<T> {
	private String name;
	private MethodSignature<T> signature;
	
	/**
	 * Convenience factory function. Prefer this over the ctor.
	 * 
	 * @param resultType
	 * @param name
	 * @param isVararg
	 * @param paramTypes
	 * @return
	 */
	public static <T> MethodDeclaration<T> create(T returnType, String name, boolean isVararg, T ... paramTypes) {
		MethodSignature<T> signature = MethodSignature.create(isVararg, returnType, paramTypes);
		
		MethodDeclaration<T> result = create(name, signature);
		return result;
	}
	
	public static <T> MethodDeclaration<T> create(T returnType, String name) {
		@SuppressWarnings("unchecked")
		MethodSignature<T> signature = MethodSignature.create(false, returnType);
		
		MethodDeclaration<T> result = create(name, signature);
		return result;
	}

	
	public static <T> MethodDeclaration<T> create(String name, MethodSignature<T> signature) {
		MethodDeclaration<T> result = new MethodDeclaration<T>(name, signature);
		return result;
	}
	
	public MethodDeclaration(String name, MethodSignature<T> signature) {
		super();
		this.name = name;
		this.signature = signature;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MethodSignature<T> getSignature() {
		return signature;
	}

	public void setSignature(MethodSignature<T> signature) {
		this.signature = signature;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((signature == null) ? 0 : signature.hashCode());
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
		MethodDeclaration other = (MethodDeclaration) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (signature == null) {
			if (other.signature != null)
				return false;
		} else if (!signature.equals(other.signature))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String argStr = Joiner.on(", ").join(signature.getParameterTypes());
		
		if(signature.isVararg()) {
			if(!signature.getParameterTypes().isEmpty()) {
				argStr += ", ";
			}
			
			argStr += "...";
		}
		
		String result = signature.getReturnType() + " " + name + " (" +  argStr + ")";
		return result;
	}
}