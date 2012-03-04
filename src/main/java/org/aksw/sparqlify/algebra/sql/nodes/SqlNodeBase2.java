package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Arrays;
import java.util.List;

public abstract class SqlNodeBase2
	extends SqlNodeBase
{
	private SqlNode left;
	private SqlNode right;
	
	public SqlNodeBase2(String aliasName, SqlNode left, SqlNode right) {
		super(aliasName);
		this.left = left;
		this.right = right;
	}

	public SqlNode getLeft() {
		return left;
	}

	public SqlNode getRight() {
		return right;
	}

	@Override
	public SqlNode copy(SqlNode... nodes) {
		checkValidArgsForCopy(nodes);
		return copy2(nodes[0], nodes[1]);
	}

	void checkValidArgsForCopy(SqlNode[] args) {
		if(args.length != 0) {
			throw new RuntimeException("Invalid number of arguments");
		}
	}

	abstract SqlNode copy2(SqlNode left, SqlNode right);
	
	@Override
	public List<SqlNode> getArgs() {
		return Arrays.asList(left, right);
	}
}
