package org.aksw.sparqlify.config.v0_2.bridge;

import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.sql.schema.Schema;

public interface SchemaProvider {
	Schema createSchemaForRelationName(String tableName);
	Schema createSchemaForQueryString(String queryString);
	
	TypeSystem getDatatypeSystem();
}
