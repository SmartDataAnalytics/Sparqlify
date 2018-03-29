package org.aksw.r2rml.api;

import org.apache.jena.rdf.model.Resource;

public interface LogicalTable
	extends Resource
{
	String getTableName();
	LogicalTable setTableName(String tableName);
	
	String getSqlQuery();
	LogicalTable setSqlQuery(String sqlQuery);	
}
