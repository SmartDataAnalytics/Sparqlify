package org.aksw.sparqlify.core.algorithms;

import java.util.function.UnaryOperator;

import org.aksw.sparqlify.core.TypeToken;

/**
 * Interface to request a serializer of values of a certain datatype.
 * 
 * @author raven
 *
 */
public interface DatatypeToString
{
	public UnaryOperator<String> asString(TypeToken datatype);
}