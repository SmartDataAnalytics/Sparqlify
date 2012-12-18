package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

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
