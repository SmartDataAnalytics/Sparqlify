package org.aksw.sparqlify.core;

import org.aksw.jena_sparql_api.views.PrefixSet;

public class RdfTermPrefix
    extends RdfTerm<PrefixSet>
{
    public RdfTermPrefix() {
        super();
    }

    public RdfTermPrefix(PrefixSet type, PrefixSet value, PrefixSet language,
            PrefixSet datatype) {
        super(type, value, language, datatype);
    }
}
