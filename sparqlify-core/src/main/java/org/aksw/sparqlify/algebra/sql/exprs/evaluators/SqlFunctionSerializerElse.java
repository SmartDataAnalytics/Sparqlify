package org.aksw.sparqlify.algebra.sql.exprs.evaluators;




public class SqlFunctionSerializerElse
	extends SqlFunctionSerializerBase1
{

	@Override
	public String serialize(String a) {
		String result = "ELSE " + a;
		
		return result;
	}

}