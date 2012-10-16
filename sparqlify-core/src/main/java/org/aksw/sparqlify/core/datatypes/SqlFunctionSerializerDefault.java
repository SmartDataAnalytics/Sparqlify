package org.aksw.sparqlify.core.datatypes;

import java.util.List;

import com.google.common.base.Joiner;

/**
 * Default function serializer:
 * ${functionName}(${arg1}, ..., ${argn})
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlFunctionSerializerDefault
	implements SqlFunctionSerializer
{	
	private String functionName;
	
	public SqlFunctionSerializerDefault(String functionName) {
		this.functionName = functionName;
	}
	
	@Override
	public String serialize(List<String> args) {
		String result = functionName + "(" + Joiner.on(", ").join(args) + ")";
		return result;
	}
}
