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
	public SqlNodeOld copy(SqlNodeOld... nodes) {
		checkValidArgsForCopy(nodes);
		return copy0();
	}

	void checkValidArgsForCopy(SqlNodeOld[] args) {
		if(args.length != 0) {
			throw new RuntimeException("Invalid number of arguments");
		}
	}
	
	abstract SqlNodeOld copy0();
	
	@Override
	public List<SqlNodeOld> getArgs() {
		return Collections.emptyList();
	}
}
