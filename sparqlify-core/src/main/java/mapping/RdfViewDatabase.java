package mapping;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.sparqlify.core.DatatypeSystemDefault;
import org.aksw.sparqlify.core.SqlDatatype;


/**
 * 
 * I want to keep the view system separate from the part that
 * actually requires a database connection
 * 
 * @author raven
 *
 */
public class RdfViewDatabase {
	/*
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}*/
	
	private static Map<String, SqlDatatype> rawTypeToSql = new HashMap<String, SqlDatatype>();
	
	static {
		// HACK - Not sure what to do with custom enums...
		rawTypeToSql.put("osmentitytype", DatatypeSystemDefault._STRING);

		rawTypeToSql.put("bool", DatatypeSystemDefault._BOOLEAN);
		rawTypeToSql.put("int2", DatatypeSystemDefault._INTEGER);
		rawTypeToSql.put("int4", DatatypeSystemDefault._INTEGER);
		rawTypeToSql.put("int8", DatatypeSystemDefault._INTEGER);

		rawTypeToSql.put("varchar", DatatypeSystemDefault._STRING);
		
		rawTypeToSql.put("text", DatatypeSystemDefault._STRING);
		rawTypeToSql.put("bpchar", DatatypeSystemDefault._STRING);
		rawTypeToSql.put("float4", DatatypeSystemDefault._FLOAT);
		rawTypeToSql.put("float8", DatatypeSystemDefault._DOUBLE);
		rawTypeToSql.put("float", DatatypeSystemDefault._DOUBLE);
		

		rawTypeToSql.put("date", DatatypeSystemDefault._DATE_TIME);
		rawTypeToSql.put("timestamp", DatatypeSystemDefault._DATE_TIME);
		
		
		rawTypeToSql.put("geography", DatatypeSystemDefault._GEOGRAPHY);
		rawTypeToSql.put("geometry", DatatypeSystemDefault._GEOMETRY);
	}
	
	
	
	public static final Pattern explainCostPattern = Pattern.compile("cost=(\\d+(\\.\\d+)?)\\.\\.(\\d+(\\.\\d+)?)");
	public static double getCostPostgres(Connection conn, String query)
		throws SQLException
	{
		String q = "EXPLAIN " + query;
		
		ResultSet rs = null;
		try {
			rs = conn.createStatement().executeQuery(q);			
			ResultSetMetaData meta = rs.getMetaData();
			
			// We only need the first line
			rs.next();
			
			
			for(int i = 0; i < meta.getColumnCount(); ++i) {
				String str = rs.getObject(i + 1).toString();
				
				Matcher matcher = explainCostPattern.matcher(str);
				matcher.find();

				String maxCostStr = matcher.group(3);				
				Double maxCost = Double.parseDouble(maxCostStr);

				return maxCost;
			}
		} finally {
			if(rs != null) {
				rs.close();
			}
		}
		
		throw new RuntimeException("Could not determine cost of the query: " + query);
	}

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

	public static SqlDatatype asDatatype(String id) {
		SqlDatatype result = rawTypeToSql.get(id.toLowerCase());
		if(result == null) {
			throw new RuntimeException("Raw SQL datatype '" + id + "' not mapped");			
		}
		
		return result;
	}
	
	
	public static Map<String, SqlDatatype> transformRawMap(Map<String, String> map) {
		Map<String, SqlDatatype> result = new HashMap<String, SqlDatatype>();

		for(Map.Entry<String, String> entry : map.entrySet()) {
			SqlDatatype value = asDatatype(entry.getValue());
			
			result.put(entry.getKey(), value);
		}
		return result;
	}
	
	public static Map<String, SqlDatatype> getTypes(Connection conn, String queryStr)
		throws Exception
	{
		Map<String, String> map = getRawTypes(conn, queryStr);
		Map<String, SqlDatatype> result = transformRawMap(map);
		return result;
	}
}
