package org.aksw.sparqlify.core.sql.expr.serialization;




public class SqlFunctionSerializerWhen
	extends SqlFunctionSerializerBase2
{

	@Override
	public String serialize(String a, String b) {
		String result = "WHEN " + a + " THEN " + b;
		return result;
	}


}