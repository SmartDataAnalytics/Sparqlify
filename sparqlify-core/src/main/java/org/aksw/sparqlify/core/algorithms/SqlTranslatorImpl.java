package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.datatypes.TypeSystem;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;


/*
interface TranslatorSql2 {
	/**
	 * We use the same Expr object for translating expressions.
	 * The difference is, that variables are assumed to correspond
	 * to column names.
	 * This means, that the expr object has to be evaluated with different semantics.
	 * 
	 * @param sparqlExpr
	 * @param binding A set of variable-expr mappings. May be null.
	 * @return
	 * /
	SqlExpr translate(Expr sparqlExpr, Map<Var, Expr> binding, Map<String, TypeToken> typeMap);
}
*/

/**
 * Evaluator for expressions.
 * If not all of an expressions' variables are bound, it tries to evaluate as much
 * as possible; hence the name "partial" evaluator.
 * 
 * 
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class SqlTranslatorImpl
	implements SqlTranslator
{	
	private static final Logger logger = LoggerFactory.getLogger(SqlTranslatorImpl.class);

	private TypeSystem datatypeSystem;

	public SqlTranslatorImpl(TypeSystem datatypeSystem) {
		this.datatypeSystem = datatypeSystem;
	}
	

	public static List<TypeToken> getTypes(Collection<SqlExpr> sqlExprs) {

		List<TypeToken> result = new ArrayList<TypeToken>(sqlExprs.size());
		for(SqlExpr sqlExpr : sqlExprs) {
			TypeToken typeName = sqlExpr.getDatatype();
			result.add(typeName);
		}
		
		return result;
	}
	
	public static boolean containsTypeError(Iterable<SqlExpr> exprs) {
		for(SqlExpr expr : exprs) {
			if(S_Constant.TYPE_ERROR.equals(expr)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isConstantsOnly(Iterable<Expr> exprs) {
		for(Expr expr : exprs) {
			if(!expr.isConstant()) {
				return false;
			}
		}
		
		return true;
	}

	public static boolean isConstantsOnlySql(Iterable<SqlExpr> exprs) {
		for(SqlExpr expr : exprs) {
			if(!expr.isConstant()) {
				return false;
			}
		}
		
		return true;
	}

	
	public static boolean isConstantArgsOnly(ExprFunction fn) {
		
		boolean result = isConstantsOnly(fn.getArgs());

		return result;
	}
	
	
	/**
	 * TODO How to pass the type error to SPARQL functions,
	 * such as logical AND/OR/NOT, so they get a chance to deal with it?
	 * 
	 * Using the SPARQL level evaluator is not really possible anymore, because we already translated to the SQL level.
	 * 
	 * We could either:
	 * . have special treatment for logical and/or/not
	 *     But then we can't extend the system to our liking
	 * . have an evaluator on the SqlExpr level, rather than the expr level
	 *     Very generic, but can we avoid the duplication with Expr and SqlExpr?
	 *     Probably we can't.
	 *     The expr structure does not allow adding a custom datatype, and mapping it externally turned out to be quite a hassle.
	 *     
	 * 
	 * 
	 * @param fn
	 * @param binding
	 * @param typeMap
	 * @return
	 */
	public SqlExpr translate(ExprFunction fn, Map<Var, Expr> binding, Map<String, TypeToken> typeMap) {
		
		SqlExpr result;
		
		List<SqlExpr> evaledArgs = new ArrayList<SqlExpr>();

		logger.debug("Processing: " + fn);
		/*
		if(containsTypeError(evaledArgs)) {
			logger.debug("Type error in argument (" + evaledArgs + ")");
			return S_Constant.TYPE_ERROR;
		}
		*/
		
		
		for(Expr arg : fn.getArgs()) {				
			SqlExpr evaledArg = translate(arg, binding, typeMap);
			
			// If an argument evaluated to type error, return type error
			// TODO: Distinguish between null and type error. Currently we use nvNothing which actually corresponds to NULL
			// (currently represented with nvNothing - is that safe? - Rather no - see above)
			/*
			if(evaledArg.equals(NodeValue.nvNothing)) {
				return NodeValue.nvNothing;
			}
			*/
			
			evaledArgs.add(evaledArg);
		}

		
//		List<TypeToken> argTypes = getTypes(evaledArgs);
		
		// There must be a function registered for the argument types
		String functionId = ExprUtils.getFunctionId(fn);
		
		SparqlFunction sparqlFunction = datatypeSystem.getSparqlFunction(functionId);
		if(sparqlFunction == null) {
			throw new RuntimeException("Sparql function not declared: " + functionId);
		}
			
			
		SqlExprEvaluator evaluator = sparqlFunction.getEvaluator();
		
		logger.debug("Evaluator for '" + functionId + "': " + evaluator);
		
		// If there is an evaluator, we can pass all arguments to it, and see if it yields a new expression
		if(evaluator != null) {
			SqlExpr tmp = evaluator.eval(evaledArgs);
			if(tmp != null) {
				return tmp;
			}
		}
		
		throw new RuntimeException("No evaluator found for " + fn);

		// If there was no evaluator, or if the evaluator returned null, continue here.
		
//		// TODO: New approach: There must always be an evaluator
//		
//		// If one of the arguments is a type error, we must return a type error.
//		if(containsTypeError(evaledArgs)) {
//			return S_Constant.TYPE_ERROR;
//		}
//		
//		SqlMethodCandidate castMethod = datatypeSystem.lookupMethod(functionId, argTypes);
//		
//		if(castMethod == null) {
//			//throw new RuntimeException("No method found for " + fn);
//			logger.debug("No method found for " + fn);
//			
//			return S_Constant.TYPE_ERROR;
//		}
//		
//		// TODO: Invoke the SQL method's invocable if it exists and all arguments are constants
//		
//		result = S_Method.createOrEvaluate(castMethod, evaledArgs);
//
//		logger.debug("[Result] " + result);
//		
//		return result;
	}
	
	
	public SqlExpr translate(NodeValue expr) {
		SqlExpr result = new S_Constant(expr);
		return result;
//		
//		NodeValue nv = expr.getConstant();
//		
//
//		Object value = ExprUtils.getJavaObject(nv);
//		XClass datatype = datatypeSystem.getByClass(value.getClass());
//		
//		SqlExpr result = new S_Constant(datatype.getToken(), value);
//
//		return result;
	}
	
	
	
	
	
	public SqlExpr translate(ExprVar expr, Map<Var, Expr> binding, Map<String, TypeToken> typeMap) {
		
		SqlExpr result;
		if(binding != null) {
			Var var = expr.asVar();
			Expr definition = binding.get(var);
			result = translate(definition, null, typeMap);
		} else {
			String varName = expr.getVarName();
			TypeToken datatype = typeMap.get(varName);

			if(datatype == null) {
				throw new RuntimeException("No datatype found for " + varName);
			}
			
			result = new S_ColumnRef(datatype, varName); 
		}
		
		return result;
	}
	
	/*
	 * How to best add interceptors (callbacks with transformation) for certain functions?
	 * 
	 * e.g.: concat(foo, concat(?x...)) -> concat(foo, ?x)
	 * lang(rdfterm(2, ?x, ?y, '')) -> ?y
	 * 
	 * The main question is, whether to apply to callback before or after the arguments are evaluated.
	 * 
	 * -> After makes more sense: Then we have constant folder arguments 
	 */
	public SqlExpr translate(Expr expr, Map<Var, Expr> binding, Map<String, TypeToken> typeMap) {
		
		//assert expr != null : "Null pointer exception";
		if(expr == null) {
			throw new NullPointerException();
		}
		
		//System.out.println(expr);
		
		SqlExpr result = null;
		if(expr.isConstant()) {
			
			result = translate(expr.getConstant()); 
			

		} else if(expr.isFunction()) {
			ExprFunction fn = expr.getFunction();
			
			result = translate(fn, binding, typeMap);
			
		} else if(expr.isVariable()) {
			
			result = translate(expr.getExprVar(), binding, typeMap);
			//result = expr;
			
			if(binding != null) {
				Expr boundExpr = binding.get(expr.asVar());
				if(boundExpr != null) {
					result = translate(boundExpr, null, typeMap); // Do not forward the binding
				}
			}
		} else {
			throw new RuntimeException("Unknown expression type encountered: " + expr);
		}

		return result;
	}


	/*
	@Override
	public Expr translateSql(Expr sparqlExpr, Map<Var, Expr> binding) {
		throw new RuntimeException("Do not use");
	}
	*/

}
