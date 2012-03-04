package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

import scala.actors.threadpool.Arrays;

public class SqlJoinN
	extends SqlNodeBaseN
{

	public SqlJoinN(String aliasName, List<SqlNode> args) {
		super(aliasName, args);
	}

	public SqlJoinN(String aliasName) {
		super(aliasName);
	}

	@Override
	public SqlNode copy(SqlNode... nodes) {
		return new SqlUnionN(null, Arrays.asList(nodes));
	}

}
