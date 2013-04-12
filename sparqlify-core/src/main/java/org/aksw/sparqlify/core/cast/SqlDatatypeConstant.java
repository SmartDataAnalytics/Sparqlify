package org.aksw.sparqlify.core.cast;

import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * Maps any node value to a constant.
 * Used for mapping typeError to SQL's NULL::boolean
 * 
 * 
 * @author raven
 *
 */
public class SqlDatatypeConstant
	implements SqlDatatype
{
	private SqlValue sqlValue;

	public SqlDatatypeConstant(SqlValue sqlValue) {
		this.sqlValue = sqlValue;
	}
	
	@Override
	public SqlValue toSqlValue(NodeValue nodeValue) {
		return sqlValue;
	}
	
}