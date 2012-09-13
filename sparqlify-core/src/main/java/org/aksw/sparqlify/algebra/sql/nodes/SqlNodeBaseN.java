package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.List;

public abstract class SqlNodeBaseN
	extends SqlNodeBase
{
	private List<SqlNode> args;
	
	public SqlNodeBaseN(String aliasName) {
		super(aliasName);
		args = new ArrayList<SqlNode>();
	}

	public SqlNodeBaseN(String aliasName, List<SqlNode> args) {
		super(aliasName);
		this.args = args;
	}
	
	public List<SqlNode> getArgs() {
		return args;
	}

/*
	@Override
	public Scope getIdScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scope getNodeScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void visit(SqlNodeVisitor visitor) {
		// TODO Auto-generated method stub
		
	}
*/	
}