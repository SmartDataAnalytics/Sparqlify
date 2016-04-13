package org.aksw.sparqlify.core.transformations;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.trash.ExprCopy;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.Expr;

/**
 * Expr transformer for rdfTerm expressions:
 * the value field of such expressions is passed through
 * 
 * Expects that the original expression is of type E_RdfTerm!
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ExprTransformerPassAsTypedLiteral
	extends ExprTransformerBase1
{	
	private Resource resultType;
	
	public ExprTransformerPassAsTypedLiteral(Resource resultType) {
		this.resultType = resultType;
	}

	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm expr) {
		/*
		List<Expr> args = fn.getArgs();
		List<Expr> newArgs = new ArrayList<Expr>(args.size());
		
		for(Expr arg : args) {
			Expr newArg = SqlTranslationUtils.getLexicalValueOrExpr(arg);
			newArgs.add(newArg);
		}
		*/
		Expr arg = expr.getLexicalValue();
		
		Expr tmp = ExprCopy.getInstance().copy(orig, arg);
		
		E_RdfTerm result = E_RdfTerm.createTypedLiteral(tmp, resultType);
		
		//E_RdfTerm result = SqlTranslationUtils.expandAnyToTerm(tmp);
		
		//E_RdfTerm result = (E_RdfTerm)tmp;
		
		return result;
	}
}