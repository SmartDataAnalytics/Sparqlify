package org.aksw.sparqlify.core.interfaces;

import com.hp.hpl.jena.sparql.expr.Expr;

public interface SqlExprSerializer {
	String serialize(Expr expr);
}
