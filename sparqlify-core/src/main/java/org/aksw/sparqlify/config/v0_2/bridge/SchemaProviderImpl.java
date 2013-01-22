package org.aksw.sparqlify.config.v0_2.bridge;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SchemaImpl;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * A class that provides the schema for a SQL query string or table name
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SchemaProviderImpl
	implements SchemaProvider
{
	private static final Logger logger = LoggerFactory.getLogger(SchemaProvider.class);
	
	private Connection conn;
	private TypeSystem datatypeSystem;
	private Map<String, String> aliasMap; // TODO Maybe this has to be a function to capture int([0-9]*) -> int
	
	public SchemaProviderImpl(Connection conn, TypeSystem datatypeSystem, Map<String, String> aliasMap) {
		this.conn = conn;
		this.datatypeSystem = datatypeSystem;
		this.aliasMap = aliasMap;
	}
	
	
	
	public Schema createSchemaForRelationName(String tableName) {

		//Set<String> tableNameCache = JdbcUtils.fetchRelationNames(conn);

		
		// TODD We might have to escape table names...
		String escTableName = "\"" + tableName + "\"";
		
		// FIXME Use database metadata for fetching schemas of tables
		Schema result = createSchemaForQueryString("SELECT * FROM " + escTableName);

		return result;
	}
	
	
	public Schema createSchemaForQueryString(String queryString) {

		logger.info("Retrieving schema for query: " + queryString);
		
		logger.warn("Using ugly hack for adding a limit");
		if(!queryString.contains("LIMIT")) {
			queryString += " LIMIT 1";
		}

		Map<String, TypeToken> typeMap = null;
		
		try {
			typeMap = SchemaProviderImpl.getTypes(conn, queryString, datatypeSystem, aliasMap);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		for(Entry<String, TypeToken> entry : typeMap.entrySet()) {
			logger.info(entry.getKey() + " -> " + entry.getValue());
		}
		
		
		// FIXME Preserve order of column names
		List<String> columnNames = new ArrayList<String>(typeMap.keySet());
		
		
		Schema result = new SchemaImpl(columnNames, typeMap);
		
		return result;
	}
	

	/*
	public static void loadDatatypes(Connection conn, Collection<RdfView> views)
		throws Exception
	{
		for(RdfView view : views) {
			
			if(view.getSqlNode() == null ) {
				continue;
			}
			
			if(!view.getColumnToDatatype().isEmpty()) {
				continue;
			}
		
			SqlGenerator generator = new SqlGenerator();
			
			
			//String queryString = generator.generateMM(view.getSqlNode()); //SqlAlgebraToString.asString(view.getSqlNode());
			String queryString = getTableOrQueryAsQuery(view.getSqlNode()).getQueryString();
			
			
			logger.debug("Retrieving datatypes for columns of: " + queryString);
			
			Map<String, SqlDatatype> columnToType = RdfViewDatabase.getTypes(conn, queryString);
			for(Entry<String, SqlDatatype> entry : columnToType.entrySet()) {
				logger.info(entry.getKey() + " -> " + entry.getValue());
			}
			
			view.getColumnToDatatype().putAll(columnToType);
		}
	}
	*/


	public static Map<String, String> getRawTypes(Connection conn, String queryStr)
			throws Exception
	{
		
		
		Map<String, String> result = new HashMap<String,String>();
		ResultSet rs = conn.createStatement().executeQuery(queryStr);
	
		try {
			ResultSetMetaData meta = rs.getMetaData();
			
			for(int i = 1; i <= meta.getColumnCount(); ++i) {
				String name = meta.getColumnName(i);
				//int type = meta.getColumnType(i);
				
				String typeName = meta.getColumnTypeName(i);
				//System.out.println("TypeName: " + typeName);
			
				result.put(name, typeName);
			}
		} finally {
			rs.close();
		}

		/*
		System.out.println(SqlDatatypeBoolean.getInstance().hashCode());
		System.out.println(SqlDatatypeInteger.getInstance().hashCode());
		System.out.println(SqlDatatypeString.getInstance().hashCode());
		*/

		return result;
	}

//	public static XClass lookupDatatype(String typeName, TypeSystem datatypeSystem, Map<String, String> aliasMap) {
////		throw new RuntimeException("No longer supported");
//		System.out.println("Alias name lookup with lower case");
//		String lookupName = aliasMap.get(typeName.toLowerCase());
//		
//		if(lookupName == null) {
//			lookupName = typeName;
//		}
//		
//		TypeMapper typeMaper = datatypeSystem.getTypeMapper();
//		RDFDatatype type = typeMapper.getTypeByName(lookupName);
//		
//		if(type == null) {
//			throw new RuntimeException("Raw SQL datatype '" + typeName + "' not mapped");			
//		}
//		
//		return result;
//	}

		
//	public static Map<String, TypeToken> transformRawMap(Map<String, String> map, TypeSystem datatypeSystem, Map<String, String> aliasMap) {
//		Map<String, TypeToken> result = new HashMap<String, TypeToken>();
//
//		for(Map.Entry<String, String> entry : map.entrySet()) {
//			XClass clazz = lookupDatatype(entry.getValue(), datatypeSystem, aliasMap);
//			
//			result.put(entry.getKey(), clazz.getToken());
//		}
//		return result;
//	}
	public static Map<String, TypeToken> transformRawMap(Map<String, String> map, TypeSystem datatypeSystem, Map<String, String> aliasMap) {
		Map<String, TypeToken> result = new HashMap<String, TypeToken>();

		for(Map.Entry<String, String> entry : map.entrySet()) {
			// TODO Get a datatype object that can convert SQL Values between NodeValues and vice versa.
			// Problem: Can we have a NodeValue with datatype but Null string?
			
			
			//XClass clazz = lookupDatatype(entry.getValue(), datatypeSystem, aliasMap);
			//result.put(entry.getKey(), clazz.getToken());
			
			TypeToken typeToken = TypeToken.alloc(entry.getValue());
			result.put(entry.getKey(), typeToken);

		}
		
		return result;
	}

	
	public static Map<String, TypeToken> getTypes(Connection conn, String queryStr, TypeSystem datatypeSystem, Map<String, String> aliasMap)
		throws Exception
	{
		Map<String, String> map = getRawTypes(conn, queryStr);
		Map<String, TypeToken> result = transformRawMap(map, datatypeSystem, aliasMap);
		return result;
	}



	@Override
	public TypeSystem getDatatypeSystem() {
		return datatypeSystem;
	}
}
