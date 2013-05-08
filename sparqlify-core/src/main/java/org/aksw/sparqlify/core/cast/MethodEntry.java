package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;

public class MethodEntry<T> {
	private String id;
	private String name;
	private MethodSignature<T> signature;
	
	public MethodEntry(String id, String name, MethodSignature<T> signature) {
		super();
		this.id = id;
		this.name = name;
		this.signature = signature;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
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
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		MethodEntry other = (MethodEntry) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
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
		return "MethodEntry [id=" + id + ", name=" + name + ", signature="
				+ signature + "]";
	}
}