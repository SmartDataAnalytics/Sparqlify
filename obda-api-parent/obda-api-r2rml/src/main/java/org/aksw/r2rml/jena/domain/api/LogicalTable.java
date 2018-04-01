package org.aksw.r2rml.jena.domain.api;

/**
 * Interface for RDF-based logical tables.
 * 
 * As this denotes the same information as in the more basic
 * {@link org.aksw.obda.domain.api.LogicalTable}, deriving from it should be
 * safe.
 * 
 *  
 * 
 * @author raven Apr 1, 2018
 *
 */
public interface LogicalTable
	extends MappingComponent, org.aksw.obda.domain.api.LogicalTable
{
	//String getTableName();
	LogicalTable setTableName(String tableName);
	
	//String getSqlQuery();
	LogicalTable setQueryString(String sqlQuery);	
}
