package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mapping.ExprCopy;

import org.aksw.sparqlify.core.domain.VarBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.util.ExprUtils;

public class ExprEvaluatorPartial
	implements ExprEvaluator
{
	
	private static final Logger logger = LoggerFactory.getLogger(ExprEvaluatorPartial.class);
	
	private FunctionRegistry registry;
	
	/**
	 *  The transformer is called AFTER all of a functions arguments have been evaluated.
	 *  
	 */
	private ExprTransformer exprTransformer;
	
	public ExprEvaluatorPartial(FunctionRegistry registry, ExprTransformer exprTransformer) {
		this.registry = registry;
		this.exprTransformer = exprTransformer;
	}
	
	
	public static boolean isConstantArgsOnly(ExprFunction fn) {
		for(Expr arg : fn.getArgs()) {
			if(!arg.isConstant()) {
				return false;
			}
		}
		
		return true;
	}
	
	public Expr eval(ExprFunction fn, Map<Var, Expr> binding) {
		List<Expr> evaledArgs = new ArrayList<Expr>();
		
		for(Expr arg : fn.getArgs()) {				
			Expr evaledArg = eval(arg, binding);
			
			// If an argument evaluated to type error, return type error
			// (currently represented with nvNothing - is that safe?)			
			if(evaledArg.equals(NodeValue.nvNothing)) {
				return NodeValue.nvNothing;
			}
			
			evaledArgs.add(evaledArg);
		}
		
		Expr newExpr = ExprCopy.getInstance().copy(fn, evaledArgs);
		Expr tmp = newExpr;
		
		if(exprTransformer != null && newExpr.isFunction()) {
			tmp = exprTransformer.transform(newExpr.getFunction());
		}
				
		
		// If some arguments are not constant, we can't evaluate
		if(tmp.isFunction() && !ExprEvaluatorPartial.isConstantArgsOnly(tmp.getFunction())) {
			return tmp;
		}

		
		
		// Check if the function's IRI is not registered
		// If not, don't try to evaluate it
		String fnIri = fn.getFunctionIRI();			
		if(fnIri != null && !fnIri.isEmpty()) {
			if(registry.get(fnIri) == null) {
				return tmp;
			}
		}
		
		Expr result = tmp;
		
		try {
			result = ExprUtils.eval(tmp);
		} catch(Exception e) {
			// Failed to evaluate - use original value
			logger.warn("Failed to evaluate expr: " + tmp);
		}
		
		return result;
	}
	
	/*
	 * How to best add interceptors (callbacks with transformation) for certain functions?
	 * 
	 * e.g.: concat(foo, concat(?x...)) -> concat(foo, ?x)
	 * lang(rdfterm(2, ?x, ?y, '')) -> ?y
	 * 
	 * The main question is, whether to apply to callback before or after the arguments are evaluated.
	 * 
	 * -> After makes more sense: Then we have constant folder arguments 
	 */
	public Expr eval(Expr expr, Map<Var, Expr> binding) {
		
		System.out.println(expr);
		
		Expr result = null;
		if(expr.isConstant()) {
			result = expr;
		} else if(expr.isFunction()) {
			ExprFunction fn = expr.getFunction();
			
			result = eval(fn, binding);
		} else if(expr.isVariable()) {
			
			if(binding != null) {
				Expr boundExpr = binding.get(expr.asVar());
				if(boundExpr != null) {
					result = eval(boundExpr, null); // Do not forward the binding
				}
			}
		} else {
			throw new RuntimeException("Unknown expression type encountered: " + expr);
		}

		return result;
	}

}
