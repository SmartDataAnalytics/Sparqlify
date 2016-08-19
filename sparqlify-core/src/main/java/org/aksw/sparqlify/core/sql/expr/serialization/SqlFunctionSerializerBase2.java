package org.aksw.sparqlify.core.sql.expr.serialization;

import java.util.List;


public abstract class SqlFunctionSerializerBase2
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