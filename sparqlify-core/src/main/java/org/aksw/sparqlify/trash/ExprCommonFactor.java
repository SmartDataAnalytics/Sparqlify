package org.aksw.sparqlify.trash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.reflect.MultiMethod;

import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.FunctionLabel;
import org.apache.jena.sparql.expr.NodeValue;

/**
 * Factors out the common top-level expressions of two given expressions.
 * 
 * 
 * @author raven
 *
 */
public class ExprCommonFactor {
	
	private Generator generator;
	
	public ExprCommonFactor(Generator generator)
	{
		this.generator = generator;
	}
	
	private Map<Var, Expr> mapA = new HashMap<Var, Expr>();
	private Map<Var, Expr> mapB = new HashMap<Var, Expr>();

	//private Map<SqlDatatype, Map<Integer, Var>> datatypeToColumn = new HashMap<SqlDatatype, Map<Integer, Var>>();
	

	public Expr transformMM(Expr a, Expr b)
	{
		return (Expr)MultiMethod.invoke(this, "transform", a, b);
	}
	
	
	public Expr transform(Expr a, Expr b) {
		return transformHelper(a, b);
	}

	/*
	public List<Expr> transformList(Iterable<Expr> exprs)
	{
		ExprList result = new ExprList();
		for(Expr expr : exprs) {
			result.add(transformMM(expr));
		}
		
		return result;
	}*/
	
	public Expr transformHelper(Expr a, Expr b) {
		// No match found - create a new helper variable
		
		if(a.equals(b)) {
			return a;
		}
		
		Var var = Var.alloc(generator.next());
		ExprVar result = new ExprVar(var);

		mapA.put(var, a);
		mapB.put(var, b);
		
		return result;
	}

	
	public Expr transform(ExprFunction a, ExprFunction b) {
		if(a.getClass().equals(b.getClass())) {
			if(a.getFunctionSymbol().equals(b.getFunctionSymbol())) {
				if(a.numArgs() == b.numArgs()) {
					
					ExprList tmp = new ExprList();
					for(int i = 0; i < a.numArgs(); ++i) {
						Expr newExpr = transformMM(a.getArgs().get(i), b.getArgs().get(i));
						tmp.add(newExpr);					
					}
					
					return ExprCopy.getInstance().copy(a, tmp);
				}
			}			
		}

		return transformHelper(a, b);		
	}
	

	public Expr transformHelper(List<Expr> exprs, List<Map<Var, Expr>> inout) {
		// No match found - create a new helper variable
		
		Var var = Var.alloc(generator.next());
		ExprVar result = new ExprVar(var);

		for(int i = 0; i < exprs.size(); ++i) {
			inout.get(i).put(var, exprs.get(i));
		}
		
		return result;
	}


	public Expr transform(List<Expr> exprs, List<Map<Var, Expr>> inout) {
		
		if(inout.isEmpty()) {
			for(int i = 0; i < exprs.size(); ++i) {
				inout.add(new HashMap<Var, Expr>());
			}
		}
		
		// Get first non null expr
		// FIXME Not sure if nulls should be allowed, currently null support
		// is not fully implemented
		Expr proto = null;
		for(int i = 0; i < exprs.size(); ++i) {
			proto = exprs.get(i);
			if(proto == null) {
				continue;
			}
		}
		Class<?> clazz = proto.getClass();

		for(Expr expr : exprs) {
			if(!expr.getClass().equals(clazz)) {
				Expr result = transformHelper(exprs, inout);
				return result;
			}
		}
		
		if(proto instanceof ExprFunction) {
			ExprFunction protoFunc = (ExprFunction)proto;
			int numArgs = protoFunc.numArgs();
			FunctionLabel protoName = protoFunc.getFunctionSymbol();
			
			for(Expr expr : exprs) {
				ExprFunction tmp = (ExprFunction)expr;
				if(numArgs != tmp.numArgs() || !protoName.equals(tmp.getFunctionSymbol())) {
					Expr result = transformHelper(exprs, inout);
					return result;
				}
			}

			ExprList transformedArgs = new ExprList();
			for(int i = 0; i < numArgs; i++) {
				List<Expr> args = new ArrayList<Expr>();
				for(Expr expr : exprs) {
					ExprFunction tmp = (ExprFunction)expr;
					
					args.add(tmp.getArgs().get(i));
				}

				Expr tmp = transform(args, inout);
				transformedArgs.add(tmp);
			}
			
			Expr result = ExprCopy.getInstance().copy(protoFunc, transformedArgs);
			return result;
		}
		else if(proto instanceof NodeValue) {
			for(Expr expr : exprs) {
				if(!expr.equals(proto)) {
					Expr result = transformHelper(exprs, inout);
					return result;
				}				
			}
			return proto;
		}

		Expr result = transformHelper(exprs, inout);
		return result;
	}
}




