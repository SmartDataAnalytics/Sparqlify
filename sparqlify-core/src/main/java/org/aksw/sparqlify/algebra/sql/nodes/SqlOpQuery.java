package org.aksw.sparqlify.algebra.sql.nodes;

import org.openjena.atlas.io.IndentedWriter;

public class SqlOpQuery
	extends SqlOpBase0
{
	private String queryString;
	private String aliasName;

	public SqlOpQuery(Schema schema, String queryString) {
		super(schema);
		this.queryString = queryString;
	}

	public SqlOpQuery(Schema schema, String queryString, String aliasName) {
		super(schema);
		this.queryString = queryString;
		this.aliasName = aliasName;
	}

	
	public String getQueryString() {
		return queryString;
	}
	
	public String getAliasName() {
		return aliasName;
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

	
	/*
	@Override
	public String toString() {
		return "SqlOpQuery [queryString=" + queryString + "]";
	}
	*/

	
}
