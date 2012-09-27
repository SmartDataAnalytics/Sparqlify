package org.aksw.sparqlify.algebra.sql.nodes;

public abstract class SqlOpBase2
	extends SqlOpBase
{
	protected SqlOp left;
	protected SqlOp right;

	public SqlOpBase2(Schema schema, SqlOp left, SqlOp right) {
		super(schema);
		this.left = left;
		this.right = right;
	}
	
	public SqlOp getLeft() {
		return left;
	}
	
	public SqlOp getRight() {
		return right;
	}
}
