package org.aksw.sparqlify.restriction;

public enum Type {
	UNKNOWN,
	//BLANK,
	URI, // TODO This should probably be resource (i.e. uri + blank node)
	LITERAL,
	//PLAIN_LITERAL,
	//TYPED_LITERAL,
}