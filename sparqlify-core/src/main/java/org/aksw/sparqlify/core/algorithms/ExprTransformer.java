package org.aksw.sparqlify.core.algorithms;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;

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
	Expr transform(ExprFunction fn);
}
