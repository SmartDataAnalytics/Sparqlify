package org.aksw.sparqlify.algebra.sql.exprs.evaluators;




public class SqlFunctionSerializerWhen
	extends SqlFunctionSerializerBase2
{

	@Override
	public String serialize(String a, String b) {
		String result = "WHEN " + a + " THEN " + b;
		return result;
	}


}