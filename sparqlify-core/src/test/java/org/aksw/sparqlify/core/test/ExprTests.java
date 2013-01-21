package org.aksw.sparqlify.core.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.ExprSqlRewrite;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.SqlTranslatorImpl2;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.vocabulary.XSD;

public class ExprTests {

	
	private static final Logger logger = LoggerFactory.getLogger(ExprTests.class);
	
	private SqlTranslator sqlRewriter;
	private SqlExprSerializerSystem serializerSystem;
	
	public ExprTests() {
		sqlRewriter = SparqlifyUtils.createSqlRewriter();
		serializerSystem = SparqlifyUtils.createSerializerSystem();
	}

	public SqlExpr rewriteToString(Expr expr, Map<Var, Expr> binding, Map<String, TypeToken> typeMap) {
		ExprSqlRewrite rewrite = sqlRewriter.translate(expr, binding, typeMap);
		SqlExpr result = SqlTranslatorImpl2.asSqlExpr(rewrite);
		
		return result;
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
				ExprUtils.parse("?a = '1'"),
				ExprUtils.parse("?b = '1'"),
		};
		
		// Null means skip result
		String[] expecteds = new String[] {
				"\"x\" = 1",
				"\"y\" = 1",
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
			
			System.out.println("" + actual);
			if(expected != null) {
				Assert.assertEquals(expected, actual);
			}
		}

		
		// We check the expected result based on the string serialization
		
		
	}
}
