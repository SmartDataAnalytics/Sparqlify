package org.aksw.sparqlify.trash;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunction3;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;


class ExprCopySlow {

	private static ExprCopy instance;
	
	public static ExprCopy getInstance()
	{
		if(instance == null) {
			instance = new ExprCopy();
		}
		return instance;
	}
	
	public Expr deepCopy(Expr proto)
	{
		return deepCopy(proto, ExprArgs.getArgs(proto));
	}
	
	public Expr deepCopy(Expr proto, ExprList args)
	{
		return copy(proto, deepCopy(args));
	}

	public ExprList deepCopy(ExprList exprs) {
		ExprList result = new ExprList();
		for(Expr expr : exprs) {
			result.add(deepCopy(expr));
		}
		
		return result;
	}
	

	/**
	 * Creates a copy of an expression, with different arguments.
	 * 
	 * 
	 * @param proto
	 * @param args
	 */
	public Expr copy(Expr proto, Expr ...args)
	{
		@SuppressWarnings("unchecked")
		ExprList list = new ExprList(Arrays.asList(args));
		return (Expr)MultiMethod.invoke(this, "_copy", proto, list);
	}
		
	
	public Expr copy(Expr proto, ExprList args)
	{
		return (Expr)MultiMethod.invoke(this, "_copy", proto, args);
	}

	public Expr copy(Expr proto, List<Expr> args)
	{
		return (Expr)MultiMethod.invoke(this, "_copy", proto, new ExprList(args));
	}

	
	public Expr copy(Expr expr) {
		return copy(expr, ExprArgs.getArgs(expr));
	}
	
	public Expr _copy(ExprFunction0 func, ExprList args) {
		return func;
	}

	public Expr _copy(ExprFunction1 func, ExprList args) {
		return func.copy(args.get(0));
	}

	public Expr _copy(ExprFunction2 func, ExprList args) {
		return func.copy(args.get(0), args.get(1));
	}

	public Expr _copy(ExprFunction3 func, ExprList args) {
		return func.copy(args.get(0), args.get(1), args.get(2));
	}
	
	public List<String> classNameList(Iterable<Class<?>> classes) {
		List<String> result = new ArrayList<String>();
		for(Class<?> clazz : classes) {
			result.add(clazz.getName());
		}
		
		return result;
	}
	
	public Expr _copy(ExprFunctionN func, ExprList args) {
		return (Expr)MultiMethod.invoke(func, "copy", args);
	}

	/*
	public Expr _copy(ExprFunctionOp funcOp, ExprList args, Op opArg) {
		throw new NotImplementedException();
	}*/

	public Expr _copy(NodeValue nv, ExprList args) {
		return nv;
	}

	public Expr _copy(ExprVar nv, ExprList args) {
		return nv;
	}

	public Expr _copy(ExprAggregator eAgg, ExprList args) {
		throw new NotImplementedException();
	}    
}




public class ExprCopy {

	private static ExprCopy instance;
	
	public static ExprCopy getInstance()
	{
		if(instance == null) {
			instance = new ExprCopy();
		}
		return instance;
	}
	
	public Expr deepCopy(Expr proto)
	{
		Expr result;

		if(proto == null) {
			throw new NullPointerException();
		}
		if(proto.isConstant()) {
			result = deepCopy(proto.getConstant());
		}
		else if(proto.isFunction()) {
			result = deepCopy(proto.getFunction());
		}
		else if(proto.isVariable()) {
			result = deepCopy(proto.getExprVar());
		}
		else {
			throw new RuntimeException("Don't know how to copy " + proto + " of type " + proto.getClass());
		}

		return result;
		//return deepCopy(proto, ExprArgs.getArgs(proto));
	}
	
	
	public Expr deepCopy(NodeValue expr) {
		return expr;
	}
	
	public Expr deepCopy(ExprVar expr) {
		return expr;
	}
	
	public Expr deepCopy(ExprFunction fn) {
		List<Expr> args = fn.getArgs();
		
		List<Expr> newArgs = new ArrayList<Expr>(args.size());
		for(Expr arg : args) {
			Expr newArg = deepCopy(arg);
			newArgs.add(newArg);
		}

		Expr result = deepCopy(fn, newArgs);
		return result;
	}
	
