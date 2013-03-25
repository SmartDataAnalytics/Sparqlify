package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

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