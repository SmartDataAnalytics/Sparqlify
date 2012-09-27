package org.aksw.sparqlify.algebra.generic.exprs;

import java.util.List;

/**
 * Base class for expressions.
 * "T" corresponds to the type of the leaf nodes
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 * @param <T>
 */
public interface G_Expr<T> {

	int getArgCount();
	G_Expr<T> getArg(int index);
	
	List<G_Expr<T>> getArgs();
	
	G_Expr<T> copy(List<G_Expr<T>> args);
}
