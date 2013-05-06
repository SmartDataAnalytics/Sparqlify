package org.aksw.sparqlify.algebra.sql.exprs2;

/**
 * A visitor interface (actually just a dummy)
 * 
 * However, I don't like this. We usually just need to know whether something
 * is a function, columnRef or literal
 * 
 * So for this we hardly need a full blown visitor for all possible leaf nodes
 * 
 * @author raven
 *
 * @param <T>
 */
public interface SqlExprVisitor<T> {
	
	/**
	 * Fallback method for all expressions not captured by this visitor 
	 * 
	 * @param expr
	 * @return
	 */
	T visit(SqlExpr expr);
}
