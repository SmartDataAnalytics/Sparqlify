package org.aksw.sparqlify.config.v0_2.bridge;

import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.core.datatypes.TypeSystem;

public interface SchemaProvider {
	Schema createSchemaForRelationName(String tableName);
	Schema createSchemaForQueryString(String queryString);
	
	TypeSystem getDatatypeSystem();
}
