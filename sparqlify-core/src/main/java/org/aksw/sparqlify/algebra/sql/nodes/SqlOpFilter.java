package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.sparql.algebra.transform.SqlExprUtils;
import org.aksw.sparqlify.core.sql.schema.Schema;
import org.apache.jena.atlas.io.IndentedWriter;

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
		SqlOpFilter result = new SqlOpFilter(op.getSchema(), op, exprs); 
		return result;
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
	
	public boolean isEmpty() {
		boolean containsFalse = SqlExprUtils.containsFalse(exprs, true);
		
		boolean result = containsFalse || subOp.isEmpty();
		
		return result;
	}
}
