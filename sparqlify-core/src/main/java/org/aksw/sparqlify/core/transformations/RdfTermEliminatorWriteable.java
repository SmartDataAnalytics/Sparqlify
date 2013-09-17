package org.aksw.sparqlify.core.transformations;

public interface RdfTermEliminatorWriteable
	extends RdfTermEliminator
{
	void register(String functionSymbol, ExprTransformer exprTransformer);
}