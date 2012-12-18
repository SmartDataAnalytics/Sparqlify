package org.aksw.sparqlify.core.algorithms;

import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.factory.Factory2;
import org.aksw.sparqlify.trash.ExprCopy;

import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_LogicalOr;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.sse.Tags;

class ExprFactory_LogicalAnd
	implements Factory2<Expr>
{
	@Override
	public Expr create(Expr a, Expr b)
	{
		return new E_LogicalAnd(a, b);
	}	
}

class ExprFactory_LogicalOr
	implements Factory2<Expr>
{
	@Override
	public Expr create(Expr a, Expr b)
	{
		return new E_LogicalOr(a, b);
	}	
}

class ExprFactory_GreaterThanOrEqual
	implements Factory2<Expr>
{
	@Override
	public Expr create(Expr a, Expr b)
	{
		return new E_GreaterThanOrEqual(a, b);
	}	
}

class ExprFactory_GreaterThan
	implements Factory2<Expr>
{
	@Override
	public Expr create(Expr a, Expr b)
	{
		return new E_GreaterThan(a, b);
	}	
}

class ExprFactory_LessThanOrEqual
	implements Factory2<Expr>
{
	@Override
	public Expr create(Expr a, Expr b)
	{
		return new E_LessThanOrEqual(a, b);
	}	
}

class ExprFactory_LessThan
	implements Factory2<Expr>
{
	@Override
	public Expr create(Expr a, Expr b)
	{
		return new E_LessThan(a, b);
	}	
}

class ExprFactory_Copy2
	implements Factory2<Expr> {
	
	private ExprFunction2 prototype;
	
	public ExprFactory_Copy2(ExprFunction2 prototype) {
		this.prototype = prototype;
	}

	@Override
	public Expr create(Expr a, Expr b)
	{
		Expr result = ExprCopy.getInstance().copy(prototype, a, b);
		return result;
	}	
}

public class ExprFactoryUtils {
	
	private static final Map<String, Factory2<Expr>> binaryFactories = new HashMap<String, Factory2<Expr>>();	
	
	public static final Factory2<Expr> factoryLogicalAnd = new ExprFactory_LogicalAnd();
	public static final Factory2<Expr> factoryLogicalOr = new ExprFactory_LogicalOr();

	public static final Factory2<Expr> factoryLessThan = new ExprFactory_LessThan();
	public static final Factory2<Expr> factoryLessThanOrEqual = new ExprFactory_LessThanOrEqual();
	public static final Factory2<Expr> factoryGreaterThanOrEqual = new ExprFactory_GreaterThanOrEqual();
	public static final Factory2<Expr> factoryGreaterThan = new ExprFactory_GreaterThan();
	
	static {
		binaryFactories.put(Tags.symLT, factoryLessThan);
		binaryFactories.put(Tags.symLE, factoryLessThanOrEqual);
		binaryFactories.put(Tags.symGE, factoryGreaterThanOrEqual);
		binaryFactories.put(Tags.symGT, factoryGreaterThan);
	}

	public static Factory2<Expr> getFactory2(String tag) {
		Factory2<Expr> result = binaryFactories.get(tag);
		return result;
	}
	
	public static Factory2<Expr> createCopyFactory2(ExprFunction2 prototype) {
		Factory2<Expr> result = new ExprFactory_Copy2(prototype);
		
		return result;
	}
}
