package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.Arrays;
import java.util.List;

public abstract class SqlNodeBase2
	extends SqlNodeBase
{
	private SqlNodeOld left;
	private SqlNodeOld right;
	
	public SqlNodeBase2(String aliasName, SqlNodeOld left, SqlNodeOld right) {
		super(aliasName);
		this.left = left;
		this.right = right;
	}

	public SqlNodeOld getLeft() {
		return left;
	}

	public SqlNodeOld getRight() {
		return right;
	}

	@Override
	public SqlNodeOld copy(SqlNodeOld... nodes) {
		checkValidArgsForCopy(nodes);
		return copy2(nodes[0], nodes[1]);
	}

	void checkValidArgsForCopy(SqlNodeOld[] args) {
		if(args.length != 0) {
			throw new RuntimeException("Invalid number of arguments");
		}
	}

	abstract SqlNodeOld copy2(SqlNodeOld left, SqlNodeOld right);
	
	@Override
	public List<SqlNodeOld> getArgs() {
		return Arrays.asList(left, right);
	}
}
