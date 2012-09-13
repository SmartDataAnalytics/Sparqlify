package org.aksw.sparqlify.core;

import org.aksw.sparqlify.config.lang.PrefixSet;

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
