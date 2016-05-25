package org.aksw.sparqlify.core.transformations;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.ExprCopy;
import org.aksw.jena_sparql_api.views.SparqlifyConstants;
import org.aksw.jena_sparql_api.views.SqlTranslationUtils;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */

public class ExprTransformerArithmetic
	extends ExprTransformerBase2
{
	private Resource resultType;
	
	public ExprTransformerArithmetic(Resource resultType) {
		this.resultType = resultType;
	}

	
	public Expr handleConcat(ExprFunction fn) {
		
		// The result is null if it could not be further transformed
		Expr result = SqlTranslationUtils.optimizeOpConcat(fn);
		
		/*
		if(result == null) {
			result = fn;
		}*/
		
		return result;
	}

	
	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a, E_RdfTerm b) {
		
		// Create an expression base on the arguments
		// using the type of the original expression
		
		Expr isCompatibleTypes = ExprTransformerRdfTermComparator.createTypeCheck(a, b);
		
		E_RdfTerm result;
		if(isCompatibleTypes.equals(SparqlifyConstants.nvTypeError)) {
			result = SqlTranslationUtils.expandConstant(SparqlifyConstants.nvTypeError);
		}
		else {
			
			Expr va = a.getLexicalValue();
			Expr vb = b.getLexicalValue();
			Expr ex = ExprCopy.getInstance().copy(orig, va, vb);
			
			if(isCompatibleTypes.equals(NodeValue.TRUE)) {
				result = E_RdfTerm.createTypedLiteral(ex, resultType);
			}
			else {
				Expr cond = new E_Conditional(isCompatibleTypes, ex, SparqlifyConstants.nvTypeError);
				result = E_RdfTerm.createTypedLiteral(cond, resultType);
			}
			
		}

		return result;
	}
	
	public static final NodeValue two = NodeValue.makeDecimal(2);
	public static final NodeValue three = NodeValue.makeDecimal(3);
	
	public static Expr createTypeCheck(E_RdfTerm a, E_RdfTerm b) {
		
		// Condition: Either the types are equal or both are either 2 (plain) or 3 (typed literal)
		
		// TODO Already evaluate here as far as possible - Otherwise the expressions get annoyingly complicated
		Expr at = a.getType();
		Expr bt = b.getType();
		
		
		Expr result;
		
		
		if(at.isConstant() && bt.isConstant()) {
			NodeValue x = at.getConstant();
			NodeValue y = at.getConstant();
			
			boolean isSameValue = NodeValue.sameAs(x, y);

			boolean isBothLiterals = 
					(NodeValue.sameAs(x, two) || NodeValue.sameAs(x, three)) &&
					(NodeValue.sameAs(y, two) || NodeValue.sameAs(y, three));

			boolean isCompatibleTypes = isSameValue || isBothLiterals;
			
			if(isCompatibleTypes) {
				result = NodeValue.TRUE;
			} else {
				result = SparqlifyConstants.nvTypeError;
			}

			
		} else {
		
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
			result = new E_Conditional(eqTA, NodeValue.TRUE, SparqlifyConstants.nvTypeError);
		
		}
		

		return result;
	}
	
	public static E_RdfTerm processOpRdfTerm(E_RdfTerm a, E_RdfTerm b, Expr innerExpr, Resource resultType) {
		
		
		// Condition: Either the types are equal or both are either 2 (plain) or 3 (typed literal)
		

		Expr eqT = createTypeCheck(a, b);

		
		//Expr eqV = new E_Equals(a.getLexicalValue(), b.getLexicalValue());
		
		Expr eqV = innerExpr;
		
		
		// TODO We need to consider type hierarchies, but for now we just skip on that.
		Expr eqD = NodeValue.TRUE;
		//Expr eqD = new E_Equals(a.getDatatype(), b.getDatatype());
		Expr eqL = new E_Equals(a.getLanguageTag(), b.getLanguageTag());
		
		Expr tmp =
				new E_LogicalAnd(
						new E_LogicalAnd(eqT, eqV),
						new E_LogicalAnd(eqD, eqL)
				);
		
		E_RdfTerm result = E_RdfTerm.createTypedLiteral(tmp, resultType);

		return result;
		
	}
}