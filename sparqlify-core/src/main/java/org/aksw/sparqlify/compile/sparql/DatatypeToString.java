package org.aksw.sparqlify.compile.sparql;

import org.aksw.commons.factory.Factory1;
import org.aksw.sparqlify.core.SqlDatatype;

public interface DatatypeToString
{
    public Factory1<String> asString(SqlDatatype datatype);
}