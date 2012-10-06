package org.aksw.sparqlify.config.syntax;

import java.util.List;

public class FunctionSignature {
	private String name;
	private List<ParamType> paramTypeList;
	
	public FunctionSignature(String name, List<ParamType> paramTypeList) {
		super();
		this.name = name;
		this.paramTypeList = paramTypeList;
	}

	public String getName() {
		return name;
	}

	public List<ParamType> getParamTypeList() {
		return paramTypeList;
	}

	@Override
	public String toString() {
		return "FunctionSignature [name=" + name + ", paramTypeList="
				+ paramTypeList + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((paramTypeList == null) ? 0 : paramTypeList.hashCode());
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
		FunctionSignature other = (FunctionSignature) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (paramTypeList == null) {
			if (other.paramTypeList != null)
				return false;
		} else if (!paramTypeList.equals(other.paramTypeList))
			return false;
		return true;
	}
}

