package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Arrays;
import java.util.List;

public class SqlUnionN
	extends SqlNodeBaseN
{
	public SqlUnionN(String aliasName, List<SqlNode> args) {
		super(aliasName, args);
	}

	public SqlUnionN(String aliasName) {
		super(aliasName);
	}

	@Override
	public SqlNode copy(SqlNode... nodes) {
		// XXX WAS NULL
		return new SqlUnionN(this.getAliasName(), Arrays.asList(nodes));
	}	
}
