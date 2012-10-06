package org.aksw.sparqlify.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.algebra.sparql.transform.E_SqlFunctionRegistered;
import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExpr;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.FunctionDeclaration;
import org.aksw.sparqlify.config.syntax.ParamType;
import org.aksw.sparqlify.core.algorithms.ExprEvaluatorPartial;
import org.aksw.sparqlify.core.algorithms.ExprTransformerMap;
import org.aksw.sparqlify.core.algorithms.RegisteredFunction;
import org.aksw.sparqlify.expr.util.ExprUtils;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprFunction;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;

/*
interface SqlStringTransformer
{
	String transform(S_Function function, List<SqlExpr> args);
}
*/

 
class FunctionTransformRegistry {
	private static final Logger logger = LoggerFactory.getLogger(FunctionTransformRegistry.class);
	
	private DatatypeSystem datatypeSystem;
	
	private Multimap<String, RegisteredFunction> registry = HashMultimap.create();
	
	
	public FunctionTransformRegistry(DatatypeSystem datatypeSystem) {
		this.datatypeSystem = datatypeSystem;
	}
		
	public void add(FunctionDeclaration declaration) {
		String returnTypeName = declaration.getSignature().getReturnTypeName();
		
		List<ParamType> paramTypeList = declaration.getSignature().getParamTypeList();
		
		SqlDatatype returnType = datatypeSystem.requireByName(returnTypeName);
		
		List<SqlDatatype> parameterTypes = new ArrayList<SqlDatatype>();
		for(ParamType paramType : paramTypeList) {
			SqlDatatype resPt = datatypeSystem.requireByName(paramType.getDatatypeName());
			
			parameterTypes.add(resPt);
		}
		
		MethodSignature<SqlDatatype> typeSignature = MethodSignature.create(returnType, parameterTypes);
		
		RegisteredFunction registeredFn = new RegisteredFunction(declaration, typeSignature);
		
		registry.put(declaration.getSignature().getFunctionName(), registeredFn);
		
	}
	
	// Crappy SqlExpr shit... Can we see the SQL level as "on-top" of the Sparql one? So that we can say: SqlExpr(datatype, Expr) ???
	//
	
	
	public Collection<RegisteredFunction> lookup(String functionName, Iterable<Expr> sqlArgs) {
		// Get the argument types
		List<SqlDatatype> argTypes = new ArrayList<SqlDatatype>();
		for(Expr arg : sqlArgs) {
						
			if(!(arg instanceof SqlExpr)) {
				throw new RuntimeException("Argument does not have an SQL datatype assigned: " + arg + " in function " + functionName);
			}
			
			SqlExpr sqlArg = (SqlExpr)arg; 
			
			
			argTypes.add(sqlArg.getDatatype());
		}

		
		Collection<RegisteredFunction> signatures = registry.get(functionName);

		
		Collection<RegisteredFunction> result = lookup(signatures, argTypes);
		
		return result;
	}
	
	public Collection<RegisteredFunction> lookup(Collection<RegisteredFunction> regFns, List<SqlDatatype> argTypes) {
		
		
		// Check if there is an appropriate signature registered
		List<RegisteredFunction> result = new ArrayList<RegisteredFunction>(); 

		for(RegisteredFunction regFn : regFns) {	
			 
			MethodSignature<SqlDatatype> signature = regFn.getTypeSignature();
			
			if(signature.getParameterTypes().size() > argTypes.size()) {
				continue; // Not enough arguments provided
			}
			
			
			if(!signature.isVararg() && signature.getParameterTypes().size() < argTypes.size()) {
				continue; // Too many arguments provided
			}
			
			int n = Math.min(argTypes.size(), signature.getParameterTypes().size());
			
			boolean isCandidate = true;
			for(int i = 0; i < n ; ++i) {
				SqlDatatype a = argTypes.get(i);
				SqlDatatype b = signature.getParameterTypes().get(i);
				
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
		Collection<RegisteredFunction> result = lookup(fnId, fn.getArgs());
				
		return result;
	}
	
	public Expr transform(ExprFunction func) {

		Collection<RegisteredFunction> candidates = lookup(func);
		
		switch(candidates.size()) {
		case 0: {
			logger.warn("Returning false; although it should be type-error");
			//return new SqlExprValue(false);
			return null;
		}
		case 1: {
			RegisteredFunction regFn = candidates.iterator().next(); 
			Expr result = new E_SqlFunctionRegistered(regFn, func.getArgs());
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


public class FunctionDeclarationTests {

	private static final Logger logger = LoggerFactory.getLogger(FunctionDeclarationTests.class);
	
	@Test
	public void test() throws RecognitionException {
		ConfigParser parser = new ConfigParser();

		Config config = parser.parse("PREFIX ex:<http://ex.org/> DECLARE FUNCTION boolean ex:intersects(geometry ?a, geometry ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
		
		ExprTransformerMap exprTransformer = new ExprTransformerMap();	
		ExprEvaluatorPartial evaluator = new ExprEvaluatorPartial(FunctionRegistry.get(), exprTransformer);
		
		System.out.println(config.getFunctionDeclarations());
	}
}
