package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlDistinct
	extends SqlNodeBase1
{
	public SqlDistinct(SqlNodeOld subNode) {
		super(null, subNode);
	}

	public SqlDistinct(String aliasName, SqlNodeOld subNode) {
		super(aliasName, subNode);
	}

	@Override
	SqlNodeOld copy1(SqlNodeOld subNode) {
		return new SqlDistinct(getAliasName(), subNode);
	}
}
