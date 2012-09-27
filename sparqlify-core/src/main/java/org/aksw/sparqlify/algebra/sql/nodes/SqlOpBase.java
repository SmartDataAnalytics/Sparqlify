package org.aksw.sparqlify.algebra.sql.nodes;

public abstract class SqlOpBase
	implements SqlOp
{
	protected Schema schema;
	
	public SqlOpBase(Schema schema) {
		this.schema = schema;
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	public boolean isEmpty() {
		return false;
	}
}
