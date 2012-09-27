package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;

public interface Schema {
	int getColumnCount();
	String getColumnName(int index);
	SqlDatatype getColumnType(int index);	
	
	SqlDatatype getColumnType(String name);
	
	List<String> getColumnNames();
	Map<String, SqlDatatype> getTypeMap();
}
