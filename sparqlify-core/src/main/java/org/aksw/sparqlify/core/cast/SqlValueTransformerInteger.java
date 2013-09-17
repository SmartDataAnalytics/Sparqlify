package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;

public class SqlValueTransformerInteger
	implements SqlValueTransformer
{
	/**
	 * TODO The transformer should be able to cast to specific subtypes of int
	 * so: How to handle e.g. the case: (int2)1234 ???
	 * 
	 * In the generic case, we would need something such as e.g.
	 * castToInt(int byteCount, Object value)
	 * 
	 * @param sqlValue
	 * @return
	 * @throws CastException
	 */
	@Override
	public SqlValue transform(SqlValue sqlValue) throws CastException {
		
		
		String str = "" + sqlValue.getValue(); 
		//TypeMapper tm = TypeMapper.getInstance();

		//String typeName = TypeToken.Int.toString();
		Long v;
		try {
			v = Long.parseLong(str);
		} catch(NumberFormatException e) {
			throw new CastException("Could not cast " + str + " to integer");
		}
		
		
		SqlValue result = new SqlValue(TypeToken.Long, v);
		// String typeName = XSD.integer.toString();
		//RDFDatatype dt = tm.getSafeTypeByName(typeName);

		//Node node = Node.createLiteral(str, dt);
		//NodeValue result = NodeValue.makeNode(node);
		return result;
	}
}