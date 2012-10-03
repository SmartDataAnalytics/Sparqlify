package org.aksw.sparqlify.core.algorithms;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

public interface ExprTransformer {
	Expr transform(ExprFunction fn);
}
