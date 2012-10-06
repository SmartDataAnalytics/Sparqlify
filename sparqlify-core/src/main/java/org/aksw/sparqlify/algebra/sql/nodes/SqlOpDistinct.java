package org.aksw.sparqlify.algebra.sql.nodes;


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
