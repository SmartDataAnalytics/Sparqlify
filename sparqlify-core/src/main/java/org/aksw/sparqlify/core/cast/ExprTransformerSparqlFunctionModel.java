package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.transformations.ExprTransformer;
import org.aksw.sparqlify.type_system.CandidateMethod;
import org.aksw.sparqlify.type_system.FunctionModel;
import org.aksw.sparqlify.type_system.MethodDeclaration;
import org.aksw.sparqlify.type_system.TypeSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.NodeValue;

public class ExprTransformerSparqlFunctionModel
	implements ExprTransformer
{
	
	private static final Logger logger = LoggerFactory.getLogger(ExprTransformerSparqlFunctionModel.class);
	

	// TODO Move to appropriate place
	private static E_RdfTerm typeError = E_RdfTerm.createTypedLiteral(SparqlifyConstants.nvTypeError, SparqlifyConstants.nvTypeError);
	
	private FunctionModel<String> sparqlModel;

	
	public ExprTransformerSparqlFunctionModel(FunctionModel<String> sparqlModel) {
		this.sparqlModel = sparqlModel;
	}
	
	
	public static List<Expr> getTermValues(List<E_RdfTerm> rdfTerms) {
		List<Expr> result = new ArrayList<Expr>(rdfTerms.size());
		for(E_RdfTerm rdfTerm : rdfTerms) {
			Expr item = rdfTerm.getLexicalValue();
			
			result.add(item);
		}
		
		return result;
	}
	
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
	
}