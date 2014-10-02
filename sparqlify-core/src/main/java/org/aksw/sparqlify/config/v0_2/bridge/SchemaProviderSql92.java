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
public class SchemaProviderSql92
    implements SchemaProvider
{
    private static final Logger logger = LoggerFactory.getLogger(SchemaProviderSql92.class);

    private Connection conn;
    private TypeSystem typeSystem;

    public SchemaProviderSql92(Connection conn, TypeSystem typeSystem) {
        this.conn = conn;
        this.typeSystem = typeSystem;
    }

    public Schema createSchemaForRelationName(String tableName) {
        // TODO We might have to escape table names...
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

        Schema result = SchemaProviderUtils.createSchemaForQueryString(conn, queryString, typeSystem);
        return result;
    }


    @Override
    public TypeSystem getDatatypeSystem() {
        return typeSystem;
    }
}
