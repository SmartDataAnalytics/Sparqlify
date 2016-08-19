package org.aksw.sparqlify.config.v0_2.bridge;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaper;
import org.aksw.sparqlify.core.sql.schema.Schema;
import org.aksw.sparqlify.core.sql.schema.SchemaImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class BasicTableInfo {
	private Map<String, String> rawTypeMap;
	private Set<String> nullableColumns;
	
	public BasicTableInfo(Map<String, String> rawTypeMap, Set<String> nullableColumns) {
		this.rawTypeMap = rawTypeMap;
		this.nullableColumns = nullableColumns;
	}

	public Map<String, String> getRawTypeMap() {
		return rawTypeMap;
	}

	public Set<String> getNullableColumns() {
		return nullableColumns;
	}
}


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
    private SqlEscaper sqlEscaper;

	public SchemaProviderImpl(Connection conn, TypeSystem datatypeSystem, Map<String, String> aliasMap, SqlEscaper sqlEscaper) {
		this.conn = conn;
		this.datatypeSystem = datatypeSystem;
		this.aliasMap = aliasMap;
		this.sqlEscaper = sqlEscaper;
	}
	
	
	
	public Schema createSchemaForRelationName(String tableName) {

		//Set<String> tableNameCache = JdbcUtils.fetchRelationNames(conn);

		
		// TODD We might have to escape table names...
		String escTableName = sqlEscaper.escapeTableName(tableName); //"\"" + tableName + "\"";
		
		// FIXME Use database metadata for fetching schemas of tables
		Schema result = createSchemaForQueryString("SELECT * FROM " + escTableName);

		return result;
	}
	
	
	public Schema createSchemaForQueryString(String queryString) {

		//String queryString = normalizeQueryString(rawQueryString);
		
		
		logger.info("Retrieving schema for query: " + queryString);
		
		logger.warn("Using ugly hack for adding a limit");
		if(!queryString.contains("LIMIT")) {
			queryString += " LIMIT 1";
		}

		//Map<String, TypeToken> typeMap = null;
		BasicTableInfo tableInfo;
		
		try {
			tableInfo = getRawTypes(conn, queryString);
			
			//typeMap = SchemaProviderImpl.getTypes(conn, queryString, datatypeSystem, aliasMap);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Map<String, String> rawTypeMap = tableInfo.getRawTypeMap();
		Set<String> nullableColumns = tableInfo.getNullableColumns();
		
		Map<String, TypeToken> typeMap = getTypes(rawTypeMap, datatypeSystem, rawTypeMap); 

		for(Entry<String, TypeToken> entry : typeMap.entrySet()) {
			logger.info(entry.getKey() + " -> " + entry.getValue());
		}
		
		// FIXME Preserve order of column names
		List<String> columnNames = new ArrayList<String>(typeMap.keySet());
		
		
		Schema result = new SchemaImpl(columnNames, typeMap, nullableColumns);
		
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


	public static BasicTableInfo getRawTypes(Connection conn, String queryStr)
			throws Exception
	{
		Map<String, String> rawTypeMap = new HashMap<String,String>();
		// TODO We need full column metadata after all I guess
		//Map<String, Integer> paddingMap = new HashMap<String, String>();
		Set<String> nullableColumns = new HashSet<String>();
		
		
		ResultSet rs = conn.createStatement().executeQuery(queryStr);
	
		try {
			ResultSetMetaData meta = rs.getMetaData();
			
			for(int i = 1; i <= meta.getColumnCount(); ++i) {
			    //String name = meta.getColumnName(i);

				String name = meta.getColumnLabel(i);
				int isNullable = meta.isNullable(i);
				
				if(isNullable != ResultSetMetaData.columnNoNulls) {
					nullableColumns.add(name);
				}
				
				//int type = meta.getColumnType(i);
				
				String typeName = meta.getColumnTypeName(i);
				//int displaySize = meta.getColumnDisplaySize(i);
				//System.out.println("TypeName: " + typeName);
			
				rawTypeMap.put(name, typeName);
			}
		} finally {
			rs.close();
		}

		/*
		System.out.println(SqlDatatypeBoolean.getInstance().hashCode());
		System.out.println(SqlDatatypeInteger.getInstance().hashCode());
		System.out.println(SqlDatatypeString.getInstance().hashCode());
		*/

		BasicTableInfo result = new BasicTableInfo(rawTypeMap, nullableColumns); 
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

	
	public static Map<String, TypeToken> getTypes(Map<String, String> rawTypeMap, TypeSystem datatypeSystem, Map<String, String> aliasMap)
	{
		Map<String, TypeToken> result = transformRawMap(rawTypeMap, datatypeSystem, aliasMap);
		return result;
	}



	@Override
	public TypeSystem getDatatypeSystem() {
		return datatypeSystem;
	}
}
