package org.aksw.sparqlify.core.transformations;

import java.util.List;

import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class ExprTransformerLogicalAndDontUse
	//implements ExprTransformer
{
	
	public static NodeValue min(NodeValue a, NodeValue b) {
		
		if(a.equals(NodeValue.nvNothing)) {
			if(!(b.equals(NodeValue.FALSE))) {
				return a;
			} else {
				return b;
			}
			
		} else if(a.equals(NodeValue.FALSE)) {

			return a;

		} else {
			// If a is true, the result solely depends on b
			return b;
		}

	}
	
	/**
	 * Return the "lower" constant with:
	 * 0 < e < 1
	 * 
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static Expr getLowerConstant(Expr a, Expr b) {
		NodeValue result = null; 
		
		if(a.isConstant()) {
			result = a.getConstant();
		}
		
		if(b.isConstant()) {
			
			NodeValue bValue = b.getConstant();
			
			if(result != null) {
				result = min(result, bValue); 
			} else {
				result = bValue;
			}
		}
		
		return result;
	}
	
	//@Override
	public Expr transform(ExprFunction fn) {
		
		List<Expr> args = fn.getArgs();
		if(args.size() != 2) {
			throw new RuntimeException("Invalid number of arguments; 1 expected, got: " + fn);
		}
		
		// FIXME Check whether lang has the right number of argument
		Expr left = args.get(0);
		Expr right = args.get(1);
				
		
		Expr tmp = getLowerConstant(left, right);
		// If there is no constant, just return
		if(tmp == null ) {
			return fn;
		}
		
		if(tmp.equals(NodeValue.FALSE)) {
			return NodeValue.FALSE;
		}
				
		Expr fnArg = left.isFunction() ? left : right;
		if(fnArg.isFunction()) {
			
			if(tmp.equals(NodeValue.TRUE)) {
				return fnArg;
			} else {
				return new E_LogicalAnd(tmp, fnArg);
			}
		} else {
			return tmp;
		}
			
	}
}
