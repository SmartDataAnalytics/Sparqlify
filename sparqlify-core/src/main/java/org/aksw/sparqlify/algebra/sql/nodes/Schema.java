package org.aksw.sparqlify.algebra.sql.nodes;

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
	//boolean isNullable(boolean assumption);
}
