package org.aksw.sparqlify.compile.sparql;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mapping.SparqlifyConstants;

import org.aksw.commons.factory.Factory1;
import org.aksw.commons.factory.Factory2;
import org.aksw.commons.util.Pair;
import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sparql.expr.E_StrConcatPermissive;
import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystem;
import org.aksw.sparqlify.algebra.sql.datatype.DatatypeSystemDefault;
import org.aksw.sparqlify.algebra.sql.exprs.S_Concat;
import org.aksw.sparqlify.algebra.sql.exprs.S_Equal;
import org.aksw.sparqlify.algebra.sql.exprs.S_Function;
import org.aksw.sparqlify.algebra.sql.exprs.S_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.S_Regex;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprValue;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sparql.DnfUtils;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.expr.E_Equals;
import com.hp.hpl.jena.sparql.expr.E_Function;
import com.hp.hpl.jena.sparql.expr.E_GreaterThan;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThan;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.E_NotEquals;
import com.hp.hpl.jena.sparql.expr.E_Regex;
import com.hp.hpl.jena.sparql.expr.E_StrConcat;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprFunction2;
import com.hp.hpl.jena.sparql.expr.ExprList;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.FunctionLabel;
import com.hp.hpl.jena.sparql.expr.NodeValue;


/**
 * 
 * The following optimizations are currently implemented:
 * 
 * - f(argsA) = f(argsB) -> argA_0 = argB_0 && ... && argA_n = argB_n
 *   Currently this rule is always applied, however it is only true for
 *   deterministic, side-effect free functions.
 *   It will cause pitfalls if anyone every tried something like rand() = rand():
 *   This will return always true (i think - but maybe not)
 *    
 * 
 * The following optimizations are about to be implemented:
 * 
 * - Inverse function on variables
 *  op(f(expr_with_vars), const) -> op(expr_with_var, f^-1(const)) (if f^-1 exists) 
 * 
 *    op can be =, <, > ... (not sure right now whether the shift in semantics is acceptable)
 *    Also note, that this optization currently does not take function indexes into account.
 *    So if there was in index f(x) our optimization would be wrong in thinking that using x alone is better.
 *    
 * 
 * 
 * 
 * @author Claus Stadler
 *
 */

class Alignment
extends Pair<List<Expr>, List<Expr>>
{
public Alignment(List<Expr> key, List<Expr> value) {
	super(key, value);
}		

	public boolean isSameSize() {
		return this.getKey().size() == this.getValue().size();
	}
}


/**
 * 
 * @author raven
 *
 * @param <T>
 */
interface InverseFunctionManager<T>
{
	public Factory1<Expr> getInverse(String functionIri);
}


class InverseFunctionManagerImpl
	implements InverseFunctionManager<Expr>
{
	// (Ideally only temporary) singleton. 
	private static InverseFunctionManagerImpl instance = null;
	
	public static InverseFunctionManagerImpl getInstance() {
		if (instance == null) {
			instance = new InverseFunctionManagerImpl(); 
		}
		return instance;
	}
	
	// Maps a function label to a factory for creating its inverse
	private Map<String, Factory1<Expr>> inverseFactory = new HashMap<String, Factory1<Expr>>();
	
	public InverseFunctionManagerImpl() {
		
		// urlEncode^-1 = urlDecode 
		inverseFactory.put(SparqlifyConstants.urlEncode, new Factory1<Expr>() {
			@Override
			public Expr create(Expr arg) {
				return new E_Function(SparqlifyConstants.urlDecode, new ExprList(arg));
			}
		});

		
		// urlDecode^-1 = urlEncode 
		inverseFactory.put(SparqlifyConstants.urlDecode, new Factory1<Expr>() {
			@Override
			public Expr create(Expr arg) {
				return new E_Function(SparqlifyConstants.urlEncode, new ExprList(arg));
			}
		});
		
	}
	
	public Factory1<Expr> getInverse(String functionIri) {
		return inverseFactory.get(functionIri);
	}
}

