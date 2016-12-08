package org.aksw.sparqlify.core.cast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.jena_sparql_api.views.ExprCopy;
import org.aksw.jena_sparql_api.views.SqlTranslationUtils;
import org.aksw.sparqlify.algebra.sql.exprs2.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs2.S_ColumnRef;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Function;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExprFunction;
import org.aksw.sparqlify.algebra.sql.nodes.Projection;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.ExprSqlRewrite;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.sql.expr.evaluation.SqlExprEvaluator;
import org.aksw.sparqlify.type_system.CandidateMethod;
import org.aksw.sparqlify.type_system.FunctionModel;
import org.aksw.sparqlify.type_system.FunctionModelMeta;
import org.aksw.sparqlify.type_system.MethodEntry;
import org.aksw.sparqlify.type_system.MethodSignature;
import org.aksw.sparqlify.type_system.TypeSystemUtils;
import org.apache.jena.sdb.core.Generator;
import org.apache.jena.sdb.core.Gensym;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;



class ExprHolder {
    private Object expr;

    public ExprHolder(SqlExpr expr) {
        this.expr = expr;
    }

    public ExprHolder(Expr expr) {
        this.expr = expr;
    }

    public boolean isSqlExpr() {
        boolean result = this.expr instanceof SqlExpr;
        return result;
    }

    public boolean isExpr() {
        boolean result = this.expr instanceof Expr;
        return result;
    }

    public Expr getExpr() {
        return (Expr)expr;
    }

    public SqlExpr getSqlExpr() {
        return (SqlExpr) expr;
    }

    @Override
    public String toString() {
        return "ExprHolder: " + expr;
    }
}


class RewriteState {
    private Generator genSym;
    private Projection projection = new Projection();

    public RewriteState() {
        this(Gensym.create("s"));
    }

    public RewriteState(Generator genSym) {
        super();
        this.genSym = genSym;
        this.projection = projection;
    }

    public Generator getGenSym() {
        return genSym;
    }
    public Projection getProjection() {
        return projection;
    }
}


