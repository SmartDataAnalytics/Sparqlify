package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;

public class SqlOpUnionN
	extends SqlOpBaseN
{
	private String aliasName;
	
	
	public SqlOpUnionN(Schema schema, List<SqlOp> subOps) {
		super(schema, subOps);
	}

	public SqlOpUnionN(Schema schema, List<SqlOp> subOps, String aliasName) {
		super(schema, subOps);
		this.aliasName = aliasName;
	}
	
	public String getAliasName() {
		return aliasName;
	}

	
	public static SqlOpUnionN create(List<SqlOp> subOps) {
		
		System.err.println("FIXME schemas of union members not validated");
		
		SqlOp pick = subOps.get(0);
		Schema schema = pick.getSchema();
		
		return new SqlOpUnionN(schema, subOps);
	}
}
