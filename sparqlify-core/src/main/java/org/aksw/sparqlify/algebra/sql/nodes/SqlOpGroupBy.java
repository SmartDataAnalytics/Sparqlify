package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlAggFunction;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.sql.schema.Schema;
import org.apache.jena.atlas.io.IndentedWriter;

public class SqlOpGroupBy
	extends SqlOpBase1
{
	private List<SqlExpr> groupByExprs;
	private List<SqlAggFunction> aggregators;
	
	/*
	public SqlOpGroupBy(Schema schema, SqlOp subOp) {
		this(schema, subOp, null);
	}
	*/

	public SqlOpGroupBy(Schema schema, SqlOp subOp, List<SqlExpr> groupByExprs, List<SqlAggFunction> aggregators) {
		super(schema, subOp);
		this.groupByExprs = groupByExprs;
		//this.aggregators = aggregators;
	}
	
	public List<SqlExpr> getGroupByExprs() {
		return groupByExprs;
	}

	public List<SqlAggFunction> getAggregators() {
		return aggregators;
	}

	public static SqlOpGroupBy create(SqlOp subOp, List<SqlExpr> groupByExprs, List<SqlAggFunction> aggregators) {
		SqlOpGroupBy result = new SqlOpGroupBy(subOp.getSchema(), subOp, groupByExprs, aggregators);
		return result;
	}

	/*
	public static SqlOpGroupBy create(SqlOp subOp, List<String> groupByColumns, List<SqlExprAggregator> aggregators) {
		
		
		SqlOpGroupBy result = new SqlOpGroupBy(subOp.getSchema(), subOp, groupByExprs, aggregators);
		return result;
	}
	*/

	
	
	@Override
	public boolean isEmpty() {
		boolean result = subOp.isEmpty();
		return result;
	}
	
	@Override
	public void write(IndentedWriter writer) {
		writer.println("SqlOpGroupBy (" + groupByExprs + "," + aggregators);
		
		writer.incIndent();
		subOp.write(writer);
		writer.decIndent();
		
		
		writer.print(")");
	}

}
