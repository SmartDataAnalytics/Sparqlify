package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.openjena.atlas.io.IndentedWriter;

public abstract class SqlOpBaseN
	extends SqlOpBase
{
	protected List<SqlOp> subOps;

	public SqlOpBaseN(Schema schema, List<SqlOp> subOps) {
		super(schema);
		this.subOps = subOps;
	}
		
	public List<SqlOp> getSubOps() {
		return subOps;
	}
	
	@Override
	public void write(IndentedWriter writer) {
		writer.println("SqlOpUnion(" + schema.getColumnNames());
		
		writer.incIndent();
		
		boolean isFirst = true;
		for(SqlOp subOp : subOps) {
			subOp.write(writer);
			if(isFirst) { 
				writer.println(",");
				isFirst = false;
			} else {
				writer.println();
			}
		}
		writer.decIndent();		
		
		writer.print(")");
	}
}
