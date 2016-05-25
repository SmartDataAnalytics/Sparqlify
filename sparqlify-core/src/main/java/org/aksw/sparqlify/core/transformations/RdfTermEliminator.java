package org.aksw.sparqlify.core.transformations;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.apache.jena.sparql.expr.Expr;

public interface RdfTermEliminator {
	E_RdfTerm _transform(Expr expr);
}
