package org.aksw.sparqlify.core.sql.expr.serialization;




public class SqlFunctionSerializerElse
	extends SqlFunctionSerializerBase1
{

	@Override
	public String serialize(String a) {
		String result = "ELSE " + a;
		
		return result;
	}

}