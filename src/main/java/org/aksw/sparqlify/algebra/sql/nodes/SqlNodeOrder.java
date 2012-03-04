package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.SqlSortCondition;

public class SqlNodeOrder
	extends SqlNodeBase1
{
	private List<SqlSortCondition> conditions;

	public SqlNodeOrder(SqlNode subNode, List<SqlSortCondition> conditions)
	{
	    super(null, subNode);
	    this.conditions = conditions;
	}

	public SqlNodeOrder(String aliasName, SqlNode subNode, List<SqlSortCondition> conditions)
	{
	    super(aliasName, subNode);
	    this.conditions = conditions;
	}
	

	public List<SqlSortCondition> getConditions() {
		return conditions;
	}



	@Override
	SqlNode copy1(SqlNode subNode) {
		return new SqlNodeOrder(this.getSubNode(), conditions);
	}
}
