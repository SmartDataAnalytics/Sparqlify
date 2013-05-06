package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

public class SqlOpOrder
	extends SqlOpBase1
{
	private List<SqlSortCondition> sortConditions;

	public SqlOpOrder(Schema schema, SqlOp subOp, List<SqlSortCondition> sortConditions) {
		super(schema, subOp);
		this.sortConditions = sortConditions;
	}

	
	public static SqlOpOrder create(SqlOp subOp, List<SqlSortCondition> sortConditions) {
		SqlOpOrder result = new SqlOpOrder(subOp.getSchema(), subOp, sortConditions);
		return result;
	}


	public List<SqlSortCondition> getSortConditions() {
		return sortConditions;
	}
}
