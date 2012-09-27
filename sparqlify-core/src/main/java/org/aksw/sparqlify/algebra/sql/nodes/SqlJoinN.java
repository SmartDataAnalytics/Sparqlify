package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Arrays;
import java.util.List;

public class SqlJoinN
	extends SqlNodeBaseN
{

	public SqlJoinN(String aliasName, List<SqlNodeOld> args) {
		super(aliasName, args);
	}

	public SqlJoinN(String aliasName) {
		super(aliasName);
	}

	@Override
	public SqlNodeOld copy(SqlNodeOld... nodes) {
		return new SqlUnionN(null, Arrays.asList(nodes));
	}

}
