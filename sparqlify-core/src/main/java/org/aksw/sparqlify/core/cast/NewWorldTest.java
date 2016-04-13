package org.aksw.sparqlify.core.cast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.aksw.sparqlify.algebra.sparql.expr.E_RdfTerm;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.algebra.sql.exprs2.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.ExprSqlRewrite;
import org.aksw.sparqlify.core.transformations.RdfTermEliminatorImpl;
import org.aksw.sparqlify.core.transformations.SqlTranslationUtils;
import org.aksw.sparqlify.type_system.FunctionModel;
import org.aksw.sparqlify.type_system.MethodDeclaration;
import org.aksw.sparqlify.type_system.MethodEntry;
import org.aksw.sparqlify.type_system.MethodSignature;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import com.google.common.base.Function;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

/* This is the SqlExprTransformer
 interface ExprTypeEvaluator {
 SqlExpr eval(Expr expr);
 }

 class ExprTypeEvaluatorImpl
 implements ExprTypeEvaluator
 {
 @Override
 public SqlExpr eval(Expr expr) {
 //ExprEvaluato

 // TODO Auto-generated method stub
 return null;
 }

 }
 */






/*
 class ExprEvaluatorJena
 implements ExprEvaluator
 {
 @Override
 public Expr eval(Expr expr) {
 Expr result = ExprUtils.eval(expr);
 return result;
 }

 @Override
 public Expr eval(Expr expr, Map<Var, Expr> binding) {
 // TODO Auto-generated method stub
 return null;
 }
 }
 */

class SparqlEvaluatorChain implements SqlExprEvaluator {
	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		// TODO Auto-generated method stub
		return null;
	}

}

class SparqlEvaluatorDefault implements SqlExprEvaluator {
	@Override
	public SqlExpr eval(List<SqlExpr> args) {

		// TODO Auto-generated method stub
		return null;
	}
}

class SparqlEvaluatorTypeSystem implements SqlExprEvaluator {

	@Override
	public SqlExpr eval(List<SqlExpr> args) {
		// TODO Auto-generated method stub
		return null;
	}

}


// The question is, whether a SqlValue transformer should be a
// subClassOf SqlExprEvaluator.
interface Foo
	extends SqlExprEvaluator
{
	SqlExpr eval(List<SqlExpr> args);
}


/**
 * Greatly simplified design, which unifies SPARQL and SQL functions.
 * 
 * With this design, SqlExpr should be renamed to TypedExpr: It simply captures
 * the datatype which an expression yields.
 * 
 * 
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 * 
 */
public class NewWorldTest {

	private static final Logger logger = LoggerFactory.getLogger(NewWorldTest.class);
	
	public static <K, V> void putForAll(Map<K, V> map, Collection<K> keys, V value) {
		for(K key : keys) {
			map.put(key, value);
		}
	}
	
	/*
	
	class SparqlSqlModel {
		private FunctionModel<TypeToken> sqlModel;

		Multimap<String, String> sparqlSqlDecls = typeSystem.getSparqlSqlDecls();
		Map<String, SqlExprEvaluator> sqlImpls = typeSystem.getSqlImpls();
		
	}
	*/
	
	
	public static <K, V> V getNotNull(Map<K, V> map, Object key) {
		V result = map.get(key);
		if(result == null) {
			throw new NullPointerException("No entry found for key " + key + " in map " + map);
		}
		return result;
	}

	public static <I, O> MethodDeclaration<O> transform(MethodDeclaration<I> dec, Function<I, O> fn) {
		MethodSignature<O> s = transform(dec.getSignature(), fn);
		MethodDeclaration<O> result = MethodDeclaration.create(dec.getName(), s);
		
		return result;
	}

	public static <I, O> MethodSignature<O> transform(MethodSignature<I> sig, Function<I, O> fn) {
		O returnType = fn.apply(sig.getReturnType());
		
		List<I> items = sig.getParameterTypes();
		List<O> paramTypes = new ArrayList<O>(items.size());
		for(I item : items) {
			O paramType = fn.apply(item);
			paramTypes.add(paramType);
		}
		
		I vat = sig.getVarArgType();
		O varArgType = vat == null ? null : fn.apply(vat); 
		
		MethodSignature<O> result = MethodSignature.create(returnType, paramTypes, varArgType);
		
		return result;
	}

	
	//public static Collection<Entry> process(Collection<Entry>)
	
//	public static FunctionModel<String> deriveRdfTypeModel(FunctionModel<TypeToken> sqlModel, Map<TypeToken, String> typeToUri) {
//		// TODO: Derive the type map
//	
//		//MethodDeclaration
//		
//		Map<String, String> sqlToRdfType = MapReader.readFromResource("/type-uri.tsv");
//		Map<String, String> sqlTypeHierarchy = MapReader.readFromResource("/type-hierarchy.default.tsv");
//		
//		Map<String, String> rdfTypeHierarchy = MapReader.readFromResource("/rdf-type-hierarchy.tsv");
//		
//		FunctionModel<String> result = new FunctionModelImpl<String>(typeHierarchyProvider);
//	}

