package org.aksw.sparqlify.util;

public interface ExprAccessor<T> {
	
	public abstract boolean isLogicalNot(T expr);
	public abstract boolean isLogicalAnd(T expr);
	public abstract boolean isLogicalOr(T expr);

	
	
//public abstract List<? extends T> getArgs(T expr);
	public abstract T getArg(T expr);
	
	public abstract T getArg1(T expr);
	public abstract T getArg2(T expr);

	public abstract T createLogicalAnd(T a, T b);
	public abstract T createLogicalOr(T a, T b);
	public abstract T createLogicalNot(T expr);	
}