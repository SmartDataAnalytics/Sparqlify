package org.aksw.sparqlify.core.algorithms;

import org.aksw.commons.util.factory.Factory1;
import org.aksw.sparqlify.core.TypeToken;

public interface TypeSerializer
{
    public Factory1<String> asString(TypeToken datatype);
}