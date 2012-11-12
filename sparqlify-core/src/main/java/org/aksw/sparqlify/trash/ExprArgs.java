package org.aksw.sparqlify.trash;

import org.aksw.commons.util.reflect.MultiMethod;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;


/**
 * Class for retrieval of an expression's arguments
 * 
 * @author raven
 *
 */
public class ExprArgs {
	public static ExprList getArgs(Expr expr)
	{
		return (ExprList)MultiMethod.invokeStatic(ExprArgs.class, "_getArgs", expr);
	}
	
	public static ExprList createList(Iterable<Expr> args) {
		ExprList result = new ExprList();
		for(Expr arg : args) {
			result.add(arg);
		}
		
		return result;
	}

	public static ExprList create(Expr... args) {
		ExprList result = new ExprList();
		for(Expr arg : args) {
			result.add(arg);
		}
		
		return result;
	}

    public static ExprList _getArgs(Expr expr) {
    	return new ExprList();
    }

    public static ExprList _getArgs(ExprFunction expr) {
    	return new ExprList(expr.getArgs());
    }
}