	public static <I, O> FunctionModel<O> transform(FunctionModel<I> srcModel, FunctionModel<O> dstModel, Function<I, O> fn) {
	

		
		Collection<MethodEntry<I>> sqlMethods = srcModel.getMethodEntries();
		

		
		List<MethodDeclaration<O>> dstDecs = new ArrayList<MethodDeclaration<O>>();
		for(MethodEntry<I> sqlMethod : sqlMethods) {
			MethodDeclaration<I> dec = sqlMethod.getDeclaration();
			
			// Map the id to the SPARQL function of which the SQL method is an implementation
			String sqlFnId = sqlMethod.getId();

			String sparqlFnName = null;
			if(true) {throw new RuntimeException("Not fully implemented");}
			// Map the name back to the sparql function
			
			MethodSignature<I> srcSig = dec.getSignature();
			
			MethodSignature<O> dstSig = transform(srcSig, fn);
			
			MethodDeclaration<O> dstDec = MethodDeclaration.create(sparqlFnName, dstSig);
			
			dstDecs.add(dstDec);
		}
	
		// For every sparqlDec, add it to the sparqlModel
		for(MethodDeclaration<O> sparqlDec : dstDecs) {
			dstModel.registerFunction(sparqlDec);
		}

		return dstModel;
	}
	
	
	
