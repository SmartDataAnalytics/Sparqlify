package org.aksw.sparqlify.core.sql.expr.serialization;

/**
 * Just passes the argument through
 * 
 * Pattern: arg1
 * 
 * @author raven
 *
 */
public class SqlFunctionSerializerPassThrough
	extends SqlFunctionSerializerBase1
{	
	public SqlFunctionSerializerPassThrough() {
	}
	
	
	@Override
	public String serialize(String result) {
		return result;
	}
}