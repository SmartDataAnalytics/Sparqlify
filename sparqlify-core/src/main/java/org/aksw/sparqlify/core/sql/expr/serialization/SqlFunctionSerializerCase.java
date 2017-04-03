package org.aksw.sparqlify.core.sql.expr.serialization;

import java.util.List;

import com.google.common.base.Joiner;



public class SqlFunctionSerializerCase
	implements SqlFunctionSerializer
{
	@Override
	public String serialize(List<String> args) {
		String result = "CASE ";
		result += Joiner.on(" ").join(args);
		result += " END";
		return result;
	}
}