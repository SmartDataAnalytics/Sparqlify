package org.aksw.r2rml.jena.domain.api;

public interface LogicalTable
	extends MappingComponent
{
	String getTableName();
	LogicalTable setTableName(String tableName);
	
	String getSqlQuery();
	LogicalTable setSqlQuery(String sqlQuery);	
}
