package org.aksw.sparqlify.algebra.sql.exprs2;

import com.hp.hpl.jena.sparql.expr.NodeValue;


public interface SqlExprConstant
	extends SqlExpr
{
	NodeValue getValue();
	//<T> T getValue();
}
