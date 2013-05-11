package org.aksw.sparqlify.core.cast;

import java.util.Collection;

import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;

public interface SqlExprSerializerSystem
	extends SqlExprSerializer
{
	void addSerializer(String functionId, SqlFunctionSerializer serializer);
	
	/**
	 * Add serializers for a collection of functionIds
	 * 
	 * @param functionNames
	 * @param serializer
	 */
	void addSerializer(Collection<String> functionIds, SqlFunctionSerializer serializer);
}
