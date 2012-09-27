package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlOpBase1
	extends SqlOpBase
{
	protected SqlOp subOp;

	public SqlOpBase1(Schema schema, SqlOp subOp) {
		super(schema);
		this.subOp = subOp;
	}
	
	public SqlOp getSubOp() {
		return subOp;
	}
}
