package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.apache.jena.atlas.io.IndentedWriter;

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
	
	public abstract boolean isEmpty();

	
//	public boolean isEmpty() {
//		return false;
//	}
	
	public void write(IndentedWriter writer) {
		//writer.println(toString());
		writer.println(this.getClass().getSimpleName());
		//SqlOpFormatter.format(this);
	}
	
	@Override
	public String toString() {
		String result = SqlOpFormatter.format(this);
		return result;
	}
	
	public SqlOp copy(Schema schema, List<SqlOp> newOps) {
		throw new RuntimeException("Not implemented");
	}
}
