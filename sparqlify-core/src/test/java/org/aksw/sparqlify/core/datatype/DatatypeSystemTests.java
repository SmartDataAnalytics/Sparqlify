package org.aksw.sparqlify.core.datatype;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.FunctionDeclaration;
import org.aksw.sparqlify.config.syntax.FunctionDeclarationTemplate;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.ExprEvaluatorPartial;
import org.aksw.sparqlify.core.algorithms.ExprEvaluatorSql;
import org.aksw.sparqlify.core.algorithms.ExprTransformerMap;
import org.aksw.sparqlify.core.algorithms.FunctionRegistrySql;
import org.aksw.sparqlify.core.algorithms.SqlTranslatorImpl;
import org.aksw.sparqlify.core.datatypes.DatatypeSystemCustom;
import org.aksw.sparqlify.core.datatypes.DefaultCoercions;
import org.aksw.sparqlify.core.datatypes.SqlExprOps;
import org.aksw.sparqlify.core.datatypes.XMethod;
import org.aksw.sparqlify.core.datatypes.XMethodImpl;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.function.FunctionRegistry;
import com.hp.hpl.jena.sparql.util.ExprUtils;






class Registry {
	
}

class Ops {
	
	/*
	 * Arithmetic
	 */
	
	// Integer
	
	public static Integer add(Integer a, Integer b) {
		return a + b;
	}

	public static Integer subtract(Integer a, Integer b) {
		return a - b;
	}

	public static Integer multiply(Integer a, Integer b) {
		return a * b;
	}
	
	public static Integer divide(Integer a, Integer b) {
		return a / b;
	}

	// Double
	
	public static Double add(Double a, Double b) {
		return a + b;
	}

	public static Double subtract(Double a, Double b) {
		return a - b;
	}

	public static Double multiply(Double a, Double b) {
		return a * b;
	}
	
	public static Double divide(Double a, Double b) {
		return a / b;
	}
	
	
	/*
	 * In-/Equality
	 */
	

	public static Boolean lessThan(Integer a, Integer b) {
		return a < b;
	}

	public static Boolean lessThanOrEquals(Integer a, Integer b) {
		return a <= b;
	}

	public static Boolean equals(Integer a, Integer b) {
		return a == b;
	}

	public static Boolean greaterThanOrEquals(Integer a, Integer b) {
		return a >= b;
	}

	public static Boolean greaterThan(Integer a, Integer b) {
		return a > b;
	}

	
	public static Boolean lessThan(Double a, Double b) {
		return a < b;
	}

	public static Boolean lessThanOrEquals(Double a, Double b) {
		return a <= b;
	}

	public static Boolean equals(Double a, Double b) {
		return a == b;
	}

	public static Boolean greaterThanOrEquals(Double a, Double b) {
		return a >= b;
	}

	public static Boolean greaterThan(Double a, Double b) {
		return a > b;
	}

	
	public static Boolean lessThan(String a, String b) {
		return a.compareTo(b) < 0;
	}

	public static Boolean lessThanOrEquals(String a, String b) {
		return a.compareTo(b) <= 0;
	}

	public static Boolean equals(String a, String b) {
		return a.equals(b);
	}

	public static Boolean greaterThanOrEquals(String a, String b) {
		return a.compareTo(b) >= 0;
	}

	public static Boolean greaterThan(String a, String b) {
		return a.compareTo(b) > 0;
	}


	public static Boolean logicalAnd(Boolean a, Boolean b) {
		return a && b;
	}

	public static Boolean logicalOr(Boolean a, Boolean b) {
		return a || b;
	}

	public static Boolean logicalNot(Boolean a) {
		return !a;
	}
	

	/*
	public static String myTestFunc(String str, Integer i) {
		return "XXX " + str + ", " + i + " XXX";
	}
	*/


	public static String myTestFunc(String str, Double i) {
		return "xxx " + i + "xxx";
	}

	public static Boolean equalsIgnoreCase(String a, String b) {
		return a.equalsIgnoreCase(b);
	}
}




/*
interface XMethod {
	
}
*/

public class DatatypeSystemTests {

