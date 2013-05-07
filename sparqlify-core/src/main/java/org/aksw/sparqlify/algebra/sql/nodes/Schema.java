package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.sparqlify.core.TypeToken;

public interface Schema {
	int getColumnCount();
	String getColumnName(int index);
	TypeToken getColumnType(int index);	
	
	TypeToken getColumnType(String name);
	
	List<String> getColumnNames();
	Map<String, TypeToken> getTypeMap();
	
	boolean isNullable(String columnName);
	
	
	// Any combinations of columns that are unique. Includes primary/foreign key constraints.
	//List<Set<String>> uniqueConstraints();
	
	//boolean isNullable(boolean assumption);
}
