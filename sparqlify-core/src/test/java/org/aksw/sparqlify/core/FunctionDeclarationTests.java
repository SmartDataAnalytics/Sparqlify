package org.aksw.sparqlify.core;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.syntax.FunctionDeclarationTemplate;
import org.aksw.sparqlify.core.algorithms.ExprEvaluatorSql;
import org.aksw.sparqlify.core.algorithms.ExprTransformerMap;
import org.aksw.sparqlify.core.algorithms.FunctionRegistrySql;
import org.aksw.sparqlify.core.datatypes.TypeSystem;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;


/*
interface SqlStringTransformer
{
	String transform(S_Function function, List<SqlExpr> args);
}
*/



/*
 * We need the following components:
 * FunctionRegistrySql: Given a SPARQL function call, maps it to an SQL function call
 * 
 * Whatever, the problem is: how to do the evaluation?
 * We have:
 * - the operator symbols and function names
 * - the argument types
 * 
 * Based on this information we need to somehow map it to an appropriate evaluation function.
 * Actually, Jena's approach is not too bad with the .isInteger() .getInteger() methods.
 * However, I don't see it scale to user defined types.
 * 
 * We could define the numeric plus as:
 * +(numeric, numeric) -> numeric
 * but then then we do not have a clear return type (i.e. integer or float)
 * 
 * So we would have to do:
 * +(integer, integer) -> integer
 * +(float, float) -> float
 * 
 * +(float, integer) -> ???
 * Above should eventually result in float, however there is an implict cast involved:
 *     -> +(float, toFloat(integer))
 * 
 * 
 * 
 */
 
public class FunctionDeclarationTests {

	private static final Logger logger = LoggerFactory.getLogger(FunctionDeclarationTests.class);
	
	@Test
	public void test() throws RecognitionException, IOException {
		ConfigParser parser = new ConfigParser();


		/*
		Expr test = ExprUtils.parse("'6'^^<" + XSD.getURI() + "integer> + 1");
		System.out.println(test);
		Expr result = ExprUtils.eval(test);
		System.out.println(result);
		*/
		
		TypeSystem system = SparqlifyUtils.createDefaultDatatypeSystem();
		//SqlDatatype integer = system.getByName("integer");
		/*
		SqlDatatype xfloat = system.getByName("float");
		
		Set<SqlDatatype> xxx = system.supremumDatatypes(integer, xfloat);
		System.out.println(xxx);
*/
		
		FunctionRegistrySql sqlRegistry = new FunctionRegistrySql(system);

		{
			Config config = parser.parse("PREFIX ex:<http://ex.org/> DECLARE FUNCTION boolean ex:intersects(integer ?a, integer ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
			FunctionDeclarationTemplate decl = config.getFunctionDeclarations().get(0);
			sqlRegistry.add(decl);
		}
		{
			/*
			Config config = parser.parse("PREFIX ex:<http://ex.org/> DECLARE FUNCTION boolean ex:intersects(geometry ?a, geometry ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
			FunctionDeclaration decl = config.getFunctionDeclarations().get(0);
			sqlRegistry.add(decl);
			*/
		}
		
		//FunctionSignature signature = new FunctionSignature(functionName, returnTypeName, paramTypeList)
		//FunctionDeclaration dec = new FunctionDeclarationTemplate(signature, template)
		
		ExprTransformerMap exprTransformer = new ExprTransformerMap();	
		//ExprEvaluatorPartial evaluator = new ExprEvaluatorPartial(FunctionRegistry.get(), exprTransformer);
	

		
		ExprEvaluatorSql evaluatorSql = new ExprEvaluatorSql(system, sqlRegistry);
		Expr expr = ExprUtils.parse("<http://ex.org/intersects>(1 + 1, ?a)");
		
		//SqlTranslator sqlTranslator = new SqlTranslatorImpl(system);
		//sqlTranslator.translate(expr, binding, typeMap);
		
		Map<String, SqlDatatype> typeMap = new HashMap<String, SqlDatatype>();
		//typeMap.put("a", integer);
		//evaluatorSql.eval(expr, typeMap);
		
		
		//System.out.println(config.getFunctionDeclarations());
	}
}