	public static void testExprRewrite(TypeSystem typeSystem)
	{
		
		ExprBindingSubstitutor exprBindingSubstitutor = new ExprBindingSubstitutorImpl();

		// Eliminates rdf terms from Expr (this is datatype independent)
		ExprEvaluator exprEvaluator = SqlTranslationUtils
				.createDefaultEvaluator();

		// Computes types for Expr, thereby yielding SqlExpr
		TypedExprTransformer typedExprTransformer = new TypedExprTransformerImpl(
				typeSystem);

		// Obtain DBMS specific string representation for SqlExpr

		DatatypeToStringPostgres typeSerializer = new DatatypeToStringPostgres();

		SqlLiteralMapper sqlLiteralMapper = new SqlLiteralMapperDefault(
				typeSerializer);
		SqlExprSerializerSystem serializerSystem = new SqlExprSerializerSystemImpl(
				typeSerializer, sqlLiteralMapper);


		// SqlTranslationUtils;

		// Expr e0 = ExprUtils.parse("?a + ?b = ?a");
		// Expr e0 =
		// ExprUtils.parse("<http://aksw.org/sparqlify/typedLiteral>(?c + ?d, 'http://www.w3.org/2001/XMLSchema#double')");
		// Expr e0 = ExprUtils.parse("?a = ?b || ?b = ?a && !(?a = ?a)");
		//Expr e0 = ExprUtils.parse("?e = '1'");

		
		Expr e0 = ExprUtils.parse("?f = <http://aksw.org/sparqlify/urlDecode>('foobar')");
		
		//Expr e0 = ExprUtils.parse("?c < ?d + 1");
		//Expr e0 = ExprUtils.parse("(?e + 1) * (?e + 4)");
		//Expr e0 = ExprUtils.parse("?f = <http://foobar>");
		
		Expr a = ExprUtils.parse("<http://aksw.org/sparqlify/uri>(?website)");
		// Expr b =
		// ExprUtils.parse("<http://aksw.org/sparqlify/plainLiteral>(<http://aksw.org/sparqlify/plainLiteral>(?foo))");
		Expr b = ExprUtils
				.parse("<http://aksw.org/sparqlify/uri>(<http://aksw.org/sparqlify/plainLiteral>(?foo))");

		Expr c = ExprUtils
				.parse("<http://aksw.org/sparqlify/typedLiteral>(1, 'http://www.w3.org/2001/XMLSchema#int')");
		Expr d = ExprUtils
				.parse("<http://aksw.org/sparqlify/typedLiteral>(2, 'http://www.w3.org/2001/XMLSchema#int')");

		Expr e = ExprUtils
				.parse("<http://aksw.org/sparqlify/typedLiteral>(?x, 'http://www.w3.org/2001/XMLSchema#int')");

		Expr f = ExprUtils
				.parse("<http://aksw.org/sparqlify/uri>('http://foobar')");
		// Expr e = ExprUtils.parse("");


		
		Map<Var, Expr> binding = new HashMap<Var, Expr>();
		binding.put(Var.alloc("a"), a);
		binding.put(Var.alloc("b"), b);
		binding.put(Var.alloc("c"), c);
		binding.put(Var.alloc("d"), d);
		binding.put(Var.alloc("e"), e);
		binding.put(Var.alloc("f"), f);

		// ExprHolder aaa;

		Map<String, TypeToken> typeMap = new HashMap<String, TypeToken>();
		typeMap.put("website", TypeToken.String);
		typeMap.put("foo", TypeToken.String);
		typeMap.put("x", TypeToken.Int);
		// typeMap.put("foo", TypeToken.Double);
		// typeMap.put("foo", TypeToken.Double);

		logger.debug("[ExprRewrite Phase 0]: " + e0);

		Expr e1 = exprBindingSubstitutor.substitute(e0, binding);
		logger.debug("[ExprRewrite Phase 1]: " + e1);

		//Expr e2 = exprTransformer.transform(e1);
		//logger.debug("[ExprRewrite Phase 2]: " + e2);

		RdfTermEliminatorImpl exprTrns = SparqlifyCoreInit.createDefaultTransformer(typeSystem);
		E_RdfTerm e2 = exprTrns._transform(e1);
		logger.debug("[ExprRewrite Phase 2]: " + e2);
		
		// This step should must be generalized to return an SqlExprRewrite
		Expr e3 = exprEvaluator.transform(e2);
		logger.debug("[ExprRewrite Phase 3]: " + e3);

		ExprSqlRewrite e4 = typedExprTransformer.rewrite(e3, typeMap);
		logger.debug("[ExprRewrite Phase 4]: " + e4);

		Expr et = e4.getExpr();
		if (et instanceof ExprSqlBridge) {

			ExprSqlBridge bridge = (ExprSqlBridge) et;

			SqlExpr ex = bridge.getSqlExpr();

			String e5 = serializerSystem.serialize(ex);
			logger.debug("[ExprRewrite Phase 5]: " + e5);
		} else {

			logger.debug("Done rewriting: ");
			logger.debug("" + et);
			logger.debug("" + e4.getProjection());

		}

		// How can we express,
		// that + only take arguments of type TypedLiteral? (possibly even
		// "TypedLiteral WHERE datatype is subClassOf Numeric")
		// Btw, I'd like to avoid using a reasoner in the rewriting process,
		// although we could model it with owl I guess

		// The Sparql function '='(?a, ?b) has the substitution
		// typedLiteral(sql:equals(?a, ?b), xsd:boolean) defined.
		// For sql:equals an appropriate serializer is defined.
		//

		//
		//
		//
		// {
		// MethodSignature<TypeToken> signature =
		// MethodSignature.create(TypeToken.Boolean,
		// Arrays.asList(TypeTokenPostgis.Geometry, TypeTokenPostgis.Geometry),
		// null);
		//
		// XMethod x = XMethodImpl.create(ds, "ST_INTERSECTS", signature);
		// ds.registerSqlFunction("http://ex.org/fn/intersects", x);
		// }
		//
		// {
		// SqlExprEvaluator evaluator = new SqlExprEvaluator_Concat();
		// ds.createSparqlFunction("concat", evaluator);
		//
		// /*
		// MethodSignature<TypeToken> signature = MethodSignature.create(true,
		// TypeToken.String, TypeToken.Object);
		//
		// // TODO: We need a serializer for concat
		// XMethod x = XMethodImpl.create(ds, "||", signature);
		// ds.registerSqlFunction("concat", x);
		// */
		// }
		//
		//
		// {
		// SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalAnd();
		// ds.createSparqlFunction("&&", evaluator);
		// }
		//
		// {
		// SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalOr();
		// ds.createSparqlFunction("||", evaluator);
		// }
		//
		// {
		// SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalNot();
		// ds.createSparqlFunction("!", evaluator);
		// }
		//
		//
		// /*
		// {
		// SqlExprEvaluator evaluator = new SqlExprEvaluator_Equals(ds);
		// ds.createSparqlFunction("=", evaluator);
		// }
		// */
		//
		//
		// {
		// String[] compareSymbols = new String[]{"<=", "<", "=", ">", ">="};
		// for(String opSymbol : compareSymbols) {
		// ds.createSparqlFunction(opSymbol, new
		// SqlExprEvaluator_Compare(opSymbol, ds));
		// }
		// }

	}

	public static void main(String[] args) throws IOException {
		RdfViewSystemOld.initSparqlifyFunctions();

		TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();

		String str = "$1$::text --- $rest;separator=\"+\"$";
		ST s = new ST(str, '$', '$');
		
		s.add("rest", Arrays.asList(1, 2, 3));
		
//		ST a = new ST(s);
//		a.add("1", "test");
//		System.out.println("a: " + a.render());
//
//		ST b = new ST(s);
//		//b.add("1", "bar");
//		//b.add("2", "foobar");
//		System.out.println("b: " + b.render());
//		
//		System.out.println("Yay so far");

		
//		STGroup g = new STGroup('$', '$');
//		g.
//		Compiler c = new Compiler(g);
//		CompiledST x = c.compile(str);
//		
//		x.

		// SqlTranslator sqlTranslator = new SqlTranslatorImpl(typeSystem);
		// ExprEvaluator exprTransformer =
		// SqlTranslationUtils.createDefaultEvaluator();

	}
}
