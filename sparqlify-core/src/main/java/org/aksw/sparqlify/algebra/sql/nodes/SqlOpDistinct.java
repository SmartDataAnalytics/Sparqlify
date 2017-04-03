package org.aksw.sparqlify.algebra.sql.nodes;

import org.aksw.sparqlify.core.sql.schema.Schema;

public class SqlOpDistinct
	extends SqlOpBase1
{

	public SqlOpDistinct(Schema schema, SqlOp subOp) {
		super(schema, subOp);
	}

	
	public static SqlOpDistinct create(SqlOp subOp) {
		SqlOpDistinct result = new SqlOpDistinct(subOp.getSchema(), subOp);		
		return result;
	}
}
