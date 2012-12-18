package org.aksw.sparqlify.core.algorithms;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

/**
 * The purpose of the ExprTransformer is to transform an expression
 * in order to get rid of rdf-terms.
 * Example:
 * 
 * &lt;person&gt; &gt; 5 -&gt;
 *     rdfterm(1, "person", "", "") > rdfterm(2, 5, "", "") -&gt;
 *     1 = 2 && "person" = 5 && "" = "" && "" = "" -&gt;
 *     FALSE
 * 
 * 
 * @author raven
 *
 */
public interface ExprTransformer {
	Expr transform(ExprFunction fn);
}
