package org.aksw.sparqlify.core.algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.jena_sparql_api.utils.ExprUtils;
import org.aksw.sparqlify.algebra.sparql.transform.E_SqlFunctionRegistered;
import org.aksw.sparqlify.algebra.sql.exprs.ExprSql;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.config.syntax.FunctionDeclarationTemplate;
import org.aksw.sparqlify.config.syntax.ParamType;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.datatypes.TypeSystem;
import org.aksw.sparqlify.core.datatypes.XClass;
import org.aksw.sparqlify.type_system.MethodSignature;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class FunctionRegistrySql {
    private static final Logger logger = LoggerFactory.getLogger(FunctionRegistrySql.class);

    private TypeSystem datatypeSystem;

    private Multimap<String, RegisteredFunction> registry = HashMultimap.create();


    public FunctionRegistrySql(TypeSystem datatypeSystem) {
        this.datatypeSystem = datatypeSystem;
    }


    public void add(FunctionDeclarationTemplate declaration) {
        String returnTypeName = declaration.getSignature().getReturnTypeName();

        List<ParamType> paramTypeList = declaration.getSignature().getParamTypeList();

        XClass returnType = datatypeSystem.requireByName(returnTypeName);

        List<XClass> parameterTypes = new ArrayList<XClass>();
        for(ParamType paramType : paramTypeList) {
            XClass resPt = datatypeSystem.requireByName(paramType.getDatatypeName());

            parameterTypes.add(resPt);
        }

        MethodSignature<XClass> typeSignature = MethodSignature.create(returnType, parameterTypes, null);

        RegisteredFunction registeredFn = new RegisteredFunction(declaration, typeSignature);

        registry.put(declaration.getSignature().getFunctionName(), registeredFn);

    }

    // Crappy SqlExpr shit... Can we see the SQL level as "on-top" of the Sparql one? So that we can say: SqlExpr(datatype, Expr) ???
    //


    public Collection<RegisteredFunction> lookup(String functionName, Iterable<SqlExpr> sqlArgs) {
        // Get the argument types
        List<TypeToken> argTypes = new ArrayList<TypeToken>();
        for(SqlExpr arg : sqlArgs) {

            if(!(arg instanceof ExprSql)) {
                throw new RuntimeException("Argument does not have an SQL datatype assigned: " + arg + " in function " + functionName);
            }

//			ExprSql sqlArg = (ExprSql)arg;


            argTypes.add(arg.getDatatype());
        }


        Collection<RegisteredFunction> signatures = registry.get(functionName);


        Collection<RegisteredFunction> result = lookup(signatures, argTypes);

        return result;
    }

    public Collection<RegisteredFunction> lookup(Collection<RegisteredFunction> regFns, List<TypeToken> argTypes) {


        // Check if there is an appropriate signature registered
        List<RegisteredFunction> result = new ArrayList<RegisteredFunction>();

        for(RegisteredFunction regFn : regFns) {

            MethodSignature<XClass> signature = regFn.getTypeSignature();

            if(signature.getParameterTypes().size() > argTypes.size()) {
                continue; // Not enough arguments provided
            }


            if(!signature.isVararg() && signature.getParameterTypes().size() < argTypes.size()) {
                continue; // Too many arguments provided
            }

            int n = Math.min(argTypes.size(), signature.getParameterTypes().size());

            boolean isCandidate = true;
            for(int i = 0; i < n ; ++i) {
                TypeToken a = argTypes.get(i);
                TypeToken b = null; //signature.getParameterTypes().get(i);

                logger.warn("Implement the lookup properly - taking the type hierarchy into account");

                if(datatypeSystem.supremumDatatypes(a, b).isEmpty()) {
                    isCandidate = false;
                    continue;
                }


                //datatypeSystem.
            }

            if(isCandidate) {
                result.add(regFn);
            }
        }

        return result;
    }



    public Collection<RegisteredFunction> lookup(ExprFunction fn) {

        String fnId = ExprUtils.getFunctionId(fn);
        Collection<RegisteredFunction> result = null; //lookup(fnId, fn.getArgs());

        return result;
    }

    public ExprFunction transform(String functionName, List<Expr> args) {
        Collection<RegisteredFunction> candidates = null; //lookup(functionName, args);
        ExprFunction result = pickCandidate(candidates, args);
        return result;
    }

    public ExprFunction transform(ExprFunction func, List<Expr> args) {
        String fnId = ExprUtils.getFunctionId(func);
        ExprFunction result = transform(fnId, args);
        return result;

    }

    public ExprFunction transform(ExprFunction func) {
        ExprFunction result = transform(func, func.getArgs());
        return result;
    }


    public ExprFunction pickCandidate(Collection<RegisteredFunction> candidates, List<Expr> args) {
        switch(candidates.size()) {
        case 0: {
            logger.warn("Returning false; although it should be type-error");
            //return new SqlExprValue(false);
            return null;
        }
        case 1: {
            RegisteredFunction regFn = candidates.iterator().next();
            ExprFunction result = new E_SqlFunctionRegistered(regFn, args);
            return result;
            //return new SqlStringTransformerRegisteredFunction(resultFn);
            //return new S_Function(this.sqlFunctionName, args, pair.getKey().getReturnType(), pair.getValue());
        }
        default: {
            logger.warn("Multiple overloads matched: " + candidates);
            logger.warn("Returning false; although it should be type-error");
            //return new SqlExprValue(false);
            return null;
        }
        }
    }
}