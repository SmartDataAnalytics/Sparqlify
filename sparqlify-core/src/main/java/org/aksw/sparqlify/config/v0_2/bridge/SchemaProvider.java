package org.aksw.sparqlify.config.v0_2.bridge;

import org.aksw.sparqlify.algebra.sql.nodes.Schema;

public interface SchemaProvider {
	Schema createSchemaForRelationName(String tableName);
	Schema createSchemaForQueryString(String queryString);
}
