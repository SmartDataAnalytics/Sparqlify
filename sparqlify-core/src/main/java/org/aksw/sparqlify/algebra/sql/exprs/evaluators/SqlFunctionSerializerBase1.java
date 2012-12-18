package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import java.util.List;

public abstract class SqlFunctionSerializerBase1
	implements SqlFunctionSerializer
{		
	@Override
	public String serialize(List<String> args) {
		if(args.size() != 1) {
			throw new RuntimeException("Exactly 1 arguments expected, got: " + args);
		}
		
		String result = serialize(args.get(0));
		return result;
	}
	
	public abstract String serialize(String a);	
}
