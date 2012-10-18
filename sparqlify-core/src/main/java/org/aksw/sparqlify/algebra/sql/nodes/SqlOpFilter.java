package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.openjena.atlas.io.IndentedWriter;

public class SqlOpFilter
	extends SqlOpBase1
{
	private List<SqlExpr> exprs;

	public SqlOpFilter(Schema schema, SqlOp subOp, List<SqlExpr> exprs) {
		super(schema, subOp);

		assert exprs != null : "Null pointer exception";
		
		this.exprs = exprs;
	}

	public List<SqlExpr> getExprs() {
		return exprs;
	}
	
	public static SqlOp createIfNeeded(SqlOp op, List<SqlExpr> exprs) {
		SqlOp result;
		
		if(exprs.isEmpty()) {
			result = op;
		} else {
			result = create(op, exprs);
		}
		
		return result;
	}
	
	
	public static SqlOpFilter create(SqlOp op, List<SqlExpr> exprs) {
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
