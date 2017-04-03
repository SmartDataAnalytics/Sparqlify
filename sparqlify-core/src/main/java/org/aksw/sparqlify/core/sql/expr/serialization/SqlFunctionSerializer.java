package org.aksw.sparqlify.core.sql.expr.serialization;

import java.util.List;

/**
 * Serializer for SQL functions.
 * It is assumed that the arguments are properly escaped and encoded.
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public interface SqlFunctionSerializer {
	String serialize(List<String> args);
}
