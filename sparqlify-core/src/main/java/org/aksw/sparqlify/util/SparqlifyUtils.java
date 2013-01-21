package org.aksw.sparqlify.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_LogicalNot;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlExprEvaluator_LogicalOr;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerOp1;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerOp2;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.algorithms.ExprDatatypeNorm;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SparqlSqlRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSelectBlockCollectorImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.algorithms.SqlTranslationUtils;
import org.aksw.sparqlify.core.cast.ExprBindingSubstitutor;
import org.aksw.sparqlify.core.cast.ExprBindingSubstitutorImpl;
import org.aksw.sparqlify.core.cast.NewWorldTest;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystemImpl;
import org.aksw.sparqlify.core.cast.SqlLiteralMapper;
import org.aksw.sparqlify.core.cast.SqlLiteralMapperDefault;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.cast.TypedExprTransformer;
import org.aksw.sparqlify.core.cast.TypedExprTransformerImpl;
import org.aksw.sparqlify.core.datatypes.SparqlFunction;
import org.aksw.sparqlify.core.datatypes.SparqlFunctionImpl;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlRewriter;
import org.aksw.sparqlify.core.interfaces.SqlOpSelectBlockCollector;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.h2.jdbcx.JdbcDataSource;


public class SparqlifyUtils {

//	private static final Logger logger = LoggerFactory.getLogger(SparqlifyUtils.class);
//
////	public static InputStream getResourceAsStream(String name) {
////		InputStream result = System.class.getResourceAsStream(name);
////
////		return result;
////	}
////	
////	public static Map<String, String> readMapFromResource(String name)
////			throws IOException
////	{
////		InputStream in = getResourceAsStream(name);
////		if(in == null) {
////			throw new RuntimeException("Resource not found: " + name);
////		}
////
////		Map<String, String> result = MapReader.read(in);
////		
////		return result;
////	}
//	
//	public static DatatypeSystemCustom createDefaultDatatypeSystem() throws IOException {
//		
//		//String basePath = "src/main/resources";
//		Map<String, String> typeNameToClass = MapReader.readFromResource("/type-class.tsv");
//		Map<String, String> typeNameToUri = MapReader.readFromResource("/type-uri.tsv");
//		Map<String, String> typeHierarchy = MapReader.readFromResource("/type-hierarchy.default.tsv");
//		
//		DatatypeSystemCustom result = DatatypeSystemCustom.create(typeNameToClass, typeNameToUri, typeHierarchy, SparqlifyUtils.logger);
//	
//		initDatatypeSystem(result);
//		
//		return result;
//	}
//	
//	/**
//	 * Declares a set of default operators and functions.
//	 * 
//	 * @param ds
//	 */
//	public static void initDatatypeSystem(TypeSystem ds) {
//		try {
//			_initDatatypeSystem(ds);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
//
//	public static void _initDatatypeSystem(TypeSystem ds) throws SecurityException, NoSuchMethodException
//	{
//		{
//			Method m = DefaultCoercions.class.getMethod("toDouble", Integer.class);
//			XMethod x = XMethodImpl.createFromMethod("toDouble", ds, null, m);
//			ds.registerCoercion(x);
//		}
//		
//		/*
//		 * Methods that can only be rewritten
//		 */
//		
//		{
//			MethodSignature<TypeToken> signature = MethodSignature.create(TypeToken.Boolean, Arrays.asList(TypeTokenPostgis.Geometry, TypeTokenPostgis.Geometry), null);
//			
//			XMethod x = XMethodImpl.create(ds, "ST_INTERSECTS", signature);
//			ds.registerSqlFunction("http://ex.org/fn/intersects", x);
//		}		
//
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_Concat();
//			ds.createSparqlFunction("concat", evaluator);
//			
//			/*
//			MethodSignature<TypeToken> signature = MethodSignature.create(true, TypeToken.String, TypeToken.Object);
//			
//			// TODO: We need a serializer for concat
//			XMethod x = XMethodImpl.create(ds, "||", signature);
//			ds.registerSqlFunction("concat", x);
//			*/
//		}		
//
//		
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalAnd();
//			ds.createSparqlFunction("&&", evaluator);
//		}
//
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalOr();
//			ds.createSparqlFunction("||", evaluator);
//		}
//		
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_LogicalNot();
//			ds.createSparqlFunction("!", evaluator);
//		}
//
//		
//		/*
//		{
//			SqlExprEvaluator evaluator = new SqlExprEvaluator_Equals(ds);
//			ds.createSparqlFunction("=", evaluator);
//		}
//		*/
//		
//		
////		{
////			String[] compareSymbols = new String[]{"<=", "<", "=", ">", ">="};
////			for(String opSymbol : compareSymbols) {
////				ds.createSparqlFunction(opSymbol, new SqlExprEvaluator_Compare(opSymbol, ds));
////			}
////		}
//		
////		{
////			//MethodSignature<TypeToken> signature = MethodSignature.create(TypeToken.Boolean, Arrays.asList(TypeToken.String, TypeToken.String));
////			
////			//XMethod x = XMethodImpl.create(ds, "equalsIgnoreCase", signature);
////			Method m = Ops.class.getMethod("equalsIgnoreCase", String.class, String.class);
////			XMethod x = XMethodImpl.createFromMethod("EQUALS_IGNORE_CASE", ds, null, m);
////			ds.registerSqlFunction("http://ex.org/fn/equalsIgnoreCase", x);
////		}
//
//		
//		/*
//		{
//			Method m = Ops.class.getMethod("myTestFunc", String.class, Double.class);
//			XMethod x = XMethodImpl.createFromMethod("myTestFunc", ds, null, m);
//			ds.register(x);
//		}*/
//		
//		//ds.
//		//SqlExpr result = evaluater.eval(c, null);
//		//System.out.println("Result: " + result);
//		
//		
//		//DatatypeSystem system = TestUtils.createDefaultDatatypeSystem();
//		//SqlDatatype integer = system.getByName("integer");
//		/*
//		SqlDatatype xfloat = system.getByName("float");
//		
//		Set<SqlDatatype> xxx = system.supremumDatatypes(integer, xfloat);
//		System.out.println(xxx);
//*/
//		
//		FunctionRegistrySql sqlRegistry = new FunctionRegistrySql(ds);
//
//		ConfigParser parser = new ConfigParser();
//
////		{
////			/**
////			 * Actually it is like that:
////			 * We have an abstract sparql function, which can be overloaded by several SQL functions.
////			 * E.g. the SPARQL function ogc:intersects can be implemented by ST_Intersects for geometries or for geographies.
////			 * 
////			 */
////			
////			Config config = parser.parse("PREFIX fn:<http://ex.org/fn/> DECLARE FUNCTION boolean ex:intersects(integer ?a, integer ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
////			FunctionDeclarationTemplate fnDecl = config.getFunctionDeclarations().get(0);
////			sqlRegistry.add(fnDecl);
////		}
//		{
//			/*
//			Config config = parser.parse("PREFIX ex:<http://ex.org/> DECLARE FUNCTION boolean ex:intersects(geometry ?a, geometry ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
//			FunctionDeclaration decl = config.getFunctionDeclarations().get(0);
//			sqlRegistry.add(decl);
//			*/
//		}		
//	}
//	
//
//	//public Connection 
	public static DataSource createTestDatabase() throws SQLException {
		/*
		 * Database setup
		 * 
		 */		
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:mem:test_mem");
		ds.setUser("sa");
		ds.setPassword("sa");
		 
		Connection conn = ds.getConnection();
		
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS person_to_dept;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS dept;");
		conn.createStatement().executeUpdate("DROP TABLE IF EXISTS person;");
		
		
		conn.createStatement().executeUpdate("CREATE TABLE person (id INT PRIMARY KEY, name VARCHAR, age INT)");
		conn.createStatement().executeUpdate("CREATE TABLE dept (id INT PRIMARY KEY , name VARCHAR)");
		conn.createStatement().executeUpdate("CREATE TABLE person_to_dept (person_id INT, dept_id INT, UNIQUE(person_id, dept_id))");

		conn.createStatement().executeUpdate("INSERT INTO person VALUES (1, 'Anne', 20)");
		conn.createStatement().executeUpdate("INSERT INTO person VALUES (2, 'Bob', 22)");

		conn.createStatement().executeUpdate("INSERT INTO dept VALUES (5, 'Research')");
		conn.createStatement().executeUpdate("INSERT INTO dept VALUES (6, 'Marketing')");
		
		conn.createStatement().executeUpdate("INSERT INTO person_to_dept VALUES (1, 5)");
		conn.createStatement().executeUpdate("INSERT INTO person_to_dept VALUES (2, 6)");

		return ds;
	}
//
//
	public static ViewDefinitionFactory createViewDefinitionFactory(Connection conn, Map<String, String> typeAlias) throws IOException {
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		
		ViewDefinitionFactory result = createViewDefinitionFactory(conn, typeSystem, typeAlias);
		
		return result;
	}
//	
//	
	public static ViewDefinitionFactory createViewDefinitionFactory(Connection conn, TypeSystem datatypeSystem, Map<String, String> typeAlias) throws IOException {
	
		ConfigParser parser = new ConfigParser();

		SchemaProvider schemaProvider = new SchemaProviderImpl(conn, datatypeSystem, typeAlias);
		SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

		ViewDefinitionFactory result = new ViewDefinitionFactory(parser, syntaxBridge);
		
		return result;
	}
	
	
	public static SqlExprSerializerSystem createSerializerSystem() {
		DatatypeToStringPostgres typeSerializer = new DatatypeToStringPostgres();

		SqlLiteralMapper sqlLiteralMapper = new SqlLiteralMapperDefault(
				typeSerializer);
		SqlExprSerializerSystem result = new SqlExprSerializerSystemImpl(
				typeSerializer, sqlLiteralMapper);
		
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("=");
			result.addSerializer("equals", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("AND");
			result.addSerializer("logicalAnd", serializer);

		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("OR");
			result.addSerializer("logicalOr", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1("NOT");
			result.addSerializer("logicalNot", serializer);
	
		}
	
		return result;
	}
	
	
	public static SqlTranslator createSqlRewriter() {
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
	
		
		SqlTranslator result = createSqlRewriter(typeSystem, exprTransformer);
		return result;
	}

		
	public static SqlTranslator createSqlRewriter(TypeSystem datatypeSystem, ExprEvaluator exprTransformer) {

		ExprBindingSubstitutor exprBindingSubstitutor = new ExprBindingSubstitutorImpl();
		


		// Computes types for Expr, thereby yielding SqlExpr
		TypedExprTransformer typedExprTransformer = new TypedExprTransformerImpl(datatypeSystem);

		
		//SqlTranslator sqlTranslator = new SqlTranslatorImpl(datatypeSystem);
		SqlTranslator result = new SqlTranslatorImpl2(exprBindingSubstitutor, exprTransformer, typedExprTransformer);
		
		return result;
	}
	
	/**
	 * Creates a full blown SPARQL SQL rewriter object, comprised of:
	 * - a candidate view selector (TODO rename: actually this object actually returnes a transformed algebra expression; rather than just selecting candidates. 
	 * - a op 
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public static SparqlSqlRewriter createTestRewriter(CandidateViewSelector candidateViewSelector, TypeSystem datatypeSystem) throws SQLException, IOException {		
		
		//DatatypeSystem datatypeSystem = TestUtils.createDefaultDatatypeSystem();
		//ExprTransformer exprTransformer = new ExprTransformerMap();

		// Eliminates rdf terms from Expr (this is datatype independent)		
		
		//TypedExprTransformer sqlTranslatorTmp = new TypedExprTransformerImpl(datatypeSystem);
		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
		
		SqlTranslator sqlTranslator = createSqlRewriter(datatypeSystem, exprTransformer);
		
		
		ExprDatatypeNorm exprNormalizer = new ExprDatatypeNorm();
				
		
		MappingOps mappingOps = new MappingOpsImpl(sqlTranslator, exprNormalizer);
		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
		
		//SqlExprSerializer exprSerializer = new SqlExprSerializerPostgres(); //null /* da */);
		
		
		DatatypeToStringPostgres typeSerializer = new DatatypeToStringPostgres(); 
		SqlLiteralMapper sqlLiteralMapper = new SqlLiteralMapperDefault(typeSerializer);
		//SqlExprSerializerSystem serializerSystem = new SqlExprSerializerSystemImpl(typeSerializer, sqlLiteralMapper);

