package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.jena_sparql_api.views.ExprEvaluatorPartial;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprConstant;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.datatypes.Invocable;
import org.aksw.sparqlify.core.datatypes.SqlMethodCandidate;
import org.aksw.sparqlify.core.datatypes.XClass;

// NOTE This file should become the replacement for the class PushDown

/*
 * TODO Here is my main problem: 
 * 
 * Are Sparql level expressions allowed here at all?
 * 
 * If so, how should it be evaluated? Consider AND(sparql). The arguments could be constants true and false, and we could evaluate it ot false.
 * On the other hand, we could first convert the arguments to SQL constants, and then evaluate the expression in Java on the "SQL" level.
 * 
 * This is a actually a homomorphism: We can either compute a +_dec b in the decimal system and then convert to binary, or first
 * convert the arguments to binary and then add them (using +_bin). In our case we have op_sparql and op_sql.
 * However, at the SQL level we have the datatype information which is (implementation wise) essentially missing on the SPARQL level.
 * 
 * So still: We could have
 * S_Add which does type checking, and if that suceeds, delegates the evaluation to the SPARQL level (i.e. SQL constants exists both as "object" and as "NodeValues.)
 * I guess this makes most sense.
 *  
 *
 *
 */ 


public class ExprEvaluatorSql {
	private ExprEvaluatorPartial subEvaluator;
	
	private FunctionRegistrySql sqlFunctionRegistry;
	
	private TypeSystem datatypeSystem;
	
	
	
	
	public ExprEvaluatorSql(TypeSystem datatypeSystem, FunctionRegistrySql sqlFunctionRegistry) {
		this.datatypeSystem = datatypeSystem;
		this.sqlFunctionRegistry = sqlFunctionRegistry;
	}
	
	public List<SqlExpr> transform(Iterable<SqlExpr> exprs, Map<String, XClass> typeMap) {
		//ExprList result = new ExprList();
		List<SqlExpr> result = new ArrayList<SqlExpr>();
		
		for(SqlExpr expr : exprs) {
			SqlExpr evaledExpr = eval(expr, typeMap);
			result.add(evaledExpr);
		}
		
		return result;
	}

	
	
