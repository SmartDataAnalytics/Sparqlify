package org.aksw.sparqlify.restriction;

/**
 * RDF term types
 * 
 * @author raven
 *
 */
public enum RdfTermType {
	UNKNOWN,
	BLANK,
	URI, // TODO This should probably be resource (i.e. uri + blank node)
	LITERAL,
	//PLAIN_LITERAL,
	//TYPED_LITERAL,
}