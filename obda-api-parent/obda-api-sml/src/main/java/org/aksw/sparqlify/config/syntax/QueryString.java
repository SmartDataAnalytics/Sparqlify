package org.aksw.sparqlify.config.syntax;

public class QueryString
	implements Relation
{
	private String queryString;
	
	public QueryString(String queryString) {
		super();
		this.queryString = queryString;
	}

	public String getQueryString()
	{
		return queryString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		QueryString other = (QueryString) obj;
		if (queryString == null) {
			if (other.queryString != null)
				return false;
		} else if (!queryString.equals(other.queryString))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return queryString;
	}
}
