package org.aksw.sparqlify.core.transformations;

import java.util.List;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.apache.jena.sparql.expr.Expr;

/**
 * The purpose of the ExprTransformer is to transform an expression
 * in order to get rid of rdf-terms.
 * Example:
 * 
 * &lt;person&gt; &gt; 5 -&gt;
 *     rdfterm(1, "person", "", "") > rdfterm(2, 5, "", "") -&gt;
 *     rdfterm(3, 1 = 2 && "person" = 5 && "" = "" && "" = "" -&gt, "", "");
 *     rdfterm(3, false)
 *     FALSE
 * 
 * TODO: Actually, a transformer always has to return an E_RdfTerm expression
 * 
 * @author raven
 *
 */
public interface ExprTransformer {
	E_RdfTerm transform(Expr orig, List<E_RdfTerm> exprs);
}
