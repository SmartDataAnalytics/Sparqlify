package org.aksw.sparqlify.core.algorithms;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;

/**
 * This transformer is mainly intended for "local" transforms
 * of functions.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class ExprTransformerMap
	implements ExprTransformer
{
	private Map<String, ExprTransformer> idToTransformer = new HashMap<String, ExprTransformer>();


	public ExprTransformerMap() {
	}
	
	public ExprTransformerMap(Map<String, ExprTransformer> idToTransformer) {
		this.idToTransformer = idToTransformer;
	}
	
	public Map<String, ExprTransformer> getTransformerMap() {
		return idToTransformer;
	}
	
	
	public static String getFunctionId(ExprFunction fn) {
		
		String result = null;

		result = fn.getOpName();
		if(result != null) {
			return result;
		}

		result = fn.getFunctionSymbol() == null ? null : fn.getFunctionSymbol().getSymbol();
		if(result != null) {
			return result;
		}

		result = fn.getFunctionIRI();
		/*
		if(result != null) {
			return result;
		}*/
		
		return result;
	}

	public ExprTransformer lookup(ExprFunction fn) {

		String id = ExprTransformerMap.getFunctionId(fn);
		ExprTransformer result = idToTransformer.get(id);

		return result;
	}
	
	public Expr transform(ExprFunction expr) {
		Expr result = expr;
		
		if(expr.isFunction()) {
			ExprFunction fn = expr.getFunction();
			
			ExprTransformer transformer = lookup(fn);
			if(transformer != null) {
				result = transformer.transform(expr);
			}
		}
		
		return result;
	}
}
