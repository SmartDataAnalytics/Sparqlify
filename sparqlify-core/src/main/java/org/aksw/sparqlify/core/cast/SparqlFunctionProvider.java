package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.datatypes.SparqlFunction;

public interface SparqlFunctionProvider {
	SparqlFunction getSparqlFunction(String name);
}
