package org.aksw.sparqlify.core.sql.expr.serialization;

public class SqlFunctionSerializerOp2
	extends SqlFunctionSerializerBase2
{
	private String opSymbol;
	
	public SqlFunctionSerializerOp2(String opSymbol) {
		this.opSymbol = opSymbol;
	}


	@Override
	public String serialize(String a, String b) {
		String result = "(" + a + " " + opSymbol + " " + b + ")";
		return result;
	}
}