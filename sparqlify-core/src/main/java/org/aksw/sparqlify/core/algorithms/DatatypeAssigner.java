package org.aksw.sparqlify.core.algorithms;

import java.util.Map;

import org.aksw.sparqlify.core.datatypes.XClass;

import com.hp.hpl.jena.sparql.expr.Expr;

public interface DatatypeAssigner {
	XClass assign(Expr expr, Map<String, XClass> typeMap);
}
