package org.aksw.sparqlify.config.v0_2.bridge;

import java.util.Map;

import org.aksw.sparqlify.algebra.sql.nodes.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SchemaImpl;
import org.aksw.sparqlify.core.cast.TypeSystem;


/**
 * Dummy schema provider used e.g. for the R2RML export.
 * 
 * @author patrick
 */
public class SchemaProviderDummy implements SchemaProvider {
	private TypeSystem datatypeSystem;
	private Map<String, String> aliasMap;
	
	public SchemaProviderDummy(TypeSystem datatypeSystem, Map<String, String> aliasMap) {
		this.datatypeSystem = datatypeSystem;
		this.aliasMap = aliasMap;
	}

	@Override
	public Schema createSchemaForRelationName(String tableName) {
		return new SchemaImpl();
	}

	@Override
	public Schema createSchemaForQueryString(String queryString) {
		return new SchemaImpl();
	}

	@Override
	public TypeSystem getDatatypeSystem() {
		// TODO Auto-generated method stub
		return datatypeSystem;
	}

}
