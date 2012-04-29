package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.SqlExprAggregator;

public class SqlGroup
	extends SqlNodeBase1
{
	private List<SqlExprAggregator> aggregators;
	
	public SqlGroup(SqlNode subNode, List<SqlExprAggregator> aggregators) {
		super("group", subNode);
		this.aggregators = aggregators;
	}

	@Override
	SqlNode copy1(SqlNode subNode) {
		return new SqlGroup(subNode, aggregators);
	}
}
