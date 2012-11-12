package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.List;

import org.aksw.sparqlify.core.datatypes.SqlFunctionSerializer;

abstract class SqlFunctionSerializerBase2
	implements SqlFunctionSerializer
{		
	@Override
	public String serialize(List<String> args) {
		if(args.size() != 2) {
			throw new RuntimeException("Exactly 2 arguments expected, got: " + args);
		}
		
		String result = serialize(args.get(0), args.get(1));
		return result;
	}
	
	public abstract String serialize(String a, String b);	
}