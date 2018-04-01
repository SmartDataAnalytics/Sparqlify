package org.aksw.obda.domain.impl;

import org.aksw.obda.domain.api.LogicalTable;

public abstract class LogicalTableBase
	implements LogicalTable
{
	@Override
	public boolean isTableName() {
		return tryAs(LogicalTableTableName.class).isPresent();
	}

	@Override
	public boolean isQueryString() {
		return tryAs(LogicalTableQueryString.class).isPresent();
	}

	@Override
	public String getTableName() {
		return tryAs(LogicalTableTableName.class)
				.map(LogicalTableTableName::getTableName)
				.orElseThrow(() -> new RuntimeException("Not a table"));
	}

	@Override
	public String getQueryString() {
		return tryAs(LogicalTableQueryString.class)
				.map(LogicalTableQueryString::getQueryString)
				.orElseThrow(() -> new RuntimeException("Not a query string"));
	}

}
