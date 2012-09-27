package org.aksw.sparqlify.algebra.sql.nodes;

public class SqlUnion
	extends SqlNodeBase2
{
	public SqlUnion(SqlNodeOld left, SqlNodeOld right) {
		super(null, left, right);
	}
	
	public SqlUnion(String aliasName, SqlNodeOld left, SqlNodeOld right) {
		super(aliasName, left, right);
	}
	
	@Override
	SqlNodeOld copy2(SqlNodeOld left, SqlNodeOld right) {
		// TODO Auto-generated method stub
		return null;
	}
}