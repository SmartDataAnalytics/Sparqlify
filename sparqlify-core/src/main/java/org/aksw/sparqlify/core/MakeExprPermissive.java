package org.aksw.sparqlify.core;


import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.trash.ExprCopy;

import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;

/**
 * We replace the original StrConcat with a custom version,
 * as it causes trouble when mixing types.
 * 
 * 
 * 
 * @author raven
 *
 */
public class MakeExprPermissive
	extends ExprCopy
{
	private static ExprCopy instance;
	
	public static ExprCopy getInstance()
	{
		if(instance == null) {
			instance = new MakeExprPermissive();
		}
		return instance;
	} 
	
//	public static Expr _copy(E_StrConcat func, ExprList args) {
//		return new E_StrConcatPermissive(args);
//	}
	
	@Override
	public  Expr deepCopy(ExprFunction proto, List<Expr> args) {
		Expr result;
		
		if(proto instanceof E_StrConcat) {
			result = new E_StrConcatPermissive(new ExprList(args));
		} else {
			result = super.deepCopy(proto, args);
		}
		
		return result;
	}
}
