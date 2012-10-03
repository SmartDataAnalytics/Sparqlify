package org.aksw.sparqlify.core.algorithms;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

/**
 *          +
 *        /    \
 *      -        +
 *    /   \     /  \
 *   a     ?x  ?y   d 
 * 
 * 
 * a + d - x + y
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
class ArithmeticTransformer
	implements ExprTransformer
{

	private String op1 = "+";
	private String op2 = "-";
	
	@Override
	public Expr transform(ExprFunction fn) {
		// TODO Auto-generated method stub
		return null;
	}

}