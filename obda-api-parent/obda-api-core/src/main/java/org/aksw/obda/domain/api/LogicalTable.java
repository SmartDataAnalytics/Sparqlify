package org.aksw.obda.domain.api;

public interface LogicalTable
	extends Polymorphic
{	
	boolean isTableName();
	boolean isQueryString();
	
	String getTableName();
	String getQueryString();
}
