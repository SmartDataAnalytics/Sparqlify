package org.aksw.sparqlify.test;

import java.util.HashMap;
import java.util.Map;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.jena_sparql_api.views.E_RdfTerm;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.core.algorithms.ExprSqlRewrite;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.SqlTranslatorImpl2;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.Assert;

public class ExprTests {

	
	private static final Logger logger = LoggerFactory.getLogger(ExprTests.class);
	
	private SqlTranslator sqlRewriter;
	private SqlExprSerializerSystem serializerSystem;
	
	public ExprTests() {
		sqlRewriter = SparqlifyUtils.createSqlRewriter();
		TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();

		DatatypeToString typeSerializer = new DatatypeToStringPostgres();
        // SqlEscaper sqlEscaper = new SqlEscaperDoubleQuote();
		SqlCodec sqlEscaper = SqlCodecUtils.createSqlCodecDefault();

		serializerSystem = SparqlifyCoreInit.createSerializerSystem(typeSystem, typeSerializer, sqlEscaper);
	}

	public SqlExpr rewriteToString(Expr expr, Map<Var, Expr> binding, Map<String, TypeToken> typeMap) {
		ExprSqlRewrite rewrite = sqlRewriter.translate(expr, binding, typeMap);
		SqlExpr result = SqlTranslatorImpl2.asSqlExpr(rewrite);
		
		return result;
	}

	@Test
	public void testVarWithConcat() {
		Expr a = new E_RdfTerm(NodeValue.makeDecimal(1), new ExprVar("C_8"), NodeValue.makeString(""), NodeValue.makeString(""));
		Expr b = ExprUtils.parse("<http://aksw.org/sparqlify/uri>(concat('http://example.com/', ?h__6, ';', ?h__1))");

		Expr c = new E_Equals(a, b);
		
		Map<String, TypeToken> typeMap = new HashMap<String, TypeToken>();
		typeMap.put("C_8", TypeToken.String);
		typeMap.put("h__6", TypeToken.String);
		typeMap.put("h__1", TypeToken.String);
		
		ExprSqlRewrite val = sqlRewriter.translate(c, null, typeMap);
		
		System.out.println(val.getExpr());
	}
	
	
	/**
	 * ?e = 1
	 * 
	 * 
	 */
	@Test
	public void testExpr1() {
		
//		TypeMapper tm = TypeMapper.getInstance();
//		RDFDatatype dt = tm.getSafeTypeByName(XSD.integer.toString());
//		Node node = Node.createLiteral(null, dt);		
//		System.out.println(node);
		
		
		Expr[] exprs = new Expr[] {
				ExprUtils.parse("?a = 1"),
				ExprUtils.parse("?b = '1'"),
		};
		
		// Null means skip result
		String[] expecteds = new String[] {
				"(\"x\" = 1)",
				"false"
				//"\"y\" = 1",
		};
		


		Expr a = ExprUtils.parse("<http://aksw.org/sparqlify/typedLiteral>(?x, 'http://www.w3.org/2001/XMLSchema#int')");		
		Expr b = ExprUtils.parse("<http://aksw.org/sparqlify/typedLiteral>(?y, 'http://www.w3.org/2001/XMLSchema#int')");		
		//Expr b = ExprUtils.parse("<http://aksw.org/sparqlify/uri>(<http://aksw.org/sparqlify/plainLiteral>(?foo))");
		Expr c = ExprUtils.parse("<http://aksw.org/sparqlify/typedLiteral>(1, 'http://www.w3.org/2001/XMLSchema#int')");
		Expr d = ExprUtils.parse("<http://aksw.org/sparqlify/typedLiteral>(2, 'http://www.w3.org/2001/XMLSchema#int')");
		Expr e = ExprUtils.parse("<http://aksw.org/sparqlify/typedLiteral>(?x, 'http://www.w3.org/2001/XMLSchema#int')");
		//Expr e = ExprUtils.parse("");
		
		Map<Var, Expr> binding = new HashMap<Var, Expr>();		
		binding.put(Var.alloc("a"), a);
		binding.put(Var.alloc("b"), b);
		binding.put(Var.alloc("c"), c);
		binding.put(Var.alloc("d"), d);
		binding.put(Var.alloc("e"), e);	

		Map<String, TypeToken> typeMap = new HashMap<String, TypeToken>();
		typeMap.put("x", TypeToken.Int);
		typeMap.put("y", TypeToken.alloc("int4"));

		
		for(int i = 0; i < exprs.length; ++i) {
			
			Expr expr = exprs[i];
			logger.debug("Processing Expression: " + expr);
			logger.debug("--------------------------------------------");
			
			String expected = expecteds[i];
			
			
		
			SqlExpr sqlExpr = rewriteToString(expr, binding, typeMap);
			String actual = serializerSystem.serialize(sqlExpr);
			
			logger.debug("Got expression: " + actual + " - expected: " + expected);
			if(expected != null) {
				Assert.assertEquals(expected, actual);
			}
		}

		
		// We check the expected result based on the string serialization
		
		
	}
}
