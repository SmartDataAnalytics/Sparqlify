package org.aksw.sparqlify.core.algorithms;

import java.util.List;

import org.aksw.commons.factory.Factory2;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.core.SparqlifyConstants;

import com.hp.hpl.jena.sparql.expr.E_Conditional;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.NodeValue;

/**
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */

public class ExprTransformerRdfTermComparator
	implements ExprTransformer
{
	private ExprEvaluator exprEvaluator;
	
	public ExprTransformerRdfTermComparator(ExprEvaluator exprEvaluator) {
		this.exprEvaluator = exprEvaluator;
	}
	
	public Expr handleConcat(ExprFunction fn) {
		
		// The result is null if it could not be further transformed
		Expr result = SqlTranslationUtils.optimizeOpConcat(fn);
		
		if(result == null) {
			result = fn;
		}
		
		return result;
	}

	@Override
	public Expr transform(ExprFunction fn) {
	
		Expr result = null;
		
		List<Expr> exprs = fn.getArgs();
		
		Expr left = exprs.get(0);
		Expr right = exprs.get(1);
		
				
		E_RdfTerm leftTerm = SqlTranslationUtils.expandRdfTerm(left);
		E_RdfTerm rightTerm = SqlTranslationUtils.expandRdfTerm(right);
		
		
		// TODO: The following condition breaks if we have two constants or
		// variable and constant. E.g. <http://some.uri> > 5
		
		// If none of the arguments is a E_rdfTerm, continue with further checks

		if(leftTerm == null && rightTerm == null) {
			
			Expr tmp = handleConcat(fn);
			
			return tmp;
		}

		
		// However, if one of the arguments is one, transform
		if(leftTerm == null) {
			leftTerm = SqlTranslationUtils.expandConstant(left);
		}
		
		if(rightTerm == null) {
			rightTerm = SqlTranslationUtils.expandConstant(right);
		}
	
		Factory2<Expr> exprFactory = ExprFactoryUtils.createCopyFactory2((ExprFunction2)fn);
		
		if(leftTerm != null && rightTerm != null) {
			
			result = processOpRdfTerm(leftTerm, rightTerm, exprFactory);
			
		} else {
			
			throw new RuntimeException("Should not happen: " + fn);
		}
		
		
		
		return result;
	}
	
	
	public Expr processOpRdfTerm(E_RdfTerm a, E_RdfTerm b, Factory2<Expr> opFactory) {
		
		Expr result;
		
		// Condition: Either the types are equal or both are either 2 (plain) or 3 (typed literal)
		
		Expr eqTmpA = new E_Equals(a.getType(), b.getType());
		
		Expr e =
			new E_LogicalAnd(
				new E_LogicalOr(
					new E_Equals(a.getType(), NodeValue.makeDecimal(2)),
					new E_Equals(a.getType(), NodeValue.makeDecimal(3))
				),
				new E_LogicalOr(
						new E_Equals(b.getType(), NodeValue.makeDecimal(2)),
						new E_Equals(b.getType(), NodeValue.makeDecimal(3))
				)
			);
					
				
		Expr eqTA = new E_LogicalOr(eqTmpA, e);
		
		
		E_Conditional eqT = new E_Conditional(eqTA, NodeValue.TRUE, SparqlifyConstants.nvTypeError);

		
		//Expr eqV = new E_Equals(a.getLexicalValue(), b.getLexicalValue());
		
		Expr tmpEqV = opFactory.create(a.getLexicalValue(), b.getLexicalValue());
		Expr eqV = transform((ExprFunction2)tmpEqV);
		
		
		// TODO We need to consider type hierarchies, but for now we just skip on that.
		Expr eqD = NodeValue.TRUE;
		//Expr eqD = new E_Equals(a.getDatatype(), b.getDatatype());
		Expr eqL = new E_Equals(a.getLanguageTag(), b.getLanguageTag());
		
		Expr tmp =
				new E_LogicalAnd(
						new E_LogicalAnd(eqT, eqV),
						new E_LogicalAnd(eqD, eqL)
				);
		
		result = exprEvaluator.eval(tmp, null);

		return result;
		
	}
}