package org.aksw.sparqlify.config.v0_2.bridge;

import java.sql.Connection;

import org.aksw.sparqlify.algebra.sql.nodes.Schema;
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
public class SchemaProviderOracle
    implements SchemaProvider
{
    private static final Logger logger = LoggerFactory.getLogger(SchemaProviderOracle.class);

    private Connection conn;
    private TypeSystem typeSystem;

    public SchemaProviderOracle(Connection conn, TypeSystem typeSystem) {
        this.conn = conn;
        this.typeSystem = typeSystem;
    }

    public Schema createSchemaForRelationName(String tableName) {
        // TODO We might have to escape table names...
        String escTableName = "\"" + tableName + "\"";

        // FIXME Use database metadata for fetching schemas of tables
        String queryString = "SELECT * FROM " + escTableName + " WHERE ROWNUM <= 1";
        Schema result = SchemaProviderUtils.createSchemaForQueryString(conn, queryString, typeSystem);

        return result;
    }

    public Schema createSchemaForQueryString(String queryString) {
        queryString = "SELECT * FROM (" + queryString + ") WHERE ROWNUM <= 1";

        Schema result = SchemaProviderUtils.createSchemaForQueryString(conn, queryString, typeSystem);
        return result;
    }


    @Override
    public TypeSystem getDatatypeSystem() {
        return typeSystem;
    }
}
