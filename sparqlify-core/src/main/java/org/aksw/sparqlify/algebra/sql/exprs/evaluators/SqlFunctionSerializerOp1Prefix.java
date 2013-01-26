package org.aksw.sparqlify.algebra.sql.exprs.evaluators;


public class SqlFunctionSerializerOp1Prefix
	extends SqlFunctionSerializerBase1
{
	private String opSymbol;
	
	public SqlFunctionSerializerOp1Prefix(String opSymbol) {
		this.opSymbol = opSymbol;
	}
	
	
	@Override
	public String serialize(String a) {
		String result = "(" + a + " " + opSymbol + ")";
		return result;
	}
}