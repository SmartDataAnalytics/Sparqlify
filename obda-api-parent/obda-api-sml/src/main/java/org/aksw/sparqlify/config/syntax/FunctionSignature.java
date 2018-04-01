package org.aksw.sparqlify.config.syntax;

import java.util.List;

import com.google.common.base.Joiner;

public class FunctionSignature {
	private String functionName;
	private String returnTypeName;
	private List<ParamType> paramTypeList;
	
	public FunctionSignature(String functionName, String returnTypeName, List<ParamType> paramTypeList) {
		super();
		this.functionName = functionName;
		this.returnTypeName = returnTypeName;
		this.paramTypeList = paramTypeList;
	}

	public String getFunctionName() {
		return functionName;
	}

	public String getReturnTypeName() {
		return returnTypeName;
	}
	
	public List<ParamType> getParamTypeList() {
		return paramTypeList;
	}
	
	@Override
	public String toString() {
		return returnTypeName + " " + functionName + "(" + Joiner.on(", ").join(paramTypeList) + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((functionName == null) ? 0 : functionName.hashCode());
		result = prime * result
				+ ((paramTypeList == null) ? 0 : paramTypeList.hashCode());
		result = prime * result
				+ ((returnTypeName == null) ? 0 : returnTypeName.hashCode());
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
		if (functionName == null) {
			if (other.functionName != null)
				return false;
		} else if (!functionName.equals(other.functionName))
			return false;
		if (paramTypeList == null) {
			if (other.paramTypeList != null)
				return false;
		} else if (!paramTypeList.equals(other.paramTypeList))
			return false;
		if (returnTypeName == null) {
			if (other.returnTypeName != null)
				return false;
		} else if (!returnTypeName.equals(other.returnTypeName))
			return false;
		return true;
	}

	
}

