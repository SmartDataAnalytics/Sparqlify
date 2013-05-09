package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.core.TypeToken;

public class SqlOpEmpty
	extends SqlOpLeaf
{
	public SqlOpEmpty(Schema schema) {
		this(schema, null);
	}

	public SqlOpEmpty(Schema schema, String aliasName) {
		super(schema, true, aliasName);
	}

	public static SqlOpEmpty create() {
		SqlOpEmpty result = create(new SchemaImpl());
		return result;
	}

	/**
	 * Create an empty mapping with an named, null valued, integer column
	 * 
	 * @param columnName
	 * @return
	 */
	public static SqlOpEmpty create(String columnName) {
		List<String> columnNames = new ArrayList<String>(Arrays.asList(columnName));
		Map<String, TypeToken> typeMap = new HashMap<String, TypeToken>();
		typeMap.put(columnName, TypeToken.Int);
		
		SchemaImpl schema = new SchemaImpl(columnNames, typeMap);
		
		SqlOpEmpty result = create(schema);
		return result;
	}

	
	public static SqlOpEmpty create(Schema schema) {
		return new SqlOpEmpty(schema);
	}

	public static SqlOpEmpty create(Schema schema, String aliasName) {
		return new SqlOpEmpty(schema, aliasName);
	}
	
	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public String getId() {
		return "SELECT NULL";
	}	
}
