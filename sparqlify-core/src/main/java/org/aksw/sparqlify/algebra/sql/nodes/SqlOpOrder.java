package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlOpOrder
	extends SqlOpBase1
{

	public SqlOpOrder(Schema schema, SqlOp subOp) {
		super(schema, subOp);
	}

	
	public SqlOpOrder create(SqlOp subOp) {
		SqlOpOrder result = new SqlOpOrder(subOp.getSchema(), subOp);
		return result;
	}
}
