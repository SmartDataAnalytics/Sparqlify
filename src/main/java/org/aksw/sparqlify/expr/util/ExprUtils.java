package org.aksw.sparqlify.expr.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.collections.IterableCollection;
import org.aksw.commons.factory.Factory2;
import org.aksw.commons.util.Pair;

import scala.actors.threadpool.Arrays;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.NodeValue;


public class ExprUtils {
	@SuppressWarnings("unchecked")
	public static Expr andifyBalanced(Expr ... exprs) {
		return andifyBalanced(Arrays.asList(exprs));
	}
	
	@SuppressWarnings("unchecked")
	public static Expr orifyBalanced(Expr ... exprs) {
		return orifyBalanced(Arrays.asList(exprs));
	}
	
	public static List<String> extractNames(Collection<Var> vars) {
		List<String> result = new ArrayList<String>();
		for(Var var : vars) {
			result.add(var.getName());
		}
		
		return result;
	}
	
	public static Expr andifyBalanced(Iterable<Expr> exprs) {
		return opifyBalanced(exprs, new Factory2<Expr>() {
			@Override
			public Expr create(Expr a, Expr b)
			{
				return new E_LogicalAnd(a, b);
			}
		});		
	}	
	
	/**
	 * Concatenates the sub exressions using Logical_And
	 * 
	 * and(and(0, 1), and(2, 3))
	 * 
	 * @param exprs
	 * @return
	 */
	public static <T> T opifyBalanced(Iterable<T> exprs, Factory2<T> exprFactory) {
		if(exprs.iterator().hasNext() == false) { //isEmpty()) {
			return null;
		}

		List<T> current = new ArrayList<T>(IterableCollection.wrap(exprs)); 
		
		while(current.size() > 1) { 

			List<T> next = new ArrayList<T>();
			T left = null;
			for(T expr : current) {
				if(left == null) {
					left = expr;
				} else {
					T newExpr = exprFactory.create(left, expr);
					next.add(newExpr);
					left = null;
				}
			}
	
			if(left != null) {
				next.add(left);
			}
			
			current.clear();
			
			List<T> tmp = current;
			current = next;
			next = tmp;
		}
		
		return current.get(0);
	}

	public static Expr orifyBalanced(Iterable<Expr> exprs) {
		return opifyBalanced(exprs, new Factory2<Expr>() {
			@Override
			public Expr create(Expr a, Expr b)
			{
				return new E_LogicalOr(a, b);
			}
		});		
	}
	
	
	
	
	public static Pair<Var, NodeValue> extractConstantConstraint(Expr expr) {
		if(expr instanceof E_Equals) {
			E_Equals e = (E_Equals)expr;
			return extractConstantConstraint(e.getArg1(), e.getArg2());
		}
		
		return null;
	}
	
	public static Pair<Var, NodeValue> extractConstantConstraint(Expr a, Expr b) {
		Pair<Var, NodeValue> result = extractConstantConstraintDirected(a, b);
		if(result == null) {
			result = extractConstantConstraintDirected(b, a);
		}
		
		return result;
	}

	/*
	public static void extractConstantConstraints(Expr a, Expr b, EquiMap<Var, NodeValue> equiMap) {
		extractConstantConstraints(a, b, equiMap.getKeyToValue());
	}*/
	
	
	/**
	 * If a is a variable and b is a constant, then a mapping of the variable to the
	 * constant is put into the map, and true is returned.
	 * Otherwise, nothing is changed, and false is returned.
	 * 
	 * A mapping of a variable is set to null, if it is mapped to multiple constants
	 * 
	 * 
	 * @param a
	 * @param b
	 * @param map
	 * @return
	 */
	public static Pair<Var, NodeValue> extractConstantConstraintDirected(Expr a, Expr b) {
		if(!(a.isVariable() && b.isConstant())) {
			return null;
		}
		
		Var var = a.getExprVar().asVar();
		NodeValue nodeValue = b.getConstant();		
		
		return Pair.create(var, nodeValue);
	}

	public static Collection<? extends Expr> getSubExpressions(Expr expr, boolean reflexive) {
		Set<Expr> result = new HashSet<Expr>();
		
		if(reflexive) {
			result.add(expr);
		}
		
		getSubExpressions(expr, result);		
		
		return result;
	}
	
	public static void getSubExpressions(Expr expr, Set<Expr> result) {
		if(expr.isFunction()) {
			ExprFunction f = (ExprFunction)expr;
			
			for(int i = 1; i <= f.numArgs(); ++i) {
				Expr arg = f.getArg(i);
				if(!result.contains(arg)) {
					result.add(arg);
					getSubExpressions(arg, result);
				}
			}
		}
		
	}
	
	/*
	public static boolean extractConstantConstraintsDirected(Expr a, Expr b, EquiMap<Var, NodeValue> equiMap) {
		return extractConstantConstraintsDirected(a, b, equiMap.getKeyToValue());
	}*/
	
}