		SqlExprSerializerSystem serializerSystem = createSerializerSystem();
		
		SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(serializerSystem);
		
		SqlOpSelectBlockCollector collector = new SqlOpSelectBlockCollectorImpl();
		
		SparqlSqlRewriter result = new SparqlSqlRewriterImpl(candidateViewSelector, opMappingRewriter, collector, sqlOpSerializer);

		return result;

	}

	/*
	public static org.aksw.sparqlify.config.syntax.ViewDefinition parse(String str) throws RecognitionException {
		
		ConfigParser parser = new ConfigParser();
		Config config = parser.parse(str, null);
		
		List<org.aksw.sparqlify.config.syntax.ViewDefinition> vds = config.getViewDefinitions();
	
		org.aksw.sparqlify.config.syntax.ViewDefinition result = vds.get(0);
		
		return result;
	}

	public static org.aksw.sparqlify.core.domain.ViewDefinition createViewDefinition(
			String viewDefStr, Connection conn, Map<String, String> typeAlias) throws IOException, RecognitionException {
		
		
		DatatypeSystem datatypeSystem = TestUtils.createDefaultDatatypeSystem();
		SchemaProvider schemaProvider = new SchemaProviderImpl(conn, datatypeSystem, typeAlias);
		SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);
		

		org.aksw.sparqlify.config.syntax.ViewDefinition vd = TestUtils.parse(viewDefStr);
		ViewDefinition result = syntaxBridge.create(vd);

		return result;
	}
	*/
	
}