package org.aksw.obda.domain.impl;

import java.util.Optional;

import org.aksw.obda.domain.api.LogicalTable;

public class LogicalTableQueryString
	implements LogicalTable
{
	protected String queryString;
	
	public LogicalTableQueryString(String queryString) {
		super();
		this.queryString = queryString;
	}

	@Override
	public Optional<String> tryGetQueryString() {
		return Optional.of(queryString);
	}

	@Override
	public String toString() {
		return "LogicalTableQueryString [queryString=" + queryString + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((queryString == null) ? 0 : queryString.hashCode());
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
		LogicalTableQueryString other = (LogicalTableQueryString) obj;
		if (queryString == null) {
			if (other.queryString != null)
				return false;
		} else if (!queryString.equals(other.queryString))
			return false;
		return true;
	}
}
