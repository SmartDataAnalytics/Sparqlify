package org.aksw.sparqlify.core.cast;

public interface SqlValueTransformer {
	SqlValue transform(SqlValue nodeValue) throws CastException;
}