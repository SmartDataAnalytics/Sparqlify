package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;

public class SchemaImpl
	implements Schema
{
	private List<String> names;
	private Map<String, SqlDatatype> nameToType;

	public SchemaImpl(List<String> names, Map<String, SqlDatatype> nameToType) {
		this.names = names;
		this.nameToType = nameToType;
	}
	
	public static SchemaImpl create(List<String> names, Map<String, SqlDatatype> nameToType) {
		return new SchemaImpl(names, nameToType);
	}
	
	@Override
	public int getColumnCount() {
		return names.size();
	}

	@Override
	public String getColumnName(int index) {
		return names.get(index);
	}

	@Override
	public SqlDatatype getColumnType(int index) {
		String name = names.get(index);
		SqlDatatype result = nameToType.get(name);
		
		return result;
	}

	@Override
	public List<String> getColumnNames() {
		return names;
	}

	@Override
	public SqlDatatype getColumnType(String name) {
		SqlDatatype result = nameToType.get(name);
		
		return result;
	}

	@Override
	public Map<String, SqlDatatype> getTypeMap() {
		return nameToType;
	}

}
