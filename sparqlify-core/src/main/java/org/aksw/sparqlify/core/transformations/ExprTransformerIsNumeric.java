package org.aksw.sparqlify.core.transformations;

import java.util.ArrayList;
import java.util.List;

import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.sparqlify.type_system.TypeModel;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.vocabulary.XSD;


/**
 * Checks whether xsd:numeric is a super class of the given expression
 * 
 * 
 * @author raven
 *
 */
public class ExprTransformerIsNumeric
	extends ExprTransformerBase1
{
	//private static final Logger logger = LoggerFactory.getLogger(ExprTransformerSparqlFunctionModel.class);
	

	private TypeModel<String> typeModel;

	
	public ExprTransformerIsNumeric(TypeModel<String> typeModel) {
		this.typeModel = typeModel;
	}

	
	/**
	 * TODO: IsNumeric implies not null
	 * 
	 */
	@Override
	public E_RdfTerm transform(Expr orig, E_RdfTerm a) {

		String subType = a.getDatatype().getConstant().asUnquotedString();

		boolean isDecimal = typeModel.isSuperTypeOf(XSD.decimal.toString(), subType);
		boolean isFloat = typeModel.isSuperTypeOf(XSD.xfloat.toString(), subType);
		boolean isDouble = typeModel.isSuperTypeOf(XSD.xdouble.toString(), subType);
		
		boolean isNumeric = isDecimal || isFloat || isDouble;
		
		E_RdfTerm result;
		if(isNumeric) {
			result = E_RdfTerm.TRUE;
		}
		else {
			result = E_RdfTerm.FALSE;
		}
		
		return result;
	}
	
	
	
	public static List<Expr> getTermValues(List<E_RdfTerm> rdfTerms) {
		List<Expr> result = new ArrayList<Expr>(rdfTerms.size());
		for(E_RdfTerm rdfTerm : rdfTerms) {
			Expr item = rdfTerm.getLexicalValue();
			
			result.add(item);
		}
		
		return result;
	}
	
	
/*
	@Override
	public E_RdfTerm transform(Expr orig, List<E_RdfTerm> exprs) {
		ExprFunction fn = orig.getFunction();
	
		String fnName = org.aksw.sparqlify.expr.util.ExprUtils.getFunctionId(fn);
		
		// Get the argument type list
		// All of the arguments must be typed literals (maybe also plain literal???)
		// otherwise its an type error
		List<String> argTypes = new ArrayList<String>(exprs.size());
		for(E_RdfTerm rdfTerm : exprs) {
			Expr termType = rdfTerm.getType();
			Expr datatype = rdfTerm.getDatatype();

			if(!termType.isConstant()) {
				logger.debug("Yielding type error because termType is not a contant in: " + rdfTerm + " from " + orig + " with " + exprs);
				return typeError;
			}
			
			int termTypeVal = termType.getConstant().getDecimal().intValue();
			
			if(termTypeVal != 3) {
				logger.debug("Yielding type error because termType is not 3 (typed literal) in: " + rdfTerm + " from " + orig + " with " + exprs);
				return typeError;				
			}

			if(!datatype.isConstant()) {
				logger.debug("Yielding type error because datatype is not a contant in: " + rdfTerm + " from " + orig + " with " + exprs);
				return typeError;				
			}
			
			String datatypeVal = datatype.getConstant().getString();
			argTypes.add(datatypeVal);
		}
		
		
		//FunctionModel
		CandidateMethod<String> candidate = TypeSystemUtils.lookupCandidate(sparqlModel, fnName, argTypes);
		
		E_RdfTerm result;
		if(candidate == null) {
			logger.debug("Yielding type error because no suitable candidate found for " + fnName + " with " + argTypes);
			return typeError; 
		}

		MethodDeclaration<String> dec = candidate.getMethod().getDeclaration();
		
		String name = dec.getName();
		String returnType = dec.getSignature().getReturnType();
		
		NodeValue nvReturnType = NodeValue.makeString(returnType);
		
		ExprList newArgs = new ExprList(getTermValues(exprs));			
		ExprFunction exprFn = new E_Function(name, newArgs);
		
		result = E_RdfTerm.createTypedLiteral(exprFn, nvReturnType);
		
		return result;
	}
*/
}
