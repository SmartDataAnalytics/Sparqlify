package mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.commons.util.reflect.MultiMethod;
import org.apache.commons.lang.NotImplementedException;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprAggregator;
import com.hp.hpl.jena.sparql.expr.ExprFunction0;
import com.hp.hpl.jena.sparql.expr.ExprFunction1;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprFunction3;
import com.hp.hpl.jena.sparql.expr.ExprFunctionN;
import com.hp.hpl.jena.sparql.expr.ExprFunctionOp;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueDecimal;


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

