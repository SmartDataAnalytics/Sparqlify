package org.aksw.sparqlify.core.sql.schema;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.TypeToken;

public interface Schema {
	int getColumnCount();
	String getColumnName(int index);
	TypeToken getColumnType(int index);	
	
	TypeToken getColumnType(String name);
	
	List<String> getColumnNames();
	Map<String, TypeToken> getTypeMap();
	
	boolean isNullable(String columnName);
	
	
	Schema createSubSchema(List<String> columnNames);
	
	// Any combinations of columns that are unique. Includes primary/foreign key constraints.
	//List<Set<String>> uniqueConstraints();
	
	//boolean isNullable(boolean assumption);
}