/**
 * Computes the datatype of each expression node.
 *
 * TODO: I think this class should not actually transform expressions
 *
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class TypedExprTransformerImpl
    implements TypedExprTransformer
{
    private static final Logger logger = LoggerFactory.getLogger(TypedExprTransformerImpl.class);

    // TODO Get rid of the typeSystem here, and replace it by more fine granular
    // objects
    private TypeSystem typeSystem;
    // private constantConverter

    private SparqlFunctionProvider functionProvider;

    public TypedExprTransformerImpl(TypeSystem typeSystem) { //SparqlFunctionProvider functionProvider) {
        this.typeSystem = typeSystem;
        this.functionProvider = typeSystem;

        //this.constantSqlConverter =
        //this.functionProvider = functionProvider;
    }

    public TypeSystem getTypeSystem()
    {
        return typeSystem;
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

//	public List<ExprHolder> rewriteArgsDefault(ExprFunction fn, Map<String, TypeToken> typeMap, RewriteState state)
//	{
//		List<ExprHolder> evaledArgs = new ArrayList<ExprHolder>();
//		boolean isAllSqlExpr = true;
//		for(Expr arg : fn.getArgs()) {
//
//			ExprHolder evaledArg = rewrite(arg, typeMap, state);
//
//
//			isAllSqlExpr = isAllSqlExpr && evaledArg.isSqlExpr();
//
//			// If an argument evaluated to type error, return type error
//			// TODO: Distinguish between null and type error. Currently we use nvNothing which actually corresponds to NULL
//			// (currently represented with nvNothing - is that safe? - Rather no - see above)
//			/*
//			if(evaledArg.equals(NodeValue.nvNothing)) {
//				return NodeValue.nvNothing;
//			}
//			*/
//
//			evaledArgs.add(evaledArg);
//		}
//
//		return evaledArgs;
//	}
//
//	public List<ExprHolder> rewriteArgsRdfTerm(E_RdfTerm rdfTerm, Map<String, TypeToken> typeMap, RewriteState state) {
//
//		List<Expr> args = rdfTerm.getArgs();
//		List<ExprHolder> rewrittenArgs = new ArrayList<ExprHolder>();
//
//		for(Expr arg : args) {
//			ExprHolder rewrittenArg;
//			if(arg.isConstant()) {
//				rewrittenArg = new ExprHolder(arg); // Do not rewrite constants
//			} else {
//				rewrittenArg = rewrite(arg, typeMap, state);
//			}
//
//			rewrittenArgs.add(rewrittenArg);
//		}
//
//		return rewrittenArgs;
//	}


    public ExprVar allocateVariable(SqlExpr sqlExpr, RewriteState state) {
        String varName = state.getGenSym().next();
        Var var = Var.alloc(varName);
        ExprVar result = new ExprVar(var);

        state.getProjection().put(varName, sqlExpr);

        return result;
    }

    public ExprSqlRewrite rewrite(Expr expr, Map<String, TypeToken> typeMap) {

        E_RdfTerm rdfTerm;
        if(expr.isConstant()) {
            rdfTerm = SqlTranslationUtils.expandConstant(expr);
        } else {
            rdfTerm = SqlTranslationUtils.expandRdfTerm(expr);
        }


        RewriteState state = new RewriteState();

        ExprHolder rewrite = rewrite(rdfTerm, typeMap, state);

        Expr resultExpr;
        if(rewrite.isSqlExpr()) {
            SqlExpr sqlExpr = rewrite.getSqlExpr();
            resultExpr = allocateVariable(sqlExpr, state);
        } else {
            resultExpr = rewrite.getExpr();
        }

        ExprSqlRewrite result = new ExprSqlRewrite(resultExpr, state.getProjection());

        return result;
    }


    public ExprSqlRewrite rewriteOld(Expr expr, Map<String, TypeToken> typeMap) {
        RewriteState state = new RewriteState();

        ExprHolder rewrite = rewrite(expr, typeMap, state);

        Expr resultExpr;
        if(rewrite.isSqlExpr()) {
            resultExpr = new ExprSqlBridge(rewrite.getSqlExpr());
        } else {
            resultExpr = rewrite.getExpr();
        }

        ExprSqlRewrite result = new ExprSqlRewrite(resultExpr, state.getProjection());

        return result;
    }


    public ExprHolder rewrite(Expr expr, Map<String, TypeToken> typeMap, RewriteState state) {

        ExprHolder result;
        if(expr.isConstant()) {
            NodeValue nodeValue = expr.getConstant();
            SqlExpr sqlExpr = translate(nodeValue);
            result = new ExprHolder(sqlExpr);
        }
        else if(expr.isVariable()) {
            ExprVar var = expr.getExprVar();
            SqlExpr sqlExpr = translate(var, typeMap);
            result = new ExprHolder(sqlExpr);
        }
        else if(expr.isFunction()) {
            result = rewrite(expr.getFunction(), typeMap, state);
        } else {
            throw new RuntimeException("Should not happen: " + expr);
        }

        if(result.equals(TypeToken.TypeError)) {
            System.err.println("Got type error for " + expr);
        }

        return result;
    }



    public ExprHolder rewrite(ExprFunction fn, Map<String, TypeToken> typeMap, RewriteState state) {

        ExprHolder result;

        String functionId = ExprUtils.getFunctionId(fn);

        logger.debug("Processing: " + fn);
        /*
        if(containsTypeError(evaledArgs)) {
            logger.debug("Type error in argument (" + evaledArgs + ")");
            return S_Constant.TYPE_ERROR;
        }
        */
        boolean pushConstants = true;

        if(fn instanceof E_RdfTerm) {
            // We always push constants to make data access uniform
            //pushConstants = false;
        }


        List<ExprHolder> evaledArgs = new ArrayList<ExprHolder>();
        boolean isAllSqlExpr = true;
        for(Expr arg : fn.getArgs()) {

            ExprHolder evaledArg;
            if(arg.isConstant() && !pushConstants) {
                evaledArg = new ExprHolder(arg);
            } else {
                evaledArg = rewrite(arg, typeMap, state);
            }




            isAllSqlExpr = isAllSqlExpr && evaledArg.isSqlExpr();

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

        boolean isFnRewritable = isAllSqlExpr;

        if(fn instanceof E_RdfTerm) {
            isFnRewritable = false;
        }

        // Allocate variables for all SqlExprs, pass on Exprs
        if(!isFnRewritable) {

            List<Expr> newArgs = new ArrayList<Expr>(evaledArgs.size());

            for(ExprHolder holder : evaledArgs) {

                Expr arg;

                if(holder.isSqlExpr()) {

                    SqlExpr typedExpr = holder.getSqlExpr();
                    arg = allocateVariable(typedExpr, state);

                }  else {

                    arg = holder.getExpr();

                }

                newArgs.add(arg);
            }

            Expr expr = ExprCopy.getInstance().copy(fn, newArgs);

            result = new ExprHolder(expr);

        } else {
            // All arguments are SQL Exprs
            List<SqlExpr> newArgs = new ArrayList<SqlExpr>(evaledArgs.size());

            for(ExprHolder holder : evaledArgs) {
                SqlExpr sqlExpr = holder.getSqlExpr();
                newArgs.add(sqlExpr);
            }

            SqlExpr sqlExpr = processFunction(functionId, newArgs);

            result = new ExprHolder(sqlExpr);
        }


        return result;
    }


    public SqlExpr processFunction(String functionId, List<SqlExpr> newArgs) {

        // There must be a function registered for the argument types

//		String datatype = exprTypeEvaluator.evaluateType(fn);
//		if(datatype == null) {
//			throw new RuntimeException("No datatype could be obtained for " + fn);
//		}
//
//		//result = S_Function.create(datatype, functionId, evaledArgs);



        // Get the types of the evaluated arguments
        List<TypeToken> argTypes = new ArrayList<TypeToken>(newArgs.size());
        for(SqlExpr newArg : newArgs) {
            argTypes.add(newArg.getDatatype());
        }

        FunctionModel<TypeToken> functionModel = typeSystem.getSqlFunctionModel();
        Multimap<String, String> sparqlSqlDecls = typeSystem.getSparqlSqlDecls();

        CandidateMethod<TypeToken> candidate = TypeSystemUtils.lookupSqlCandidate(functionModel, sparqlSqlDecls, functionId, argTypes);


        SqlExpr result = null;
        if(candidate != null) {

            /* Check if we can apply an inverse function:
            /* The rules are:
             * - The function must be a comparator (these implicitly take 2 arguments)
             * - One of the arguments is a function expression whose symbol has an inverse defined
             * - The other operand is a constant
             */
            FunctionModelMeta sqlMetaModel = typeSystem.getSqlFunctionMetaModel();

            String opId = candidate.getMethod().getId();

            // TODO Perform short cut evaluation of logical operators
            if(sqlMetaModel.getLogicalAnds().contains(opId)) {
                SqlExpr a = newArgs.get(0);
                SqlExpr b = newArgs.get(1);

                if(b.isConstant()) {
                    SqlExpr tmp = a;
                    a = b;
                    b = tmp;
                }

                if(a.isConstant() && a.asConstant().equals(S_Constant.TRUE)) {
                    result = b;
                }

            }
//			else if(sqlMetaModel.getLogicalOrs().contains(opId)) {
//
//			}
//			else if(sqlMetaModel.getLogicalNots().contains(opId)) {
//
//			}
            else if(sqlMetaModel.getComparators().contains(opId)) {

                // TODO: We need the name of the comparator
                //typeSystem.getSparqlSqlDecls()

                SqlExpr a = newArgs.get(0);
                SqlExpr b = newArgs.get(1);

                if(b.isConstant()) {
                    SqlExpr tmp = a;
                    a = b;
                    b = tmp;
                }

                if(a.isConstant() && b.isFunction()) {
                    String fnId = b.asFunction().getName();

                    String invId = sqlMetaModel.getInverses().get(fnId);
                    if(invId != null) {
                        Map<String, SqlExprEvaluator> sqlImpls = typeSystem.getSqlImpls();
                        SqlExprEvaluator see = sqlImpls.get(invId);

                        SqlExpr c = b.getArgs().get(0);

                        if(see == null) {
                            throw new RuntimeException("Inverse " + invId + " of " + fnId + " declared, but no implementation provided");
                        }

                        SqlExpr d = see.eval(Collections.singletonList(a));

                        List<SqlExpr> aa = Arrays.asList(d, c);





                        result = processFunction(functionId, aa);
                        // Lookp an comparator for the new argument types

                    }

                }

                // Optimize expressions such as equals(str(int), str(int)) to simply equals(int, int)
                else if(a.isFunction() && b.isFunction()) {
                    SqlExprFunction fnA = a.asFunction();
                    SqlExprFunction fnB = b.asFunction();

                    String nameA = fnA.getName();
                    String nameB = fnB.getName();

                    if(nameA.equals(nameB)) { // TODO ... and if nameA and nameB are injective...


                        //String name = functionModel.getNameById(nameA);

                        List<SqlExpr> argsA = fnA.getArgs();
                        List<SqlExpr> argsB = fnB.getArgs();

                        List<SqlExpr> bb = new ArrayList<SqlExpr>(argsA.size() + argsB.size());
                        bb.addAll(argsA);
                        bb.addAll(argsB);
                        /*
                        //List<TypeToken>
                        List<TypeToken> types = SqlExprUtils.getTypes(fnA.getArgs());
                        List<TypeToken> typesB = SqlExprUtils.getTypes(fnB.getArgs());
                        types.addAll(typesB);

                        CandidateMethod<TypeToken> cd = TypeSystemImpl.lookupSqlCandidate(functionModel, sparqlSqlDecls, name, types);
                        */

                        result = processFunction(functionId, bb);
                    }
                }

            }

            if(result == null) {


                result = createSqlExpr(candidate, newArgs);
            }

        } else {
            // Type error
            logger.info("Yielding type error because no signature found for: " + functionId + " with arguments " + argTypes);
            result = S_Constant.TYPE_ERROR;
            //throw new RuntimeException("Type error.... needs to be handled - No function found: " + functionId + " with argtypes " + argTypes);
        }

        return result;
    }


//	SparqlFunction sparqlFunction = functionProvider.getSparqlFunction(functionId);
//	if(sparqlFunction == null) {
//		throw new RuntimeException("Sparql function not declared: " + functionId);
//	}
//
//	SqlExprEvaluator evaluator = sparqlFunction.getEvaluator();
//
//	logger.debug("Evaluator for '" + functionId + "': " + evaluator);
//
//	// If there is an evaluator, we can pass all arguments to it, and see if it yields a new expression
//	if(evaluator != null) {
//		SqlExpr tmp = evaluator.eval(newArgs);
//		if(tmp != null) {
//			result = new ExprHolder(tmp);
//			//return tmp;
//		} else {
//			throw new RuntimeException("Evaluator yeld null value");
//		}
//	} else {

                // If there is no evaluator, use the default behavior:
//				MethodSignature<TypeToken> signature = sparqlFunction.getSignature();
//				if(signature != null) {
//
//					TypeToken returnType = signature.getReturnType();
//					if(returnType != null) {



                        //SqlExpr tmp = S_Function.create(returnType, functionId, newArgs);

//					} else {
//						throw new RuntimeException("Return type is null: " + signature);
//					}
//
//				} else {
//					throw new RuntimeException("Neither evaluator nor signature found for " + functionId + " in " + fn);
//				}

//			}
            // Check if the functionProvider has a definition for the functionId


    public static SqlExpr createSqlExpr(CandidateMethod<TypeToken> candidate, SqlExpr ... args) {
        return createSqlExpr(candidate, Arrays.asList(args));
    }

    public static SqlExpr createSqlExpr(CandidateMethod<TypeToken> candidate, List<SqlExpr> args) {
        MethodEntry<TypeToken> method = candidate.getMethod();
        List<CandidateMethod<TypeToken>> coercions = candidate.getCoercions();

        List<SqlExpr> newArgs;
        if(coercions != null) {

            // Apply coercions
            newArgs = new ArrayList<SqlExpr>(args.size());
            for(int i = 0; i < args.size(); ++i) {
                SqlExpr arg = args.get(i);
                CandidateMethod<TypeToken> coercion = coercions.get(i);

                SqlExpr newArg;
                if(coercion != null) {
                    newArg = createSqlExpr(coercion, Collections.singletonList(arg));
                } else {
                    newArg = arg;
                }

                newArgs.add(newArg);
            }
        } else {
            newArgs = args;
        }

        TypeToken returnType = method.getSignature().getReturnType();
        String functionId = method.getId();

        SqlExpr result = S_Function.create(returnType, functionId, newArgs);

        return result;
    }

    /**
     * This function requires a type-constructor free expression as input:
     * That is an expression that can be translated directly to SQL -
     * i.e. all bnode/uri/literal type constructors have been removed from it
     *
     *
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
    public SqlExpr translate(ExprFunction fn, Map<String, TypeToken> typeMap) {

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
            SqlExpr evaledArg = translate(arg, typeMap);

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

//		String datatype = exprTypeEvaluator.evaluateType(fn);
//		if(datatype == null) {
//			throw new RuntimeException("No datatype could be obtained for " + fn);
//		}
//
//		//result = S_Function.create(datatype, functionId, evaledArgs);


        SparqlFunction sparqlFunction = functionProvider.getSparqlFunction(functionId);
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
            } else {
                throw new RuntimeException("Evaluator yeld null value");
            }
        }

        // If there is no evaluator, use the default behaviour:
        MethodSignature<TypeToken> signature = sparqlFunction.getSignature();
        if(signature != null) {

            TypeToken returnType = signature.getReturnType();
            if(returnType != null) {

                result = S_Function.create(returnType, functionId, evaledArgs);

            }

        }

        // Check if the functionProvider has a definition for the functionId




        throw new RuntimeException("Neither evaluator nor signature found for " + fn);


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
        SqlValue sqlValue = typeSystem.convertSql(expr);

        SqlExpr result = S_Constant.create(sqlValue);
        return result;
    }

    public SqlExpr translate(ExprVar expr, Map<String, TypeToken> typeMap) {

        String varName = expr.getVarName();
        TypeToken datatype = typeMap.get(varName);


        if(datatype == null) {
            throw new RuntimeException("No datatype found for " + varName);
        }

        IBiSetMultimap<TypeToken, TypeToken> ptm = typeSystem.getPhysicalTypeMap();
        Set<TypeToken> schematicTypes = ptm.get(datatype);

        TypeToken schematicType;
        if(schematicTypes.isEmpty()) {
            schematicType = datatype;
            //typeSystem.get
            //throw new RuntimeException("Physical type " + datatype + " not known");
        } else if(schematicTypes.size() > 1) {
            throw new RuntimeException("Multiple mappings for physical type " + datatype + ": " + schematicTypes);
        } else {
            schematicType = schematicTypes.iterator().next();
        }

        // We need to map the phyical datatype to a schematic one
        // e.g. varchar -> string

        SqlExpr result = new S_ColumnRef(schematicType, varName);
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
    public SqlExpr translate(Expr expr, Map<String, TypeToken> typeMap) {

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

            result = translate(fn, typeMap);

        } else if(expr.isVariable()) {

            result = translate(expr.getExprVar(), typeMap);
        } else {
            throw new RuntimeException("Unknown expression type encountered: " + expr);
        }

        return result;
    }

}
