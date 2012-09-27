package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Arrays;
import java.util.List;



public abstract class SqlNodeBase1
	extends SqlNodeBase
{
	protected SqlNodeOld subNode;

	public SqlNodeBase1(String aliasName, SqlNodeOld subNode) {
		super(aliasName);
		this.subNode = subNode;
	}

	public SqlNodeOld getSubNode()
	{
		return subNode;
	}
	
	@Override
	public SqlNodeOld copy(SqlNodeOld... nodes) {
		checkValidArgsForCopy(nodes);
		return copy1(nodes[0]);
	}

	void checkValidArgsForCopy(SqlNodeOld[] args) {
		if(args.length != 0) {
			throw new RuntimeException("Invalid number of arguments");
		}
	}
	
	abstract SqlNodeOld copy1(SqlNodeOld subNode);
	
	@Override
	public List<SqlNodeOld> getArgs() {
		return Arrays.asList(subNode);
	}
}
