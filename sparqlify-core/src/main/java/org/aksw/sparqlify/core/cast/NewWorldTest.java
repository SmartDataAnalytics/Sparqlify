package org.aksw.sparqlify.core.cast;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.commons.util.factory.Factory2;
import org.aksw.commons.util.MapReader;
import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_Compare;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_Equals;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_LogicalAnd;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerDefault;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerOp1;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerOp2;
import org.aksw.sparqlify.algebra.sql.exprs2.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.exprs2.S_GreaterThan;
import org.aksw.sparqlify.algebra.sql.exprs2.SqlExpr;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.ExprSqlRewrite;
import org.aksw.sparqlify.core.algorithms.SqlTranslationUtils;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.datatypes.SparqlFunctionImpl;
import org.aksw.sparqlify.expr.util.NodeValueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.util.ExprUtils;
import com.hp.hpl.jena.vocabulary.XSD;

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


interface SqlValueTransformer {
	SqlValue transform(SqlValue nodeValue) throws CastException;
}

class SqlValueTransformerInteger
	implements SqlValueTransformer
{
	/**
	 * TODO The transformer should be able to cast to specific subtypes of int
	 * so: How to handle e.g. the case: (int2)1234 ???
	 * 
	 * In the generic case, we would need something such as e.g.
	 * castToInt(int byteCount, Object value)
	 * 
	 * @param sqlValue
	 * @return
	 * @throws CastException
	 */
	@Override
	public SqlValue transform(SqlValue sqlValue) throws CastException {
		
		
		String str = "" + sqlValue.getValue(); 
		//TypeMapper tm = TypeMapper.getInstance();

		//String typeName = TypeToken.Int.toString();
		Integer v;
		try {
			v = Integer.parseInt(str);
		} catch(NumberFormatException e) {
			throw new CastException("Could not cast " + str + " to integer");
		}
		
		
		SqlValue result = new SqlValue(TypeToken.Int, v);
		// String typeName = XSD.integer.toString();
		//RDFDatatype dt = tm.getSafeTypeByName(typeName);

		//Node node = Node.createLiteral(str, dt);
		//NodeValue result = NodeValue.makeNode(node);
		return result;
	}
}



class NodeValueTransformerInteger implements NodeValueTransformer {
	@Override
	public NodeValue transform(NodeValue nodeValue) throws CastException {
		String str = "" + NodeValueUtils.getValue(nodeValue);
		TypeMapper tm = TypeMapper.getInstance();

		String typeName = TypeToken.Int.toString();
		// String typeName = XSD.integer.toString();
		RDFDatatype dt = tm.getSafeTypeByName(typeName);

		Node node = Node.createLiteral(str, dt);
		NodeValue result = NodeValue.makeNode(node);
		return result;
	}
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
	
