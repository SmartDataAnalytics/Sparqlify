package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Arrays;
import java.util.List;



public abstract class SqlNodeBase1
	extends SqlNodeBase
{
	protected SqlNode subNode;

	public SqlNodeBase1(String aliasName, SqlNode subNode) {
		super(aliasName);
		this.subNode = subNode;
	}

	public SqlNode getSubNode()
	{
		return subNode;
	}
	
	@Override
	public SqlNode copy(SqlNode... nodes) {
		checkValidArgsForCopy(nodes);
		return copy1(nodes[0]);
	}

	void checkValidArgsForCopy(SqlNode[] args) {
		if(args.length != 0) {
			throw new RuntimeException("Invalid number of arguments");
		}
	}
	
	abstract SqlNode copy1(SqlNode subNode);
	
	@Override
	public List<SqlNode> getArgs() {
		return Arrays.asList(subNode);
	}
}
