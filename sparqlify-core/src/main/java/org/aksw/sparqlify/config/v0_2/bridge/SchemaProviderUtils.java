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

import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SchemaImpl;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SchemaProviderUtils {
    private static final Logger logger = LoggerFactory.getLogger(SchemaProviderUtils.class);

    public static Schema createSchemaForQueryString(Connection conn, String queryString, TypeSystem typeSystem) {
        logger.debug("Starting retrieval of schema for query: " + queryString);

        //Map<String, TypeToken> typeMap = null;
        BasicTableInfo tableInfo;

        try {
            tableInfo = getRawTypes(conn, queryString);

            //typeMap = SchemaProviderImpl.getTypes(conn, queryString, typeSystem, aliasMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Map<String, String> rawTypeMap = tableInfo.getRawTypeMap();
        Set<String> nullableColumns = tableInfo.getNullableColumns();

        Map<String, TypeToken> typeMap = getTypes(rawTypeMap, typeSystem, rawTypeMap);

        for(Entry<String, TypeToken> entry : typeMap.entrySet()) {
            logger.info(entry.getKey() + " -> " + entry.getValue());
        }

        // FIXME Preserve order of column names
        List<String> columnNames = new ArrayList<String>(typeMap.keySet());


        Schema result = new SchemaImpl(columnNames, typeMap, nullableColumns);

        logger.debug("Finished retrieval of schema for query: " + queryString);
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


        BasicTableInfo result = new BasicTableInfo(rawTypeMap, nullableColumns);
        return result;
    }

    public static Map<String, TypeToken> getTypes(Map<String, String> rawTypeMap, TypeSystem typeSystem, Map<String, String> aliasMap)
    {
        Map<String, TypeToken> result = transformRawMap(rawTypeMap, typeSystem, aliasMap);
        return result;
    }

    public static Map<String, TypeToken> transformRawMap(Map<String, String> map, TypeSystem typeSystem, Map<String, String> aliasMap) {
        Map<String, TypeToken> result = new HashMap<String, TypeToken>();

        for(Map.Entry<String, String> entry : map.entrySet()) {
            // TODO Get a datatype object that can convert SQL Values between NodeValues and vice versa.
            // Problem: Can we have a NodeValue with datatype but Null string?


            //XClass clazz = lookupDatatype(entry.getValue(), typeSystem, aliasMap);
            //result.put(entry.getKey(), clazz.getToken());

            TypeToken typeToken = TypeToken.alloc(entry.getValue());
            result.put(entry.getKey(), typeToken);

        }

        return result;
    }

}
