package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;

public interface SqlExprSerializerSystem
	extends SqlExprSerializer
{
	void addSerializer(String functionName, SqlFunctionSerializer serializer);
}