	/**
	 * This method is intended to be overridden to e.g. substitute functions
	 * 
	 * In the overriding method, call "super.deepCopy(proto, args)" for the default handling.
	 * 
	 * @param proto
	 * @param args
	 * @return
	 */
	public Expr deepCopy(ExprFunction proto, List<Expr> args) {
		Expr result = copy(proto, args);
		return result;		
	}
	
	
	
	
	public Expr deepCopy(Expr proto, ExprList args)
	{
		return copy(proto, deepCopy(args));
	}

	public ExprList deepCopy(ExprList exprs) {
		ExprList result = new ExprList();
		for(Expr expr : exprs) {
			result.add(deepCopy(expr));
		}
		
		return result;
	}
	

	/**
	 * Creates a copy of an expression, with different arguments.
	 * 
	 * 
	 * @param proto
	 * @param args
	 */
	public Expr copy(Expr proto, Expr ...args)
	{
		@SuppressWarnings("unchecked")
		List<Expr> list = Arrays.asList(args);
		return copy(proto, list);
	}
		
	
	public Expr copy(Expr proto, ExprList args)
	{
		return copy(proto, args.getList());
	}

	public Expr copy(Expr proto, List<Expr> args)
	{
		Expr result;
		
		if(!proto.isFunction()) {
			throw new RuntimeException("Must not be called on non-functions");
		}
				
		ExprFunction fn = proto.getFunction();
		result = copy(fn, args);
		
		return result;
	}
	
	public Expr copy(ExprFunction proto, List<Expr> args) {
		Expr result;
		
		if(proto == null) {
			throw new NullPointerException();
		}
		if(proto instanceof ExprFunction0) {
			result = copy((ExprFunction0)proto, args);
		}
		else if(proto instanceof ExprFunction1) {
			result = copy((ExprFunction1)proto, args);
		}
		else if(proto instanceof ExprFunction2) {
			result = copy((ExprFunction2)proto, args);
		}
		else if(proto instanceof ExprFunction3) {
			result = copy((ExprFunction3)proto, args);
		}
		else if(proto instanceof ExprFunctionN) {
			result = copy((ExprFunctionN)proto, args);
		}
		else {
			throw new RuntimeException("Don't know how to handle " + proto + " with type " + proto.getClass());
		}

		return result;
	}

	
	public Expr copy(Expr expr) {
		return copy(expr, ExprArgs.getArgs(expr));
	}
	
	public Expr copy(ExprFunction0 func, List<Expr> args) {
		return func;
	}

	public Expr copy(ExprFunction1 func, List<Expr> args) {
		return func.copy(args.get(0));
	}

	public Expr copy(ExprFunction2 func, List<Expr> args) {
		return func.copy(args.get(0), args.get(1));
	}

	public Expr copy(ExprFunction3 func, List<Expr> args) {
		return func.copy(args.get(0), args.get(1), args.get(2));
	}
	
	public List<String> classNameList(Iterable<Class<?>> classes) {
		List<String> result = new ArrayList<String>();
		for(Class<?> clazz : classes) {
			result.add(clazz.getName());
		}
		
		return result;
	}
	
	public Expr copy(ExprFunctionN func, List<Expr> args) {
		ExprList exprList = new ExprList(args);
		
		try {
			Method m = func.getClass().getDeclaredMethod("copy", ExprList.class);
			//boolean isOriginallyAccessible = m.isAccessible();
			m.setAccessible(true);
			Expr result = (Expr)m.invoke(func, exprList);
			//m.setAccessible(isOriginallyAccessible);

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/*
	public Expr _copy(ExprFunctionOp funcOp, ExprList args, Op opArg) {
		throw new NotImplementedException();
	}*/

//	public Expr _copy(NodeValue nv, List<Expr> args) {
//		return nv;
//	}
//
//	public Expr _copy(ExprVar nv, List<Expr> args) {
//		return nv;
//	}
//
//	public Expr _copy(ExprAggregator eAgg, List<Expr> args) {
//		throw new NotImplementedException();
//	}    
}