	/**
	 * Some important fact:
	 * We only derive datatypes during evaluation.
	 * Initially, we have variable definitions which include SPARQL expression with column references, but there is no type information included.
	 * And since these expressions will be transformed anyway, there is no point in prior assignment of datatypes.
	 *     Just condider the concat(... ?int) = concat(... ?name): Eventually, this will be transformed into .... ?int = ... ?name, and only then we will
	 *     discover a potential type error. FIXME: Ups, type errors in equals-concat expressions are evaluated to "false" rather than "type-error".
	 *  
	 * @param expr
	 * @param typeMap
	 * @return
	 */
	public SqlExpr eval(SqlExpr expr, Map<String, XClass> typeMap) {
		
		
		SqlExpr result;
		
		if(expr.isFunction()) {

			SqlExprFunction fn = expr.asFunction();

			List<SqlExpr> transformedArgs = transform(expr.getArgs(), typeMap);
			List<TypeToken> argTypes = new ArrayList<TypeToken>(transformedArgs.size());
			for(SqlExpr arg : transformedArgs) {
				argTypes.add(arg.getDatatype());
			}

			
			//if(true) {throw new RuntimeException("still to fix"); }
			//SqlMethodCandidate<> method = null;
			SqlMethodCandidate method = null;
//			SqlMethodCandidate method = datatypeSystem.lookupMethod(fn.getName(), argTypes);
			if(method == null) {
				throw new RuntimeException("SPARQL Function " + fn.getName() + " not declared");
			}

			Invocable invocable = method.getInvocable();
			if(invocable != null && isConstantsOnly(transformedArgs)) {
				
				Object[] argValues = new Object[transformedArgs.size()];
				for(int i = 0; i < transformedArgs.size(); ++i) {
					SqlExpr tmp = transformedArgs.get(i);
					SqlExprConstant c = tmp.asConstant();
					//NodeValue nodeValue = c.getValue();
					//Object value = NodeValueUtilsSparqlify.getValue(nodeValue);
					SqlValue sqlValue = c.getValue();
					Object value = sqlValue.getValue();
					
					argValues[i] = value; 
				}
				
				
				Object fnResult = invocable.invoke(argValues);
				
				// TODO We need to create a NodeValue from fnResult, howewer
				// we do not know the target RDF datatype
				throw new RuntimeException("Is this still in use?");
				//result = S_Constant.create(fnResult, datatypeSystem);
			} else {
				result = fn;
			}
			
			
		} else if(expr.isConstant()) {

			return expr;
			/*
			NodeValue nv = expr.getConstant();
			

			Object value = ExprUtils.getJavaObject(nv);
			XClass datatype = datatypeSystem.getByClass(value.getClass());
			
			result = new E_SqlNodeValue(nv, datatype);
			*/
			
		} else if(expr.isVariable()) {
			
			/*
			String varName = expr.getVarName();
			
			XClass datatype = typeMap.get(varName);
			if(datatype == null) {
				throw new RuntimeException("No datatype found for " + varName + ". Reasons could be that the referenced column does not exist, or case sensitivity.");
			}
			
			
			result = new E_SqlColumnRef(varName, null, datatype); //null; // ColumnRef(varName, datatype, alias=null)
			
			*/
			
			result = null;
			
		} else {
			throw new RuntimeException("Should not happen");
		}
		
		
		return result;
	}
	
	
	public static boolean isConstantsOnly(Iterable<SqlExpr> exprs) {
		for(SqlExpr expr : exprs) {
			if(!expr.isConstant()) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isConstantArgsOnly(SqlExprFunction fn) {
		
		boolean result = isConstantsOnly(fn.getArgs());

		return result;
	}

}


/*
class SqlPusher
{	
	private static final Logger logger = LoggerFactory.getLogger(SqlPusher.class);
	private static DatatypeSystemOld datatypeSystem = new DatatypeSystemDefault();
	
	public static SqlExpr push(Expr expr, SqlExprList args) {

		if(expr.isFunction()) {
			ExprFunction func = expr.getFunction();
			
			String funcIri = func.getFunctionIRI();
			
			if(funcIri != null) {
				if(funcIri.equals(SparqlifyConstants.urlEncode)) {
					logger.warn("HACK USED: IGNORING URLENCODE");
					return args.get(0);
				}
			}			
			
			
		}
		
		throw new RuntimeException("Should not happen: " + expr + "; " + args);
		//return null;
		//return expr;
	}

		
	public static SqlExpr push(E_StrConcatPermissive expr, SqlExprList args) {
		return new S_Concat(args);		
	}
	
	public static SqlExpr push(E_Bound expr, SqlExprList args) {
		return new S_IsNotNull(args.get(0));
	}
	
	public static SqlExpr push(E_StrConcat expr, SqlExprList args) {
		return new S_Concat(args);		
	}
	
	public static SqlExpr push(E_Equals expr, SqlExprList args) {
		return S_Equals.create(args.get(0), args.get(1), datatypeSystem);
	}

	/*
	public static SqlExpr push(E_Lang expr, SqlExprList args) {
		return args.get(2);
	}* /

	public static SqlExpr push(E_LangMatches expr, SqlExprList args) {
		return S_Equals.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_Add expr, SqlExprList args) {
		return S_Add.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_Subtract expr, SqlExprList args) {
		return S_Substract.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_LogicalAnd expr, SqlExprList args) {
		return new S_LogicalAnd(args.get(0), args.get(1));
	}

	public static SqlExpr push(E_LogicalOr expr, SqlExprList args) {
		return new S_LogicalOr(args.get(0), args.get(1));
	}

	public static SqlExpr push(E_LogicalNot expr, SqlExprList args) {
		return new S_LogicalNot(args.get(0));
	}

	public static SqlExpr push(E_LessThan expr, SqlExprList args) {
		return S_LessThan.create(args.get(0), args.get(1), datatypeSystem);
	}
	
	public static SqlExpr push(E_GreaterThan expr, SqlExprList args) {
		return S_GreaterThan.create(args.get(0), args.get(1), datatypeSystem);
	}
	
	public static SqlExpr push(E_GreaterThanOrEqual expr, SqlExprList args) {
		return S_GreaterThanOrEqual.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_LessThanOrEqual expr, SqlExprList args) {
		return S_LessThanOrEqual.create(args.get(0), args.get(1), datatypeSystem);
	}

	public static SqlExpr push(E_OneOf expr, SqlExprList args) {

		List<SqlExpr> equals = new ArrayList<SqlExpr>();
		
		SqlExpr first = args.get(0);
		for(int i = 1; i < args.size(); ++i) {
			SqlExpr second = args.get(i);
			
			equals.add(S_Equals.create(first, second, datatypeSystem));
		}
		
		if(equals.size() == 1) {
			return equals.get(0);
		}
		
		List<SqlExpr> current = new ArrayList<SqlExpr>();
		List<SqlExpr> next = equals;
		while(next.size() > 1) {
			
			List<SqlExpr> tmp = next;
			next = current;
			current = tmp;
			next.clear();
			
			for(int i = 0; i < current.size(); i+=2) {
				SqlExpr a = current.get(i);

				if(i + 1 >= current.size()) {
					next.add(a);
				} else {
					SqlExpr b = current.get(i + 1);
					next.add(new S_LogicalOr(a, b));
				}				
			}
		}
		

		SqlExpr result = next.get(0);
		return result;
	}
	
	public static SqlExpr push(E_Intersects expr, SqlExprList args) {
		//return new S_Intersects(args.get(0), args.get(1));
		return S_Intersects.create(args.get(0), args.get(1));
	}
	
	public static SqlExpr push(E_GenericSqlExpr expr, SqlExprList args) {
		return expr.getFuncDef().create(args);
	}

	public static SqlExpr push(E_GeographyFromText expr, SqlExprList args) {
		return new S_GeographyFromText(args.get(0));
	}

	public static SqlExpr push(E_GeomFromText expr, SqlExprList args) {
		return new S_GeometryFromText(args.get(0));
	}

	public static SqlExpr push(E_StrDatatype expr, SqlExprList args) {
		if(!(args.get(1) instanceof SqlExprValue)) {
			throw new RuntimeException("Only constants supported for casts");
		}
		
		return S_Cast.create(args.get(0), ((SqlExprValue)args.get(1)).getObject().toString(), datatypeSystem);
	}

	
	public static SqlExpr push(E_Regex expr, SqlExprList args) {		
		String flags = (args.size() == 3) ? args.get(2).toString() : "";
		String pattern = args.get(1).toString();
		
		return new S_Regex(args.get(0), pattern, flags);
	
	}


}
*/