/**
 * Deprecated, Superseded by PushDown
 * TODO The optimize methods are not deprecated, but the translate methods are
 * 
 * @author raven
 *
 */
@Deprecated
public class SqlExprOptimizer {
	public static Logger logger = LoggerFactory.getLogger(SqlExprOptimizer.class);
	
	
	public static Map<Class, Class> functionToInverse;
	
	private static DatatypeSystem datatypeSystem = new DatatypeSystemDefault();
	
	public static Expr optimizeMM(Expr expr) {
		Expr result = (Expr) MultiMethod.invokeStatic(SqlExprOptimizer.class,
				"optimize", expr);

		// Post process any result
		result = result.copySubstitute(BindingRoot.create(), true);

		Set<Set<Expr>> dnf = DnfUtils.toSetDnf(result);
		result = DnfUtils.dnfToExpr(dnf, true);

		return result;
	}

	public static Expr optimize(Expr expr) {
		return expr;
	}

	public static Expr optimize(ExprFunction expr) {
		// TODO Call optimizeMM on all args
		return expr;
	}

	public static List<Expr> optimizeList(List<Expr> exprs) {
		List<Expr> result = new ArrayList<Expr>();
		for (Expr expr : exprs) {
			result.add(expr == null ? null : optimize(expr));
		}

		return result;
	}

	public static Expr optimize(ExprFunction2 expr) {
		return expr
				.copy(optimizeMM(expr.getArg1()), optimizeMM(expr.getArg2()));
	}

	/**
	 * The expression (a < b), where a and b are
	 * RdfTerm(type, obj, lang, datatype),
	 * could become:
	 * 
	 * 
	 * Worst case:
	 * 
	 * a.type < b.type ||
	 * a.type = b.type && a.obj < b.obj ||
	 * a.type = b.type && a.type = 2 && a.obj = b.obj && a.lang < b.lang ||
	 * a.type = b.type && a.type = 3 && a.obj = b.obj && a.datatype < b.datatype ||
	 * 
	 * Optimization: Skip datatype and language in comparisions.
	 * 
	 * 
	 * TODO We have to consider datatypes to rule out invalid
	 * comparisions (in regard to the datatypes).
	 * However, this should be done on the SQL level.
	 * 
	 */
	public static Expr optimize(E_LessThan expr) {
		return optimizeCompare(expr);
	}

	public static Expr optimize(E_LessThanOrEqual expr) {
		return optimizeCompare(expr);
	}
	
	public static Expr optimize(E_GreaterThan expr) {
		return optimizeCompare(expr);
	}

	public static Expr optimize(E_GreaterThanOrEqual expr) {
		return optimizeCompare(expr);
	}

	
	/*
	public static Expr optimize(E_LogicalAnd expr) {
		Expr a = optimizeMM(expr.getArg1());
		Expr b = optimizeMM(expr.getArg1());
		
		logger.debug("TODO Handle type error correctly");
		if(a.equals(NodeValue.FALSE) || b.equals(NodeValue.FALSE)) {
			return NodeValue.FALSE;
		}
		if(a.equals(NodeValue.TRUE) && b.equals(NodeValue.TRUE)) {
			return NodeValue.TRUE;
		} else {
			return new E_LogicalAnd(a, b);
		}
		
		
	}*/
	
	/*
	class Factory2Ctor<T>
		implements IFactory2<T>
	{
		private Class clazz;
		
		public Factory2Ctor(Class clazz) {
			this.clazz = clazz;
		}
		
		@Override
		public T create(T a, T b) {
			Constructor<T> ctor = clazz.getConstructor(a, b);
			return ctor.newInstance(a, b);			
		}		
	}*/

	public static Expr optimizeCompare(ExprFunction2 expr) {
		return optimizeCompare(expr.getArg1(), expr.getArg2(), expr.getClass());
	}

