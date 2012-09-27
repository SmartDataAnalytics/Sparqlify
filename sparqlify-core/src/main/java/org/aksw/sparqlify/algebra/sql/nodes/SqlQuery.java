package org.aksw.sparqlify.algebra.sql.nodes;


public class SqlQuery
	extends SqlNodeBase0
{
	private String queryString;
	
	private String innerAlias;
	
	public SqlQuery(String aliasName) {
		this(aliasName, null, null);
	}

	public SqlQuery(String aliasName, String queryString) {
		this(aliasName, queryString, null);
	}
	
	public SqlQuery(String aliasName, String queryString, String innerAlias) {
		super(aliasName);
		this.queryString = queryString;
		this.innerAlias = innerAlias;
	}
	
	public String getInnerAlias() {
		return innerAlias;
	}
	
	public String getQueryString()
	{
		return queryString;
	}

	@Override
	SqlNodeOld copy0() {
		return null;
	}	
}
