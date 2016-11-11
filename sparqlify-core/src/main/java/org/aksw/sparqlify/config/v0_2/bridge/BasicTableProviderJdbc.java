package org.aksw.sparqlify.config.v0_2.bridge;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BasicTableProviderJdbc
	implements BasicTableInfoProvider
{
	protected Connection conn;

	public BasicTableProviderJdbc(Connection conn) {
		super();
		this.conn = conn;
	}

	@Override
	public BasicTableInfo getBasicTableInfo(String queryStr) {
		BasicTableInfo result;
		try {
			result = getRawTypes(conn, queryStr);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		return result;
	}

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
}