	public static TypeSystem createDefaultDatatypeSystem() {

		// String basePath = "src/main/resources";
		try {
			Map<String, String> typeNameToClass = MapReader
					.readFromResource("/type-class.tsv");
			Map<String, String> typeNameToUri = MapReader
					.readFromResource("/type-uri.tsv");
			
			Map<String, String> typeHierarchy = MapReader
					.readFromResource("/type-hierarchy.default.tsv");

			Map<String, String> typeMap = MapReader
					.readFromResource("/type-map.h2.tsv");

			// TODO HACK Do not add types programmatically 
			typeMap.put("INTEGER", "int");
			
			typeHierarchy.putAll(typeMap);
			
			
			TypeSystem result = TypeSystemImpl.create(typeHierarchy);

			initSparqlModel(result);

			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create the SPARQL and SQL models
	 * 
	 * 
	 * 
	 */
	public static void initSparqlModel(TypeSystem typeSystem) {

		// NodeValue xxx = NodeValue.makeInteger(1);
		// RDFDatatype yyy = xxx.asNode().getLiteral().getDatatype();
		// System.out.println(yyy);

		S_Constant x;

		// SIGH, Jena does not support assigning custom URIs to its default
		// datatypes.
		// Register type token datatypes
		TypeMapper tm = TypeMapper.getInstance();
		String xxx = "http://mytype.org/foo/bar";

//		RDFDatatype inner = new XSDBaseNumericType("int", BigInteger.class);
//		RDFDatatype i = new RDFDatatypeCustomUri("int", inner);

		
		SqlTypeMapper stm = typeSystem.getSqlTypeMapper();
		stm.register(XSD.xstring.getURI(), new SqlDatatypeDefault(TypeToken.String, new NodeValueToObjectDefault()));
		stm.register(XSD.xboolean.getURI(), new SqlDatatypeDefault(TypeToken.Boolean, new NodeValueToObjectDefault()));

		stm.register(SparqlifyConstants.nvTypeError.asNode().getLiteralDatatypeURI(), new SqlDatatypeConstant(SqlValue.TYPE_ERROR));

		
		// RDFDatatype i = new XSDBaseNumericType(TypeToken.Int.toString(),
		// BigInteger.class);

//		tm.registerDatatype(i);

		CoercionSystemImpl3 cs = (CoercionSystemImpl3) typeSystem
				.getCoercionSystem();


		cs.registerCoercion(TypeToken.alloc(XSD.integer.toString()),
				TypeToken.Int, new SqlValueTransformerInteger());

		cs.registerCoercion(TypeToken.String, TypeToken.alloc("int8"),
				new SqlValueTransformerInteger());

		cs.registerCoercion(TypeToken.String, TypeToken.alloc("int4"),
				new SqlValueTransformerInteger());

		cs.registerCoercion(TypeToken.String, TypeToken.alloc("int"),
				new SqlValueTransformerInteger());

		
		
		// TODO Finally clean up the TypeSystem 
		// The coercion system is still a hack...
		cs.registerCoercion(TypeToken.String, TypeToken.alloc("INTEGER"),
				new SqlValueTransformerInteger());

		// FunctionRegistry functionRegistry = new FunctionRegistry();

		ExprBindingSubstitutor exprBindingSubstitutor = new ExprBindingSubstitutorImpl();

		// Eliminates rdf terms from Expr (this is datatype independent)
		ExprEvaluator exprTransformer = SqlTranslationUtils
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

		// ExprEvaluator exprEvaluator = new
		// ExprEvaluatorPartial(functionRegistry, typedExprTransformer)

		// {
		// Method m = DefaultCoercions.class.getMethod("toDouble",
		// Integer.class);
		// XMethod x = XMethodImpl.createFromMethod("toDouble", ds, null, m);
		// ds.registerCoercion(x);
		// }
		//
		// /*
		// * Methods that can only be rewritten
		// */
		//
		//
		// // For most of the following functions, we can rely on Jena for their
		// // evaluation
		// ExprEvaluator evaluator = new ExprEvaluatorJena();
		//
		//
		{
			// 1. Define the signature, e.g. boolean = (rdfTerm, rdfTerm)
			MethodSignature<TypeToken> sig = MethodSignature.create(false,
					TypeToken.Boolean, TypeToken.rdfTerm, TypeToken.rdfTerm);

			// 2. Attach Jena's evaluator to it (for constant expressions)
			// (already defined globally)

			// 3. Attach a typeEvaluator to it.
			// TODO Should we skip step 2 in favor of this?

			SqlExprEvaluator evaluator = new SqlExprEvaluator_Equals(typeSystem);

			// As a fallback where Jena can't evaluate it, register a
			// transformation to an SQL expression.
			SparqlFunction f = new SparqlFunctionImpl("=", sig, evaluator, null);

			typeSystem.registerSparqlFunction(f);

			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("=");
			serializerSystem.addSerializer("equals", serializer);

			// result = new S_Serialize(TypeToken.Boolean, "AND",
			// Arrays.asList(a, b), serializer);

		}

		{
			MethodSignature<TypeToken> sig = MethodSignature.create(false,
					TypeToken.Boolean, TypeToken.rdfTerm, TypeToken.rdfTerm);


			Factory2<SqlExpr> exprFactory = new Factory2<SqlExpr>() {
				@Override
				public SqlExpr create(SqlExpr a, SqlExpr b) {
					return new S_GreaterThan(a, b);
				}
			};
			
			SqlExprEvaluator evaluator = new SqlExprEvaluator_Compare(typeSystem, exprFactory);

			// As a fallback where Jena can't evaluate it, register a
			// transformation to an SQL expression.
			SparqlFunction f = new SparqlFunctionImpl(">", sig, evaluator, null);

			typeSystem.registerSparqlFunction(f);

			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2(">");
			serializerSystem.addSerializer("greaterThan", serializer);
			
		}
		
		// SqlExprSerializerPostgres
		{
			MethodSignature<TypeToken> sig = MethodSignature.create(false,
					TypeToken.Boolean, TypeToken.Boolean, TypeToken.Boolean);
			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalAnd();

			SparqlFunction f = new SparqlFunctionImpl("&&", sig, evaluator,
					null);
			typeSystem.registerSparqlFunction(f);
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2(
					"AND");
			serializerSystem.addSerializer("logicalAnd", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerDefault("IS NOT NULL");
			serializerSystem.addSerializer("isNotNull", serializer);
		}
		
		{
			MethodSignature<TypeToken> sig = MethodSignature.create(false,
					TypeToken.Boolean, TypeToken.Boolean, TypeToken.Boolean);
			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalOr();

			SparqlFunction f = new SparqlFunctionImpl("||", sig, evaluator,
					null);
			typeSystem.registerSparqlFunction(f);
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2(
					"OR");
			serializerSystem.addSerializer("logicalOr", serializer);

		}

		{
			MethodSignature<TypeToken> sig = MethodSignature.create(false,
					TypeToken.Boolean, TypeToken.Boolean);
			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalNot();

			SparqlFunction f = new SparqlFunctionImpl("!", sig, evaluator, null);
			typeSystem.registerSparqlFunction(f);
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1(
					"NOT");
			serializerSystem.addSerializer("logicalNot", serializer);
		}


		// urlEncode
		{
			MethodSignature<TypeToken> sig = MethodSignature.create(false,
					TypeToken.String, TypeToken.String);
			//SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalNot();
			SqlExprEvaluator evaluator = null;

			SparqlFunction f = new SparqlFunctionImpl(SparqlifyConstants.urlEncode, sig, evaluator, null);
			typeSystem.registerSparqlFunction(f);
			//SqlFunctionSerializer serializer = new SqlFunctionSerializerPassThrough();
			//serializerSystem.addSerializer(SparqlifyConstants.urlEncode, serializer);
		}
		
		
		
		// {
		// MethodSignature<String> sig = MethodSignature.create(false,
		// SparqlifyConstants.numericTypeLabel,
		// SparqlifyConstants.numericTypeLabel,
		// SparqlifyConstants.numericTypeLabel);
		// SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalOr();
		//
		// SparqlFunction f = new SparqlFunctionImpl("or", sig, evaluator,
		// null);
		// typeSystem.registerSparqlFunction(f);
		// SqlFunctionSerializer serializer = new
		// SqlFunctionSerializerOp2("OR");
		// serializerSystem.addSerializer("or", serializer);
		//
		// }

		{
			// MethodSignature<String> sig = MethodSignature.create(false,
			// SparqlifyConstants.numericTypeLabel,
			// SparqlifyConstants.numericTypeLabel,
			// SparqlifyConstants.numericTypeLabel);
			// SqlExprEvaluator evaluator = new
			// SqlExprEvaluator_Arithmetic(typeSystem);
			//
			// XSDFuncOp.add(nv1, nv2);
			// XSDFuncOp.classifyNumeric(fName, nv);
			//
			// // As a fallback where Jena can't evaluate it, register a
			// transformation to an SQL expression.
			// SparqlFunction f = new SparqlFunctionImpl("+", sig, evaluator,
			// null);
			// typeSystem.registerSparqlFunction(f);
			// SqlFunctionSerializer serializer = new
			// SqlFunctionSerializerOp2("+");
			// serializerSystem.addSerializer("+", serializer);
		}

		// SqlTranslationUtils;

		// Expr e0 = ExprUtils.parse("?a + ?b = ?a");
		// Expr e0 =
		// ExprUtils.parse("<http://aksw.org/sparqlify/typedLiteral>(?c + ?d, 'http://www.w3.org/2001/XMLSchema#double')");
		// Expr e0 = ExprUtils.parse("?a = ?b || ?b = ?a && !(?a = ?a)");
		Expr e0 = ExprUtils.parse("?e = '1'");

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
		// Expr e = ExprUtils.parse("");

		Map<Var, Expr> binding = new HashMap<Var, Expr>();
		binding.put(Var.alloc("a"), a);
		binding.put(Var.alloc("b"), b);
		binding.put(Var.alloc("c"), c);
		binding.put(Var.alloc("d"), d);
		binding.put(Var.alloc("e"), e);

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

		Expr e2 = exprTransformer.transform(e1);
		logger.debug("[ExprRewrite Phase 2]: " + e2);

		// This step should must be generalized to return an SqlExprRewrite

		ExprSqlRewrite e3 = typedExprTransformer.rewrite(e2, typeMap);
		logger.debug("[ExprRewrite Phase 3]: " + e3);

		Expr et = e3.getExpr();
		if (et instanceof ExprSqlBridge) {

			ExprSqlBridge bridge = (ExprSqlBridge) et;

			SqlExpr ex = bridge.getSqlExpr();

			String e4 = serializerSystem.serialize(ex);
			logger.debug("[ExprRewrite Phase 4]: " + e4);
		} else {

			logger.debug("Done rewriting: ");
			logger.debug("" + et);
			logger.debug("" + e3.getProjection());

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

		TypeSystem typeSystem = createDefaultDatatypeSystem();

		System.out.println("Yay so far");

		// SqlTranslator sqlTranslator = new SqlTranslatorImpl(typeSystem);
		// ExprEvaluator exprTransformer =
		// SqlTranslationUtils.createDefaultEvaluator();

	}
}
