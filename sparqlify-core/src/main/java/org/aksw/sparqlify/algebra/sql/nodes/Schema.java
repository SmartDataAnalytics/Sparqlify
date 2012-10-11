package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.datatypes.XClass;

public interface Schema {
	int getColumnCount();
	String getColumnName(int index);
	XClass getColumnType(int index);	
	
	XClass getColumnType(String name);
	
	List<String> getColumnNames();
	Map<String, XClass> getTypeMap();
}
