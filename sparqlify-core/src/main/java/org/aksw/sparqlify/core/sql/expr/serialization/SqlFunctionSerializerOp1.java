package org.aksw.sparqlify.core.sql.expr.serialization;

/**
 * 
 * Pattern: (opSymbol arg1)
 * 
 * @author raven
 *
 */
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