	public static Expr optimizeCompare(Expr a, Expr b, final Class<?> clazz) {
		Factory2<Expr> factory = new Factory2<Expr>() {
			@Override
			public Expr create(final Expr a, final Expr b) {
				try {
					// FIXME We assume that jena's first constructor for
					// E_LessThan, E_LessThanOrEqual, etc. classes is
					// the one that takes two arguments of type Expr
					Constructor<?> ctor = clazz.getConstructors()[0]; //getConstructor(a.getClass(), b.getClass());
					return (Expr)ctor.newInstance(a, b);
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}};
			
		return optimizeCompare(a, b, factory);
	}
	
	
	/**
	 * 
	 * 
	 * 
	 * FIXME The following expression is crap. I must have written it before I was clear about type errors...
	 * a.type < b.type ||
	 * a.type = b.type && a.obj < b.obj ||
	 * a.type = b.type && a.type = 2 && a.obj = b.obj && a.lang < b.lang ||
	 * a.type = b.type && a.type = 3 && a.obj = b.obj && a.datatype < b.datatype ||
	 *
	 *
	 *
	 * @param a
	 * @param b
	 * @param exprFactory Factory for creating a comparision expression
	 * @return
	 */
	public static Expr optimizeCompare(Expr ea, Expr eb, Factory2<Expr> factory) {
		
		Expr oa = optimizeMM(ea);
		Expr ob = optimizeMM(eb);
		
		
		E_RdfTerm a = SqlPrePusher.asRdfTerm(oa);
		E_RdfTerm b = SqlPrePusher.asRdfTerm(ob);
		
		if(a == null || b == null) {
			//throw new RuntimeException("Arguments are no ExprRdfTerms");
			logger.warn("Arguments are no ExprRdfTerms");
			return factory.create(oa, ob);
		}
		
		NodeValue zero = NodeValue.makeInteger(0);
		NodeValue one = NodeValue.makeInteger(1);
		NodeValue two = NodeValue.makeInteger(2);
		NodeValue three = NodeValue.makeInteger(3);
		
		// This is the simple version that does not compare language and datatype
		/*
		Expr result =
			ExprUtils.orifyBalanced(
					factory.create(a.getType(), b.getType()),
					ExprUtils.andifyBalanced(new E_Equals(a.getType(), b.getType()), factory.create(a.getLexicalValue(), b.getLexicalValue()))
					);
		 */
		
		/*
		Expr result =
					ExprUtils.andifyBalanced(new E_Equals(a.getType(), b.getType()), factory.create(a.getLexicalValue(), b.getLexicalValue()));
		*/
		Expr result =
			ExprUtils.andifyBalanced(factory.create(a.getLexicalValue(), b.getLexicalValue()));
		
		return result;
	}
	

	private static InverseFunctionManager<Expr> inverseFunctionManager = InverseFunctionManagerImpl.getInstance();
	
	
	
	public static <T> Pair<T, T> createPair(T a, T b, boolean swapArgs) {
		return swapArgs ? new Pair<T, T>(b, a) : new Pair<T, T>(a, b);
	}
	
	public static Pair<Expr, Expr> optimizeFunctionOpConstant(Expr a, Expr b)
	{
		NodeValue constant = null;
		ExprFunction function = null;
		boolean swapArgs = false;
		
		Factory1<Expr> factory;
		
		if(a.isConstant() && b.isFunction()) {
			constant = a.getConstant();
			function = b.getFunction();
		} else if(a.isFunction() && b.isConstant()) {
			function = a.getFunction();
			constant = b.getConstant();
			swapArgs = true;
		} else {
			return Pair.create(a, b);
		}
			
		factory = inverseFunctionManager.getInverse(b.getFunction().getFunctionIRI());
		
		if(factory == null) {
			return Pair.create(a, b);
		}
		
		Expr invConst = factory.create(constant);
		Expr resultConst = com.hp.hpl.jena.sparql.util.ExprUtils.eval(invConst);
		
		Expr resultFunc = function.getArg(1);
		
		return createPair(resultConst, resultFunc, swapArgs);
	}

	
	public static Expr optimize(E_Equals expr) {
		
		/*
		 * for each optimization: apply. If something changed: reapply
		 * optimization
		 */

		expr = new E_Equals(optimizeMM(expr.getArg1()),
				optimizeMM(expr.getArg2()));
		
		//Expr result = expr;

		// Optimize Equals(RdfTerm(.), RdfTerm(.))
		Expr expr2 = optimizeRdfTerm((E_Equals) expr);
		if(expr2 != expr) {
			return optimizeMM(expr2);
		}
		
		Expr result = optimizeEqualsConcat(expr);


		if (result instanceof E_Equals) {
			result = optimizeRdfTerm((E_Equals) result);
		}
		
		if (!result.equals(expr)) {
			result = optimizeMM(result);
		}

		return result;
	}

	/**
	 * Converts op(f(argsA), f(argsB)) -> op1(op2(argsA[1], argsB[2]), ...,
	 * op2(...)) This is mainly used for translating Equals(rdfTerm(argsA),
	 * rdfTerm(argsB)) to And(Equals(argsA[0], argsB[0]), Equals(...), ...)
	 * 
	 * Example: f(argsA) = f(argsB) -> argsA = argsB -> argsA.1 = argsB.1 &&
	 * argsA.2 = argsB.2 && ...
	 * 
	 * TODO: How to account for extra information that might be available on the
	 * constraints on the variables, such as argX.y is a constant?
	 * 
	 * Note: This does not work it many cases for e.g. concat : concat("ab",
	 * "c") = concat("a", "bc")
	 * 
	 * 
	 * 
	 * @param expr
	 * @return
	 */
	public static Expr optimizeRdfTerm(E_Equals expr) {
		Expr result = expr;

		// TODO Check if the arguments are simple or functions
		// An expression such as f(vector1) = f(vector2) can be translated into
		// vector1 = vector2 iff
		// . f is stateless/deterministic.
		// . f is not overloaded (we do not take that case into account)

		Expr a = expr.getArg1();
		Expr b = expr.getArg2();

		if (a.isFunction() && b.isFunction()) {
			ExprFunction fa = a.getFunction();
			ExprFunction fb = b.getFunction();

			FunctionLabel la = fa.getFunctionSymbol();
			FunctionLabel lb = fb.getFunctionSymbol();

			// TODO Vector label
			if (fa.getArgs().size() == fb.getArgs().size() && la.equals(lb)) {

				List<Expr> exprs = new ArrayList<Expr>();
				for (int i = 0; i < fa.getArgs().size(); ++i) {
					Expr ea = fa.getArgs().get(i);
					Expr eb = fb.getArgs().get(i);

					exprs.add(new E_Equals(ea, eb));
				}

				result = ExprUtils.andifyBalanced(exprs);
			}
		}

		return result;
	}

	/**
	 * Merges arguments that are constants together
	 * Used for concat: concat("a", "b", "c") becomes concat("abc");
	 * 
	 * @param concat
	 * @return
	 */
	public static ExprList mergeConsecutiveConstants(Iterable<Expr> exprs) {
		String prev = null;
		ExprList newExprs = new ExprList();

		for (Expr expr : exprs) {
			if (expr.isConstant()) {
				prev = (prev == null ? "" : prev)
						+ expr.getConstant().asString();
			} else {
				if (prev != null) {
					newExprs.add(NodeValue.makeString(prev));
					prev = null;
				}
				newExprs.add(expr);
			}
		}

		if (prev != null) {
			newExprs.add(NodeValue.makeString(prev));
		}

		return newExprs;
	}

	/*
	 * public static String getPrefix(Iterable<Expr> exprs) { String result =
	 * ""; for(Expr expr : exprs) { if (!expr.isConstant()) { break; } result +=
	 * expr.getConstant().asString(); } return result; }
	 */

	public static boolean isConcatExpr(Expr expr) {
		return expr instanceof E_StrConcat || expr instanceof E_StrConcatPermissive; 
	}

	
	public static List<Alignment> toAlignment(List<Expr> a, List<List<Expr>> bs) {
		List<Alignment> result = new ArrayList<Alignment>();
		
		for(List<Expr> b : bs) {
			result.add(new Alignment(a, b));
		}
		
		return result;
	}
	
	public static List<Alignment> align(List<Expr> a, List <Expr> b) {
		List<Alignment> result;		
		List<List<Expr>> tmp = new ArrayList<List<Expr>>();
		
		if(a.size() > b.size()) {
			
			alignRec(a, 0, b, 0, tmp);
			result = toAlignment(a, tmp);
			
		} else if(a.size() < b.size()) {
			
			alignRec(b, 0, a, 0, tmp);
			result = toAlignment(b, tmp);
		} else {
			result = Collections.singletonList(new Alignment(a, b));
		}
		
		return result;
	}
	
	public static int indexOfFirstConstant(List<Expr> a, int offset) {
		for(int i = offset; i < a.size(); ++i) {
			Expr ea = a.get(i);
			
			if(ea.isConstant()) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * 
	 * 
	 * @param a 
	 * @param i index of a constant
	 * @param j position within the string of the constant
	 * @return
	 */
	public static List<Expr> copyReplace(List<Expr> a, int itemIndex, String[] parts) {
		List<Expr> result = new ArrayList<Expr>(a.size() + parts.length - 1);
		
		for(int i = 0; i < itemIndex; ++i) {
			result.add(a.get(i));
		}
		
		for(int i = 0; i < parts.length; ++i) {
			result.add(NodeValue.makeString(parts[i]));
		}

		for(int i = itemIndex + 1; i < a.size(); ++i) {
			result.add(a.get(i));
		}
		
		return result;
		
	}
	
	/*
	public static List<Expr> split(List<Expr> a, int itemIndex, int strIndex, int l) {
		List<Expr> result = new ArrayList<Expr>();
		
		for(int i = 0; i < itemIndex; ++i) {
			result.add(a.get(i));
		}
		
		String str = a.get(itemIndex).getConstant().asUnquotedString();
		if(strIndex > 0) {
			String sub = str.substring(0, strIndex);
			result.add(NodeValue.makeString(sub));
		}
		
		result.add(NodeValue.makeString(str.substring(strIndex, strIndex + l)));
		
		if(strIndex + l < str.length()) {
			result.add(NodeValue.makeString(str.substring(strIndex + l, str.length())));
		}
		
		for(int i = itemIndex + 1; i < a.size(); ++i) {
			result.add(a.get(i));
		}
		
		return result;
	}*/

	public static String[] split(String str, int i, int l) {
		int n = 1;
		
		if(i > 0) {
			n += 1;
		}
		
		if(i + l < str.length()) {
			n += 1;
		}
		
		String[] result = new String[n];
		int j = 0;

		if(i > 0) {
			result[j++] = str.substring(0, i);
		}
		
		result[j++] = str.substring(i, i + l);
		
		if(i + l < str.length()) {
			result[j++] = str.substring(i + l, str.length());
		}
		
		return result;
	}
	
	
	/**
	 * 
	 * @param a The longer array
	 * @param b The shorter one
	 */
	public static void alignRec(List<Expr> a, int oa, List<Expr> b, int ob, List<List<Expr>> result) {

		int i = indexOfFirstConstant(a, oa);
		if(i < 0) {
			result.add(b);
			return;
		}

		Expr ea = a.get(i);	
		String sa = ea.getConstant().asUnquotedString();

		for(int j = ob; j < b.size(); ++j) {
			Expr eb = b.get(j);
			
			if(!eb.isConstant()) {
				continue;
			}
			
			String sb = eb.getConstant().asUnquotedString();
			
			int k = 0;
			while((k = sb.indexOf(sa, k)) >= 0) {
				// Constants must align at beginnig and ending
				if(i == 0 && k != 0) {
					continue;
				}
				
				if(i == a.size() - 1 && k + sa.length() != sb.length()) {
					continue;
				}
				
				String[] parts = split(sb, k, sa.length());			
				List<Expr> subB = copyReplace(b, j, parts);
				
				alignRec(a, i + 1, subB, j + parts.length - 1, result);
				
				k += sa.length();
			}
		}		
	}
	
	
	/**
	 * Optimizes Equals(Concat(argsA), Concat(argsB)) FIXME: An combinations,
	 * where there are constants - Equals(Concat(args), const)
	 * 
	 * //Assumes optimized form (the whole prefix in a single arg)
	 * 
	 * The following cases are being handled: concat(prefixA, restA) =
	 * concat(prefixB, restB) If none of the prefixes is a substring of the
	 * other, the whole expression evaluated to false. otherwise, if the
	 * prefixes are equal, they will be removed. if only one further argument
	 * remains, the concat will be removed.
	 * 
	 * Also, if one of the arguments is a constant, then it is treated as
	 * Concat(const), and theabove rules are applied
	 * 
	 * @param expr
	 * @return
	 */
	public static Expr optimizeEqualsConcat(E_Equals expr) {
		Expr ta = expr.getArg1();
		Expr tb = expr.getArg2();

		// None of the arguments is a concat-expression.
		if (!isConcatExpr(ta) && !isConcatExpr(tb)) {
			return expr;
		}

		// Create a list of concat-arguments (if not a concat, treat the expression
		// as an argument
		List<Expr> la = isConcatExpr(ta) ? ta.getFunction().getArgs()
				: Collections.singletonList(ta);
		List<Expr> lb = isConcatExpr(tb) ? tb.getFunction().getArgs()
				: Collections.singletonList(tb);

		// FIXME For efficiency, this could be done once in a separate step
		la = mergeConsecutiveConstants(la).getList();
		lb = mergeConsecutiveConstants(lb).getList();

		Expr result = optimizeEqualsConcatAlign(la, lb);

		return result;
	}
	
	public static Expr optimizeEqualsConcatAlign(List<Expr> la, List<Expr> lb)
	{
		List<Alignment> cs = align(la, lb);
		
		List<Expr> ors = new ArrayList<Expr>();
		for(Alignment c : cs) {
			
			List<Expr> ands = new ArrayList<Expr>();

			if(c.isSameSize()) {
			
				for(int i = 0; i < c.getKey().size(); ++i) {
					Expr ea = c.getKey().get(i);
					
					/*
					if(i >= c.getValue().size()) {
						System.out.println("OOpps");
					}
					*/
					
					Expr eb = c.getValue().get(i);
					
					if(ea.isConstant() && ea.equals(eb)) {
						continue;
					}
					
					E_Equals eq = new E_Equals(ea, eb);
					ands.add(eq);
				}
			} else {
				ands.add(
					new E_Equals(
						new E_StrConcatPermissive(new ExprList(c.getKey())),
						new E_StrConcatPermissive(new ExprList(c.getValue()))
					));
			}
			
			Expr and = ExprUtils.andifyBalanced(ands);
			ors.add(and);
		}
		
		if(ors.size() == 0) {
			return NodeValue.FALSE;
		}
		
		Expr result = ExprUtils.orifyBalanced(ors);
		
		return result;
	}
	
	public static Expr optimizeEqualsConcat2(List<Expr> la, List<Expr> lb)
	{
		// Remove common prefixes
		for (;;) {
			Expr a = la.get(0);
			Expr b = lb.get(0);
			
			if(a.equals(b)) {
				la.remove(0);
				lb.remove(0);
			} else if(a.isConstant() && b.isConstant()) {
				String sa = a.getConstant().asUnquotedString();
				String sb = b.getConstant().asUnquotedString();
				
				if(sa.startsWith(sb)) {
					String delta = sa.substring(sb.length());
					if(delta.isEmpty()) {
						la.remove(0);
					} else {
						la.set(0, NodeValue.makeString(delta));
					}
					lb.remove(0);
				} else if(sb.startsWith(sa)) {
					String delta = sb.substring(sa.length());
					if(delta.isEmpty()) {
						lb.remove(0);
					} else {
						lb.set(0, NodeValue.makeString(delta));
					}

					la.remove(0);					
					lb.set(0, NodeValue.makeString(delta));
				}
			}
			
			break;
		}
		
		int n = Math.min(la.size(), lb.size());
		
		// Now that the common prefix was removed, we might be left with something like
		// [ "123/separator456" ]
		// [ ?y, "/separator", ?z ]
		// or
		// [ ?a, "/" , ?b ]
		// [ ?x, "/" , ?y ]
		// or
		// [ ?a, " " , ?b , "/", ?c]
		// [ ?x, "/" , ?y ]
		//
		// The task we are now facing is to find an alignment of the constants
		
		
		
		
		boolean sameLength = la.size() == lb.size();
		
		int c = 0;
		for (; c < n; ++c) {
			Expr a = la.get(c);
			Expr b = lb.get(c);
			
			if (a.equals(b)) {
				continue;
			}

			
			/*
			 * if(a.isConstant() && b.isConstant()) {
			 * if(a.getConstant().asString().equals(b.getConstant().asString()))
			 * { continue; } } else if(a.isVariable() && b.isVariable()) {
			 * if(a.equals(b)) { continue; } }
			 */

			break;
		}

		if (sameLength) {
			if (c == n) {
				// Expressions have same length and all arguments were equal
				return NodeValue.TRUE;
			} else if (c + 1 == n) {
				// Except for one argument all others were equal
				return new E_Equals(la.get(c), lb.get(c));
			}
		}

		// Remove the common prefix
		if (c == 0) {
			
			// Zero length common prefix - if both are constants, then the expression
			// can only evaluate to false
			
			if(lb.size() == 0) {
				throw new IndexOutOfBoundsException();
			}
			
			Expr a = la.get(0);
			Expr b = lb.get(0);
			if(a.isConstant() && b.isConstant()) {
				//String sa = a.getConstant().asUnquotedString();
				//String sb = b.getConstant().asUnquotedString();
				return NodeValue.FALSE;
			}

			//return expr;
			return null;
		} else {
			ExprList na = new ExprList();
			ExprList nb = new ExprList();

			for (int i = c; i < la.size(); ++i) {
				na.add(la.get(i));
			}

			for (int i = c; i < lb.size(); ++i) {
				nb.add(lb.get(i));
			}

			return new E_Equals(new E_StrConcat(na), new E_StrConcat(nb));
		}
	}

	/*
	public Expr optimizeCommonPrefix(E_Concat a, E_Concat b) {
		
	}*/
	
	
	public static SqlExpr translateMM(Expr expr) {
		return (SqlExpr) MultiMethod.invokeStatic(SqlExprOptimizer.class,
				"translate", expr);
	}

	public static List<SqlExpr> translateArgs(Iterable<Expr> exprs) {
		List<SqlExpr> result = new ArrayList<SqlExpr>();

		for (Expr arg : exprs) {
			SqlExpr sqlArg = translateMM(arg);
			result.add(sqlArg);
		}
		
		return result;
	}
	
	public static SqlExpr translate(E_Regex expr) {
		List<SqlExpr> sqlExprs = translateArgs(expr.getArgs());
		
		String flags = (sqlExprs.size() == 3) ? sqlExprs.get(2).toString() : "";
		String pattern = sqlExprs.get(1).toString();
		
		return new S_Regex(sqlExprs.get(0), pattern, flags);
	
	}

	public static SqlExpr translate(E_StrConcatPermissive expr) {
		return new S_Concat(translateArgs(expr.getArgs()));
	}

	
	/**
	 * Warning: FIXME This may cause undesired effects if thero is no
	 * appropriate override for Jena
	 * as domain classes such as E_Regex, as it would not be convertet to S_Regex
	 * @param expr
	 * @return
	 */
	public static SqlExpr translate(ExprFunction expr) {
		List<SqlExpr> list = new ArrayList<SqlExpr>();

		for (Expr arg : expr.getArgs()) {
			SqlExpr sqlArg = translateMM(arg);
			list.add(sqlArg);
		}

		
		System.err.println("Warning: 1 No datatype handling - using String");
		return new S_Function(expr.getFunctionSymbol().getSymbol(), list, DatatypeSystemDefault._STRING);
	}

	/**
	 * Overrides ExprFunction 
	 * @param expr
	 * @return
	 */
	public static SqlExpr translate(E_Function expr) {
		List<SqlExpr> list = new ArrayList<SqlExpr>();

		for (Expr arg : expr.getArgs()) {
			SqlExpr sqlArg = translateMM(arg);
			list.add(sqlArg);
		}


		System.err.println("Warning: 2 No datatype handling - using String");
		return new S_Function(expr.getFunctionIRI(), list, DatatypeSystemDefault._STRING);
	}

	
	// TODO We need a mapping of column name to datatype
	public static SqlExprColumn translateVar(Var var)
	{
		String[] parts = var.getName().split("\\.", 2);

		//SqlTable sqlTable = parts.length == 1 ? null : new SqlTable(parts[0]);
		String tableName = parts.length == 1 ? null : parts[0];
		String colName = parts[parts.length - 1];

		System.err.println("Warning: 3 No datatype handling - using String");
		return new SqlExprColumn(tableName, colName, DatatypeSystemDefault._STRING);
	}
	
	/**
	 * 
	 * Currently we translate specially named sparql variables to sql column
	 * references: ?tableAlias.columnName
	 * 
	 * 
	 * @param expr
	 * @return
	 */
	public static SqlExpr translate(ExprVar expr) {
		return translateVar(expr.asVar());
	}

	public static SqlExpr translate(NodeValue expr) {
		
		if (expr.isNumber()) {
			return new SqlExprValue(expr.getDecimal());
		} else if (expr.isBoolean() ){
			return new SqlExprValue(expr.getBoolean());
		} else if (expr.isString()) {
			return new SqlExprValue(expr.asString());
		} else {
			throw new RuntimeException("Unsupported datatype");
		}
	}

	public static SqlExpr translate(E_NotEquals expr) {
		return new S_LogicalNot(translateMM(new E_Equals(expr.getArg1(),
				expr.getArg2())));
	}

	public static SqlExpr translate(E_StrConcat expr) {
		return new S_Concat(translateList(expr.getArgs()));
	}

	public static SqlExpr translate(E_Equals expr) {
		SqlExpr a = translateMM(expr.getArg1());
		SqlExpr b = translateMM(expr.getArg2());

		return S_Equal.create(a, b, datatypeSystem);
	}

	/*
	 * public SqlExpr translate(E_Not expr) {
	 * 
	 * }
	 */

	public static List<SqlExpr> translateList(List<Expr> exprs) {
		List<SqlExpr> result = new ArrayList<SqlExpr>();

		for (Expr item : exprs) {
			result.add(item == null ? null : translateMM(item));
		}

		return result;
	}

	/*
	 * public SqlExpr translateSql(S_Vector expr) { return new
	 * S_Vector(translateSql(expr.getExprs()); }
	 */
}