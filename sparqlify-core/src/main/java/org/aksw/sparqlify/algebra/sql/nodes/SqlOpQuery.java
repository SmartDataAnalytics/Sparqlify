package org.aksw.sparqlify.algebra.sql.nodes;

import org.openjena.atlas.io.IndentedWriter;

public class SqlOpQuery
	extends SqlOpLeaf
{
	private String queryString;

	public SqlOpQuery(Schema schema, String queryString) {
		this(schema, queryString, null);
		//super(schema);
		//this.queryString = queryString;
	}

	public SqlOpQuery(Schema schema, String queryString, String aliasName) {
		this(schema, queryString, aliasName, false);
	}

	public SqlOpQuery(Schema schema, String queryString, String aliasName, boolean isEmpty) {
		super(schema, isEmpty, aliasName);
		this.queryString = queryString;
	}

	
	public String getQueryString() {
		return queryString;
	}
		
	@Override
	public void write(IndentedWriter writer) {
		String aliasPart = aliasName == null ? "" : " AS '" + aliasName + "'";
		writer.println("SqlOpQuery[" + queryString + aliasPart + "]");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((aliasName == null) ? 0 : aliasName.hashCode());
		result = prime * result
				+ ((queryString == null) ? 0 : queryString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SqlOpQuery other = (SqlOpQuery) obj;
		if (aliasName == null) {
			if (other.aliasName != null)
				return false;
		} else if (!aliasName.equals(other.aliasName))
			return false;
		if (queryString == null) {
			if (other.queryString != null)
				return false;
		} else if (!queryString.equals(other.queryString))
			return false;
		return true;
	}

	@Override
	public String getId() {
		return queryString;
	}

	
	/*
	@Override
	public String toString() {
		return "SqlOpQuery [queryString=" + queryString + "]";
	}
	*/

	
}
