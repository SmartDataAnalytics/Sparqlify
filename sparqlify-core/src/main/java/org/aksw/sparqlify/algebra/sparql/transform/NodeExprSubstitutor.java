package org.aksw.sparqlify.algebra.sparql.transform;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import mapping.ExprCopy;

import org.aksw.commons.util.reflect.MultiMethod;
import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.aggregate.Aggregator;


/**
 * Can replace nodes with expressions
 * 
 * @author raven
 *
 */
public class NodeExprSubstitutor {

	private Map<? extends Node, ? extends Expr> map;
    
	/*
	public static ExprList apply(NodeExprSubstitutor substitutor, ExprList exprs) {
		ExprList result = new ExprList();
		for(Expr expr : exprs) {
			Expr tmp = substitutor.transformList(exprs)
		}
	}*/
	
	public static NodeExprSubstitutor create(Map<String, Expr> varNameToExpr) {
		Map<Var, Expr> tmp = new HashMap<Var, Expr>();
		for(Entry<String, Expr> entry : varNameToExpr.entrySet()) {
			tmp.put(Var.alloc(entry.getKey()), entry.getValue());
		}
		
		NodeExprSubstitutor result = new NodeExprSubstitutor(tmp);
		return result;
	}
	
    public NodeExprSubstitutor(Map<? extends Node, ? extends Expr> map)
    {
    	this.map = map;
    }

    public Expr transformMM(Expr expr)
    {
    	return expr == null
    		? null
    		: (Expr)MultiMethod.invoke(this, "_transform", expr);
    }
        
    public Expr _transform(ExprFunction expr) {
    	ExprList args = transformList(expr.getArgs());
    	
    	Expr result = ExprCopy.getInstance().copy(expr, args);
    	return result;
    }
    
    public ExprList transformList(Iterable<Expr> exprs) {
    	ExprList result = new ExprList();

    	for(Expr expr : exprs) {
    		result.add(transformMM(expr));
    	}
    
    	return result;
    }

	/*
	public Expr _transform(ExprFunction0 func) {
		return func;
	}

	public Expr _transform(ExprFunction1 func, Expr expr1) {
		return func.copy(transformMM(func.getArg()));
	}

	@Override
	public Expr transform(ExprFunction2 func, Expr expr1, Expr expr2) {
		return func.copy(transformMM(func.getArg1()), transformMM(func.getArg2()));
	}

	@Override
	public Expr transform(ExprFunction3 func, Expr expr1, Expr expr2, Expr expr3) {
		return func.copy(transformMM(func.getArg1()), transformMM(func.getArg2()), transformMM(func.getArg3()));
	}

	@Override
	public Expr transform(ExprFunctionN func, ExprList args) {
		return (Expr)MultiMethod.invoke(func, "copy", transformList(args));
	}
	*/

	public Expr _transform(ExprFunctionOp funcOp, ExprList args, Op opArg) {
		throw new NotImplementedException();
	}

	public Expr _transform(NodeValue nv) {
		
		return nv;
	}

	public Expr _transform(ExprVar nv) {
		Expr tmp = map.get(nv.asVar());
		return tmp != null ? tmp : nv;
	}

	public Expr _transform(ExprAggregator eAgg) {
		
		Expr newAggExpr = this.transformMM(eAgg.getAggregator().getExpr()); 
		Aggregator newAgg = eAgg.getAggregator().copy(newAggExpr);
		
		Expr newAggVar = this.transformMM(eAgg.getAggVar());
		
		//ExprCopy.getInstance().copy(expr, eAgg.get)
		
		Expr result = new ExprAggregator(newAggVar.asVar(), newAgg);
	
		return result;

		//System.out.println("Aggregate");
		
		//eAgg.getAggregator().
		//AggCount 
		//eAgg.get
		
		
		
		//throw new NotImplementedException();
	}    
}

