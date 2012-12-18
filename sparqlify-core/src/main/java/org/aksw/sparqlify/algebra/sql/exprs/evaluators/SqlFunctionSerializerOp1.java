package org.aksw.sparqlify.algebra.sql.exprs.evaluators;


public class SqlFunctionSerializerOp1
	extends SqlFunctionSerializerBase1
{
	private String opSymbol;
	
	public SqlFunctionSerializerOp1(String opSymbol) {
		this.opSymbol = opSymbol;
	}
	
	
	@Override
	public String serialize(String a) {
		String result = "(" + opSymbol + " " + a + ")";
		return result;
	}
}