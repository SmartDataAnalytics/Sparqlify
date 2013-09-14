package org.aksw.sparqlify.type_system;


/**
 * A class that associates a method declaration with an ID.
 * Essentially the ID is what Java refers to as the "method descriptor"
 * or C++ as the linker symbol.
 * 
 * The intended use of this class is to assign a unique string to a function declaration.
 * 
 * 
 * @author raven
 *
 * @param <T>
 */
public class MethodEntry<T> {
	private String id;
	//private String name;
	//private MethodSignature<T> signature;
	private MethodDeclaration<T> declaration;
	
	@Deprecated
	public MethodEntry(String id, String name, MethodSignature<T> signature) {
		this(id, MethodDeclaration.create(name, signature));
	}
	
	public MethodEntry(String id, MethodDeclaration<T> declaration)
	{
		super();
		this.id = id;
		this.declaration = declaration;		
	}
	
	public static <T> MethodEntry<T> create(String id, MethodDeclaration<T> declaration) {
		return new MethodEntry<T>(id, declaration);
	}

	public MethodDeclaration<T> getDeclaration() {
		return declaration;
	}
	

	public String getId() {
		return id;
	}
	
	@Deprecated
	public String getName() {
		return declaration.getName();
	}

	@Deprecated
	public MethodSignature<T> getSignature() {
		return declaration.getSignature();
	}

	@Override
	public String toString() {
		return "MethodEntry [id=" + id + ", declaration=" + declaration + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((declaration == null) ? 0 : declaration.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		MethodEntry<?> other = (MethodEntry<?>) obj;
		if (declaration == null) {
			if (other.declaration != null)
				return false;
		} else if (!declaration.equals(other.declaration))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	
//	public void setSignature(MethodSignature<T> signature) {
//		this.signature = signature;
//	}
	
	
}