	private static final Logger logger = LoggerFactory.getLogger(DatatypeSystemTests.class);

	
	@Test
	public void test() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, IOException, RecognitionException {

		Object tmp = SqlExprOps.a;
		
		FunctionDeclaration decl;

		DatatypeSystemCustom ds = SparqlifyUtils.createDefaultDatatypeSystem();

		{
			Method m = DefaultCoercions.class.getMethod("toDouble", Integer.class);
			XMethod x = XMethodImpl.createFromMethod("toDouble", ds, null, m);
			//ds.register(x);
			ds.registerCoercion(x);
		}

		
//		S_Constant a = S_Constant.create("http://ex.org/", ds);
//		System.out.println(a);
//
//		S_Constant b = S_Constant.create(1, ds);
//		System.out.println(b);
//
//		S_Constant d = S_Constant.create(3.14, ds);
//		System.out.println(d);

		

		
		
		
		//S_Concat c = S_Concat.create(a, b);
		//System.out.println(c);

		//S_UserFunc c = S_UserFunc.create("http://ex.org/fn/myTestFunc", a, b);
		//System.out.println(c);

		
		// Define a random custom function
		{
			Method m = Ops.class.getMethod("myTestFunc", String.class, Double.class);
			XMethod x = XMethodImpl.createFromMethod("myTestFunc", ds, null, m);
			//SparqlFunction fn = ds.createSparqlFunction("http://ex.org/fn/myTestFunc");
			ds.registerSqlFunction("http://ex.org/fn/myTestFunc", x);
			
		}

		
		{
			//XMethodImpl.createFromMethod("http://example.org/intersects", ds, object, method)
			MethodSignature<TypeToken> signature = MethodSignature.create(TypeToken.Boolean, Arrays.asList(TypeToken.Int, TypeToken.Int), null);
			
			XMethod x = XMethodImpl.create(ds, "ST_INTERSECTS", signature);
			ds.registerSqlFunction("http://ex.org/fn/intersects", x);
			//ds.registerSqlFunction(x);
		}

		

		{
			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalAnd();
			ds.createSparqlFunction("&&", evaluator);
		}

		{
			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalOr();
			ds.createSparqlFunction("||", evaluator);
		}
		
		{
			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalNot();
			ds.createSparqlFunction("!", evaluator);
		}
		
		
		{
			//MethodSignature<TypeToken> signature = MethodSignature.create(TypeToken.Boolean, Arrays.asList(TypeToken.String, TypeToken.String));
			
			//XMethod x = XMethodImpl.create(ds, "equalsIgnoreCase", signature);
			Method m = Ops.class.getMethod("equalsIgnoreCase", String.class, String.class);
			XMethod x = XMethodImpl.createFromMethod("EQUALS_IGNORE_CASE", ds, null, m);
			ds.registerSqlFunction("http://ex.org/fn/equalsIgnoreCase", x);
		}

		
		/*
		{
			Method m = Ops.class.getMethod("myTestFunc", String.class, Double.class);
			XMethod x = XMethodImpl.createFromMethod("myTestFunc", ds, null, m);
			ds.register(x);
		}*/

		
		ExprEvaluatorSql evaluater = new ExprEvaluatorSql(ds, null); //sqlFunctionRegistry);
		//ds.
		//SqlExpr result = evaluater.eval(c, null);
		//System.out.println("Result: " + result);
		
		
		//DatatypeSystem system = TestUtils.createDefaultDatatypeSystem();
		//SqlDatatype integer = system.getByName("integer");
		/*
		SqlDatatype xfloat = system.getByName("float");
		
		Set<SqlDatatype> xxx = system.supremumDatatypes(integer, xfloat);
		System.out.println(xxx);
*/
		
		FunctionRegistrySql sqlRegistry = new FunctionRegistrySql(ds);

		ConfigParser parser = new ConfigParser();

		{
			/**
			 * Actually it is like that:
			 * We have an abstract sparql function, which can be overloaded by several SQL functions.
			 * E.g. the SPARQL function ogc:intersects can be implemented by ST_Intersects for geometries or for geographies.
			 * 
			 */
			
			Config config = parser.parse("PREFIX fn:<http://ex.org/fn/> DECLARE FUNCTION boolean ex:intersects(integer ?a, integer ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
			FunctionDeclarationTemplate fnDecl = config.getFunctionDeclarations().get(0);
			sqlRegistry.add(fnDecl);
		}
		{
			/*
			Config config = parser.parse("PREFIX ex:<http://ex.org/> DECLARE FUNCTION boolean ex:intersects(geometry ?a, geometry ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
			FunctionDeclaration decl = config.getFunctionDeclarations().get(0);
			sqlRegistry.add(decl);
			*/
		}
		
		ExprTransformerMap exprTransformer = new ExprTransformerMap();	
		
		ExprEvaluator evaluatorSql = new ExprEvaluatorPartial(FunctionRegistry.get(), exprTransformer);
		//Expr expr = ExprUtils.parse("<http://ex.org/intersects>(1 + 1, ?a)");
		
		
		Expr expr = ExprUtils.parse("<http://ex.org/fn/equalsIgnoreCase>('a', 'b') && <http://ex.org/fn/intersects>(1 + 1, ?a)");

		Expr evaledExpr = evaluatorSql.eval(expr, null);
		
		
		SqlTranslator sqlTranslator = new SqlTranslatorImpl(ds);
		
		Map<Var, Expr> binding = new HashMap<Var, Expr>();
		
		Map<String, TypeToken> typeMap = new HashMap<String, TypeToken>();
		typeMap.put("a", TypeToken.Int);
		
		
		System.out.println(evaledExpr);
		SqlExpr sqlExpr = sqlTranslator.translate(evaledExpr, binding, typeMap);

		System.out.println("Final: " + sqlExpr);
		
		//S_Method method = (S_Method)sqlExpr;
		//System.out.println(method.getMethod().getSerializer().serialize(Arrays.asList("test", "b")));
	}
}

