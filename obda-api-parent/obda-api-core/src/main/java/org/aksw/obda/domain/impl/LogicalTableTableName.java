package org.aksw.obda.domain.impl;

import java.util.Optional;

import org.aksw.obda.domain.api.LogicalTable;

public class LogicalTableTableName
	implements LogicalTable
{
	protected String tableName;
	
	public LogicalTableTableName(String tableName) {
		super();
		this.tableName = tableName;
	}

	@Override
	public Optional<String> tryGetTableName() {
		return Optional.of(tableName);
	}
	
	@Override
	public String toString() {
		return "LogicalTableTableName [tableName=" + tableName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
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
		LogicalTableTableName other = (LogicalTableTableName) obj;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}
	
}
