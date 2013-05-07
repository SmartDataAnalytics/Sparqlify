package org.aksw.sparqlify.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

interface CollectionFactory<T> {
	Collection<T> newCollection();
}


class CollectionFactoryArrayList<T>
	implements CollectionFactory<T>
{
	@Override
	public ArrayList<T> newCollection() {
		return new ArrayList<T>();
	}
}



public class CnfTransformer<T> {

	private ExprAccessor<T> accessor;
	
	public CnfTransformer(ExprAccessor<T> accessor) {
		this.accessor = accessor;
	}
	
	
	public T eval(T expr) {
		T result = eval(expr, accessor);
		return result;
	}

	
	public static <T> T eval(T expr, ExprAccessor<T> accessor)
	{
		T result;
		
		if(accessor.isLogicalNot(expr)) {
			
			T child = accessor.getArg(expr);

			T newExpr;
			
			if (accessor.isLogicalAnd(child)) {
				newExpr = accessor.createLogicalOr(
						eval(accessor.createLogicalNot(accessor.getArg1(child)), accessor),
						eval(accessor.createLogicalNot(accessor.getArg2(child)), accessor));
			}
			else if (accessor.isLogicalOr(child)) {
				newExpr = accessor.createLogicalAnd(
						eval(accessor.createLogicalNot(accessor.getArg(child)), accessor),
						eval(accessor.createLogicalNot(accessor.getArg(child)), accessor));
			}
			else if (accessor.isLogicalNot(child)) {
				newExpr = eval(accessor.getArg(child), accessor);
			}
			else {
				return expr;
			}
			
			result = eval(newExpr, accessor);
		}
		
		
		else if (accessor.isLogicalAnd(expr)) {
			//return expr;
			//return eval(expr);
			result = accessor.createLogicalAnd(
					eval(accessor.getArg1(expr), accessor),
					eval(accessor.getArg2(expr), accessor));
		}		

		else if (accessor.isLogicalOr(expr)) {

			T aa = eval(accessor.getArg1(expr), accessor);
			T bb = eval(accessor.getArg2(expr), accessor);
			
			// If at least one of the expr's argument is a logical and,
			// then it will be assigned to a
			T a = null;
			T b = null;
			
			if (accessor.isLogicalAnd(aa)) {
				a = aa;
				b = bb;
			}
			else if(accessor.isLogicalAnd(bb)) {
				a = bb;
				b = aa;
			}
			
			if(a == null) {

				result = accessor.createLogicalOr(aa, bb);

			} else {
				result = accessor.createLogicalAnd(
						eval(accessor.createLogicalOr(accessor.getArg1(a), b), accessor),
						eval(accessor.createLogicalOr(accessor.getArg2(a), b), accessor));
			}
		}		

		else {
			result = expr;
		}
//		else if (expr instanceof E_NotEquals) { // Normalize (a != b) to !(a = b) --- this makes it easier to find "a and !a" cases
//			return new E_LogicalNot(eval(new E_Equals(expr.getArg(1), expr.getArg(2))));
//		}

		
		return result;
	}

	
	public static <T> List<Collection<T>> toCnf(T expr, ExprAccessor<T> accessor) {
		
		List<Collection<T>> result = toCnf(Collections.singleton(expr), accessor);
		return result;
	}
	
	public static <T> List<Collection<T>> toCnf(Iterable<T> exprs, ExprAccessor<T> accessor) {
		List<Collection<T>> result = new ArrayList<Collection<T>>();
		
		CollectionFactory<T> clauseFactory = new CollectionFactoryArrayList<T>();

		for(T expr : exprs) {
			T tmp = eval(expr, accessor);
			
			collectAnd(tmp, result, accessor, clauseFactory);
		}
		
		return result;
	}
	

	
	/**
	 * This method only words if the input expressions are in DNF,
	 * otherwise you will likely get junk back.
	 * 
	 * @param exprs
	 * @return
	 */
	public static <T> List<Collection<T>> cnfToClauses(Iterable<T> exprs, ExprAccessor<T> accessor) {
		List<Collection<T>> result = new ArrayList<Collection<T>>();

		CollectionFactory<T> clauseFactory = new CollectionFactoryArrayList<T>();

		
		for(T expr : exprs) {
			collectAnd(expr, result, accessor, clauseFactory);
		}
		
		return result;
	}

	
	public static <T> void collectAnd(T expr, Collection<Collection<T>> clauses, ExprAccessor<T> accessor, CollectionFactory<T> clauseFactory)
	{		
		if(accessor.isLogicalAnd(expr)) {
			collectAnd(accessor.getArg1(expr), clauses, accessor, clauseFactory);
			collectAnd(accessor.getArg2(expr), clauses, accessor, clauseFactory);
		}
		else if(accessor.isLogicalOr(expr)) {
			//List<Expr> ors = new ArrayList<Expr>();
			Collection<T> ors = clauseFactory.newCollection();
			collectOr(expr, ors, accessor);

			clauses.add(ors);
		} else {
			Collection<T> ors = clauseFactory.newCollection();
			ors.add(expr);
			
			clauses.add(ors);
		}
	}

	public static <T> void collectOr(T expr, Collection<T> clause, ExprAccessor<T> accessor)
	{
		if(accessor.isLogicalOr(expr)) {			
			collectOr(accessor.getArg1(expr), clause, accessor);
			collectOr(accessor.getArg2(expr), clause, accessor);
		} else {
			clause.add(expr);			
		} 
	}

}
