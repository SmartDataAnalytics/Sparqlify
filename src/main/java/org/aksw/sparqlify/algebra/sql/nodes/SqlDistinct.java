package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlDistinct
	extends SqlNodeBase1
{
	public SqlDistinct(SqlNode subNode) {
		super(null, subNode);
	}

	public SqlDistinct(String aliasName, SqlNode subNode) {
		super(aliasName, subNode);
	}

	@Override
	SqlNode copy1(SqlNode subNode) {
		return new SqlDistinct(getAliasName(), subNode);
	}
}
