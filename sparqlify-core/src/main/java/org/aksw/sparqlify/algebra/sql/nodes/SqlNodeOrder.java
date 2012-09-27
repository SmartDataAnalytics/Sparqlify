package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import org.aksw.sparqlify.algebra.sql.exprs.SqlSortCondition;

public class SqlNodeOrder
	extends SqlNodeBase1
{
	private List<SqlSortCondition> conditions;

	public SqlNodeOrder(SqlNodeOld subNode, List<SqlSortCondition> conditions)
	{
	    super(null, subNode);
	    this.conditions = conditions;
	}

	public SqlNodeOrder(String aliasName, SqlNodeOld subNode, List<SqlSortCondition> conditions)
	{
	    super(aliasName, subNode);
	    this.conditions = conditions;
	}
	

	public List<SqlSortCondition> getConditions() {
		return conditions;
	}



	@Override
	SqlNodeOld copy1(SqlNodeOld subNode) {
		return new SqlNodeOrder(this.getSubNode(), conditions);
	}
}