/**
 * Factors out the common top-level expressions of two given expressions.
 * 
 * 
 * @author raven
 *
 * /
class ExprCommonFactorOld {
	
	private Generator generator;
	
	public ExprCommonFactor(Generator generator)
	{
		this.generator = generator;
	}
	
	private Map<Var, Expr> mapA = new HashMap<Var, Expr>();
	private Map<Var, Expr> mapB = new HashMap<Var, Expr>();

	private int nextHelperVarId = 0;

	public Expr transformMM(Expr a, Expr b)
	{
		return (Expr)MultiMethod.invoke(this, "transform", a, b);
	}
	
	
	public Expr transform(Expr a, Expr b) {
		return transformHelper(a, b);
	}

	/*
	public List<Expr> transformList(Iterable<Expr> exprs)
	{
		ExprList result = new ExprList();
		for(Expr expr : exprs) {
			result.add(transformMM(expr));
		}
		
		return result;
	}* /
	
	public Expr transformHelper(Expr a, Expr b) {
		// No match found - create a new helper variable
		
		Var var = Var.alloc(generator.next());
		ExprVar result = new ExprVar(var);

		mapA.put(var, a);
		mapB.put(var, b);
		
		return result;
	}

	
	public Expr transform(ExprFunction a, ExprFunction b) {
		if(a.getClass().equals(b.getClass())) {
			if(a.getFunctionSymbol().equals(b.getFunctionSymbol())) {
				if(a.numArgs() == b.numArgs()) {
					
					ExprList tmp = new ExprList();
					for(int i = 0; i < a.numArgs(); ++i) {
						Expr newExpr = transformMM(a.getArgs().get(i), b.getArgs().get(i));
						tmp.add(newExpr);					
					}
					
					return ExprCopy.copyMM(a, tmp);
				}
			}			
		}

		return transformHelper(a, b);		
	}
	

	public Expr transformHelper(List<Expr> exprs, List<Map<Var, Expr>> inout) {
		// No match found - create a new helper variable
		//int helperVarId = nextHelperVarId++;
		
		Var var = Var.alloc(generator.next());
		ExprVar result = new ExprVar(var);

		for(int i = 0; i < exprs.size(); ++i) {
			inout.get(i).put(var, exprs.get(i));
		}
		
		return result;
	}


	public Expr transform(List<Expr> exprs, List<Map<Var, Expr>> inout) {
		
		if(inout.isEmpty()) {
			for(int i = 0; i < exprs.size(); ++i) {
				inout.add(new HashMap<Var, Expr>());
			}
		}
		
		// Get first non null expr
		// FIXME Not sure if nulls should be allowed, currently null support
		// is not fully implemented
		Expr proto = null;
		for(int i = 0; i < exprs.size(); ++i) {
			proto = exprs.get(i);
			if(proto == null) {
				continue;
			}
		}
		Class<?> clazz = proto.getClass();

		for(Expr expr : exprs) {
			if(!expr.getClass().equals(clazz)) {
				Expr result = transformHelper(exprs, inout);
				return result;
			}
		}
		
		if(proto instanceof ExprFunction) {
			ExprFunction protoFunc = (ExprFunction)proto;
			int numArgs = protoFunc.numArgs();
			FunctionLabel protoName = protoFunc.getFunctionSymbol();
			
			for(Expr expr : exprs) {
				ExprFunction tmp = (ExprFunction)expr;
				if(numArgs != tmp.numArgs() || !protoName.equals(tmp.getFunctionSymbol())) {
					Expr result = transformHelper(exprs, inout);
					return result;
				}
			}

			ExprList transformedArgs = new ExprList();
			for(int i = 0; i < numArgs; i++) {
				List<Expr> args = new ArrayList<Expr>();
				for(Expr expr : exprs) {
					ExprFunction tmp = (ExprFunction)expr;
					
					args.add(tmp.getArgs().get(i));
				}

				Expr tmp = transform(args, inout);
				transformedArgs.add(tmp);
			}
			
			Expr result = ExprCopy.copyMM(protoFunc, transformedArgs);
			return result;
		}
		else if(proto instanceof NodeValue) {
			for(Expr expr : exprs) {
				if(!expr.equals(proto)) {
					Expr result = transformHelper(exprs, inout);
					return result;
				}				
			}
			return proto;
		}

		Expr result = transformHelper(exprs, inout);
		return result;
	}
}
*/

