package org.aksw.sparqlify.core.domain.input;

import org.apache.jena.atlas.io.IndentedWriter;

public interface Indentable
{
	void asString(IndentedWriter writer);
}