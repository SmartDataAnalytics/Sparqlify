package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlUnion
	extends SqlNodeBase2
{
	public SqlUnion(SqlNode left, SqlNode right) {
		super(null, left, right);
	}
	
	public SqlUnion(String aliasName, SqlNode left, SqlNode right) {
		super(aliasName, left, right);
	}
	
	@Override
	SqlNode copy2(SqlNode left, SqlNode right) {
		// TODO Auto-generated method stub
		return null;
	}
}