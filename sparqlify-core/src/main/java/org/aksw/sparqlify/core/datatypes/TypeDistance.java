package org.aksw.sparqlify.core.datatypes;

public class TypeDistance {
 
	private Integer inheritanceDepth;
	private XMethod coercion;
	
	public TypeDistance(Integer inheritanceDepth, XMethod coercion) {
		this.inheritanceDepth = inheritanceDepth;
		this.coercion = coercion;
	}

	public Integer getInheritanceDepth() {
		return inheritanceDepth;
	}

	public XMethod getCoercion() {
		return coercion;
	}

	@Override
	public String toString() {
		return "TypeDistance [inheritanceDepth=" + inheritanceDepth
				+ ", coercion=" + coercion + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((coercion == null) ? 0 : coercion.hashCode());
		result = prime * result + inheritanceDepth;
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
		TypeDistance other = (TypeDistance) obj;
		if (coercion == null) {
			if (other.coercion != null)
				return false;
		} else if (!coercion.equals(other.coercion))
			return false;
		if (inheritanceDepth != other.inheritanceDepth)
			return false;
		return true;
	}
	
	
}