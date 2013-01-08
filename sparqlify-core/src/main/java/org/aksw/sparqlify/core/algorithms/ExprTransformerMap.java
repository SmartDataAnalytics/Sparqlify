package org.aksw.sparqlify.core.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.sparqlify.expr.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private static final Logger logger = LoggerFactory.getLogger(ExprTransformerMap.class);
	
	private Map<String, ExprTransformer> idToTransformer = new HashMap<String, ExprTransformer>();


	public ExprTransformerMap() {
	}
	
	public ExprTransformerMap(Map<String, ExprTransformer> idToTransformer) {
		this.idToTransformer = idToTransformer;
	}
	
	public Map<String, ExprTransformer> getTransformerMap() {
		return idToTransformer;
	}
	
	
	public ExprTransformer lookup(ExprFunction fn) {

		String id = ExprUtils.getFunctionId(fn);
		ExprTransformer result = idToTransformer.get(id);

		return result;
	}
	
	private static Set<Expr> seenErrors = new HashSet<Expr>();
	
	public Expr transform(ExprFunction expr) {
		Expr result = expr;
		
		if(expr.isFunction()) {
			ExprFunction fn = expr.getFunction();
			
			ExprTransformer transformer = lookup(fn);
			if(transformer != null) {
				result = transformer.transform(expr);
			} else {
				
				if(!seenErrors.contains(fn)) {
					logger.warn("No transformer registered for " + fn);
					seenErrors.add(fn);
				}
				
				//throw new RuntimeException("No transformer registered for " + fn);
			}
		}
		
		return result;
	}
}
