package org.aksw.sparqlify.algebra.sql.exprs;

import java.util.List;

public interface SqlStringTransformer
{
	String transform(S_Function function, List<String> args);
}