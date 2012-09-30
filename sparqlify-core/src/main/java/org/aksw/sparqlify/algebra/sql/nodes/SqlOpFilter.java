package org.aksw.sparqlify.algebra.sql.nodes;

import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.sparql.expr.ExprList;

public class SqlOpFilter
	extends SqlOpBase1
{
	private ExprList exprs;

	public SqlOpFilter(Schema schema, SqlOp subOp, ExprList exprs) {
		super(schema, subOp);
		this.exprs = exprs;
	}

	public ExprList getExprs() {
		return exprs;
	}
	
	public static SqlOpFilter create(SqlOp op, ExprList exprs) {
		return new SqlOpFilter(op.getSchema(), op, exprs);
	}
	
	@Override
	public void write(IndentedWriter writer) {
		writer.println("SqlOpFilter" + exprs + "(");
		
		writer.incIndent();
		subOp.write(writer);
		writer.println();
		writer.decIndent();
		
		writer.print(")");
	}
}
