package org.aksw.sparqlify.algebra.sql.exprs.evaluators;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;

import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.XSDFuncOp;

/**
 * Process for finding the appropriate op:
 * 
 * Given a set of expressions whose datatype is known, do the following:
 * . a) check if for the opName there exists a method that accepts the given arguments.
 * . b) if so, check if it has an evaluator attached (this is this object)
 * . c) check whether the given arguments datatypes are valid according to a datatype system
 * . d) if all arguments are constants, we can also use Jena to evaluate the expression 
 *      
 * 
 * ISSUE: step A requires the OpName to be defined with a signature
 * However, only in step c this is enforced.
 * 
 * So here is the master plan: The point of the "transformation" is to get
 * rid of all the RDF-term expressions, and instead reduce them to
 * operations on conventional datatypes.
 * 
 * As such, there are no RDF terms on the SqlExpr level.
 * 
 * 
 *  
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlExprEvaluator_Arithmetic
	extends SqlExprEvaluator2
{

	@Override
	public SqlExpr eval(SqlExpr a, SqlExpr b) {
		
//		if(a.isConstant()) {
//			NodeValue x = a.asConstant().getValue();
//			
//			if(b.isConstant()) {
//				NodeValue y = b.asConstant().getValue();
//				
//				XSDFuncOp.add(x, y);
//			}
//			
//			
//		}
		return null;
	}

}
