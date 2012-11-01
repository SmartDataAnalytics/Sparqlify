package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExprAggregator;

public class SqlOpGroupBy
	extends SqlOpBase1
{
	private List<String> groupColumns;
	private List<SqlExprAggregator> aggregators;
	
	/*
	public SqlOpGroupBy(Schema schema, SqlOp subOp) {
		this(schema, subOp, null);
	}
	*/

	public SqlOpGroupBy(Schema schema, SqlOp subOp, List<String> groupColumns, List<SqlExprAggregator> aggregators) {
		super(schema, subOp);
		this.groupColumns = groupColumns;
		this.aggregators = aggregators;
	}
	
	public List<String> getGroupColumns() {
		return groupColumns;
	}

	public List<SqlExprAggregator> getAggregators() {
		return aggregators;
	}

	public static SqlOpGroupBy create(SqlOp subOp, List<String> groupColumns, List<SqlExprAggregator> aggregators) {
		SqlOpGroupBy result = new SqlOpGroupBy(subOp.getSchema(), subOp, groupColumns, aggregators);
		return result;
	}

	@Override
	public boolean isEmpty() {
		boolean result = subOp.isEmpty();
		return result;
	}
}
