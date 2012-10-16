package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Method;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.DatatypeSystem;
import org.aksw.sparqlify.core.datatypes.SqlMethodCandidate;
import org.aksw.sparqlify.core.datatypes.XClass;
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

	private DatatypeSystem datatypeSystem;

	public SqlTranslatorImpl(DatatypeSystem datatypeSystem) {
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
	
	public static boolean isConstantArgsOnly(ExprFunction fn) {
		
		boolean result = isConstantsOnly(fn.getArgs());

		return result;
	}
	
	
	
	public SqlExpr translate(ExprFunction fn, Map<Var, Expr> binding, Map<String, TypeToken> typeMap) {
		
		SqlExpr result;
		
		List<SqlExpr> evaledArgs = new ArrayList<SqlExpr>();

		if(containsTypeError(evaledArgs)) {
			logger.debug("Type error in argument (" + evaledArgs + ")");
			return S_Constant.TYPE_ERROR;
		}
		
		
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
		
		
		List<TypeToken> argTypes = getTypes(evaledArgs);
		
		// There must be a function registered for the argument types
		String functionId = ExprUtils.getFunctionId(fn);
		SqlMethodCandidate castMethod = datatypeSystem.lookupMethod(functionId, argTypes);
		
		if(castMethod == null) {
			//throw new RuntimeException("No method found for " + fn);
			logger.debug("No method found for " + fn);
			
			return S_Constant.TYPE_ERROR;
		}
		
		result = S_Method.create(castMethod, evaledArgs);

		return result;
	}
	
	
	public SqlExpr translate(NodeValue expr) {
		NodeValue nv = expr.getConstant();
		

		Object value = ExprUtils.getJavaObject(nv);
		XClass datatype = datatypeSystem.getByClass(value.getClass());
		
		SqlExpr result = new S_Constant(value, datatype.getToken());

		return result;
	}
	
	
	public SqlExpr translate(ExprVar expr, Map<Var, Expr> binding, Map<String, TypeToken> typeMap) {
		String varName = expr.getVarName();
		TypeToken typeName = typeMap.get(varName);

		if(typeName == null) {
			throw new RuntimeException("No datatype found for " + varName);
		}
		
		SqlExpr result = new S_ColumnRef(typeName, varName);
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


	@Override
	public Expr translateSql(Expr sparqlExpr, Map<Var, Expr> binding) {
		throw new RuntimeException("Do not use");
	}

}
