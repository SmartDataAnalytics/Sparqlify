package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.datatypes.XClass;

interface TypeResolver {
	XClass resolve(String typeName);
}