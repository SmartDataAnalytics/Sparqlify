package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Collections;
import java.util.List;

import org.aksw.sparqlify.core.sql.schema.Schema;

public abstract class SqlOpBase1
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
	
	@Override
	public boolean isEmpty() {
		return subOp.isEmpty();
	}

	@Override
	public List<SqlOp> getSubOps() {
		return Collections.singletonList(subOp);
	}
	
	
}
