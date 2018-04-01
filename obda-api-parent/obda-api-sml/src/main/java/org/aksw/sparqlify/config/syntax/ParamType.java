package org.aksw.sparqlify.config.syntax;

import org.apache.jena.sparql.core.Var;

public class ParamType {
	private String datatypeName;
	private Var var;
	
	public ParamType(String datatypeName, Var var) {
		this.datatypeName = datatypeName;
		this.var = var;
	}

	public String getDatatypeName() {
		return datatypeName;
	}

	public Var getVar() {
		return var;
	}

	
	
	@Override
	public String toString() {
		return datatypeName + " " + var;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((datatypeName == null) ? 0 : datatypeName.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
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
		ParamType other = (ParamType) obj;
		if (datatypeName == null) {
			if (other.datatypeName != null)
				return false;
		} else if (!datatypeName.equals(other.datatypeName))
			return false;
		if (var == null) {
			if (other.var != null)
				return false;
		} else if (!var.equals(other.var))
			return false;
		return true;
	}
	
	
}
