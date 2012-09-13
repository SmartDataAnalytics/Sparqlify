package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Collections;
import java.util.List;


public abstract class SqlNodeBase0
	extends SqlNodeBase
{
	public SqlNodeBase0(String aliasName) {
		super(aliasName);
	}

	@Override
	public SqlNode copy(SqlNode... nodes) {
		checkValidArgsForCopy(nodes);
		return copy0();
	}

	void checkValidArgsForCopy(SqlNode[] args) {
		if(args.length != 0) {
			throw new RuntimeException("Invalid number of arguments");
		}
	}
	
	abstract SqlNode copy0();
	
	@Override
	public List<SqlNode> getArgs() {
		return Collections.emptyList();
	}
}
