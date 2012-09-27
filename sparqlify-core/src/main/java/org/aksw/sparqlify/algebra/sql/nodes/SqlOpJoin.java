package org.aksw.sparqlify.algebra.sql.nodes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.datatype.SqlDatatype;


public class SqlOpJoin
	extends SqlOpBase2
{
	public SqlOpJoin(Schema schema, SqlOp left, SqlOp right) {
		super(schema, left, right);
	}
	
	public static Schema createJoinSchema(Schema a, Schema b) {
		List<String> names = new ArrayList<String>();
		names.addAll(a.getColumnNames());
		names.addAll(b.getColumnNames());
		
		Map<String, SqlDatatype> typeMap = new HashMap<String, SqlDatatype>();
		typeMap.putAll(a.getTypeMap());
		typeMap.putAll(b.getTypeMap());
		
		
		Schema result= new SchemaImpl(names, typeMap);

		return result;
	}
	
	public static SqlOpJoin create(SqlOp a, SqlOp b) {
		
		Schema newSchema = createJoinSchema(a.getSchema(), b.getSchema());
		
		SqlOpJoin result = new SqlOpJoin(newSchema, a, b);
		
		return result;
	}
	
}
