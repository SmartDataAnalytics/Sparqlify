package org.aksw.sparqlify.config.v0_2.bridge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.r2rml.jena.sql.transform.SqlParseException;
import org.aksw.r2rml.sql.transform.SqlUtils;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.sql.schema.Schema;
import org.aksw.sparqlify.core.sql.schema.SchemaImpl;
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

	//protected Connection conn;
	protected BasicTableInfoProvider basicTableInfoProvider;
	protected TypeSystem datatypeSystem;
	protected Map<String, String> aliasMap; // TODO Maybe this has to be a function to capture int([0-9]*) -> int
	// Encoding for generation of SQL queries
	protected SqlCodec downstreamSqlEncoder;
	
	// Encoder for encoding the obtained column/table names
	// Upstream encoding is not (yet?) needed: we don't use
	// any qualified table names and columns obtained from jdbc metadata
	// protected SqlCodec upstreamSqlEncoder;

	public SchemaProviderImpl(
			BasicTableInfoProvider basicTableInfoProvider,
			TypeSystem datatypeSystem,
			Map<String, String> aliasMap,
			SqlCodec downstreamSqlEncoder) {
		// this(basicTableInfoProvider, datatypeSystem, aliasMap, downstreamSqlEncoder, SqlCodecUtils.createSqlCodecDefault());
		this.basicTableInfoProvider = basicTableInfoProvider;
		this.datatypeSystem = datatypeSystem;
		this.aliasMap = aliasMap;
		this.downstreamSqlEncoder = downstreamSqlEncoder;
	}

/*
	public SchemaProviderImpl(
			BasicTableInfoProvider basicTableInfoProvider,
			TypeSystem datatypeSystem,
			Map<String, String> aliasMap,
			SqlCodec downstreamSqlEncoder,
			SqlCodec upstreamSqlEncoder) {
		//this.conn = conn;
		this.basicTableInfoProvider = basicTableInfoProvider;
		this.datatypeSystem = datatypeSystem;
		this.aliasMap = aliasMap;
		this.downstreamSqlEncoder = downstreamSqlEncoder;
		// this.upstreamSqlEncoder = upstreamSqlEncoder;
	}
*/


	public Schema createSchemaForRelationName(String tableName) {

		//Set<String> tableNameCache = JdbcUtils.fetchRelationNames(conn);


		// TODD We might have to escape table names...
		// String escTableName = sqlEscaper.escapeTableName(tableName); //"\"" + tableName + "\"";
		String escTableName;
		try {
			escTableName = SqlUtils.reencodeTableNameDefault(tableName, downstreamSqlEncoder);
			// escTableName = SqlUtils.harmonizeTableName(tableName, downstreamSqlEncoder);
		} catch (SqlParseException e) {
			throw new RuntimeException(e);
		}

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
			tableInfo = basicTableInfoProvider.getBasicTableInfo(queryString); //getRawTypes(conn, queryString);

			//typeMap = SchemaProviderImpl.getTypes(conn, queryString, datatypeSystem, aliasMap);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		Map<String, String> tmpTypeMap = tableInfo.getRawTypeMap();
		
		// TODO Add a preprocessing step to allow configure remapping of datatypes reported by the jdbc driver
		Map<String, String> rawTypeMap = tmpTypeMap.entrySet().stream()
				.collect(Collectors.toMap(
						Entry::getKey,
						// e -> upstreamSqlEncoder.forColumnName().encode(e.getKey()),
						e -> e.getValue().equalsIgnoreCase("serial") ? "integer" : e.getValue()));
		
		
		Set<String> nullableColumns = tableInfo.getNullableColumns().stream()
				//.map(upstreamSqlEncoder.forColumnName()::encode)
				.collect(Collectors.toCollection(LinkedHashSet::new));

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
		Map<String, TypeToken> result = new LinkedHashMap<String, TypeToken>();

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
