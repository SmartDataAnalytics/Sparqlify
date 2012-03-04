package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlGroup
	extends SqlNodeBase1
{
	public SqlGroup(SqlNode subNode) {
		super("group", subNode);
	}

	@Override
	SqlNode copy1(SqlNode subNode) {
		return new SqlGroup(subNode);
	}
}
