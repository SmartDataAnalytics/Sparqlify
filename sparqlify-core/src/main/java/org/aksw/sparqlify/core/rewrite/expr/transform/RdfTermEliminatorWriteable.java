package org.aksw.sparqlify.core.rewrite.expr.transform;

public interface RdfTermEliminatorWriteable
	extends RdfTermEliminator
{
	void register(String functionSymbol, ExprTransformer exprTransformer);
}