package org.aksw.sparqlify.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.commons.util.jdbc.SqlUtils;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.limit.QueryExecutionFactoryLimit;
import org.aksw.jena_sparql_api.timeout.QueryExecutionFactoryTimeout;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerCase;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerElse;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerOp1;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerOp1Prefix;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerOp2;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerPassThrough;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializerWhen;
import org.aksw.sparqlify.algebra.sql.exprs.evaluators.SqlFunctionSerializer_Join;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.ConfiguratorCandidateSelector;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderDummy;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.SparqlifyConstants;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.DatatypeToStringPostgres;
import org.aksw.sparqlify.core.algorithms.ExprDatatypeNorm;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SparqlSqlStringRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSelectBlockCollectorImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.algorithms.ViewDefinitionNormalizerImpl;
import org.aksw.sparqlify.core.cast.ExprBindingSubstitutor;
import org.aksw.sparqlify.core.cast.ExprBindingSubstitutorImpl;
import org.aksw.sparqlify.core.cast.FunctionModel;
import org.aksw.sparqlify.core.cast.NewWorldTest;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystemImpl;
import org.aksw.sparqlify.core.cast.SqlLiteralMapper;
import org.aksw.sparqlify.core.cast.SqlLiteralMapperDefault;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.cast.TypedExprTransformer;
import org.aksw.sparqlify.core.cast.TypedExprTransformerImpl;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriterImpl;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
import org.aksw.sparqlify.core.interfaces.SqlOpSelectBlockCollector;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryEx;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryExImpl;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyExplain;
import org.aksw.sparqlify.core.transformations.RdfTermEliminator;
import org.aksw.sparqlify.core.transformations.SqlTranslationUtils;
import org.antlr.runtime.RecognitionException;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


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
	public static void initTestDatabase(DataSource ds) throws SQLException {
		Connection conn = ds.getConnection();
		try {
			initTestDatabase(conn);
		}
		finally {
			conn.close();
		}		
	}
	
	public static void initTestDatabase(Connection conn) throws SQLException {
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
	}
	

	public static DataSource createTestDatabase() throws SQLException {
		/*
		 * Database setup
		 * 
		 */		
//		JdbcDataSource ds = new JdbcDataSource();
//		ds.setURL("jdbc:h2:mem:test_mem");
//		ds.setUser("sa");
//		ds.setPassword("sa");

		DataSource ds = createDefaultDatabase("testdb");
		initTestDatabase(ds);
		
		return ds;
	}
	
	public static DataSource createDefaultDatabase(String name) {
		JdbcDataSource ds = new JdbcDataSource();
		//ds.setURL("jdbc:h2:mem:" + name + ";mode=postgres");
		ds.setURL("jdbc:h2:mem:" + name + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
		ds.setUser("sa");
		ds.setPassword("sa");

		return ds;
	}

	public static void shutdownH2(DataSource dataSource) throws SQLException {
		Connection conn = dataSource.getConnection();
		conn.createStatement().execute("SHUTDOWN");
	}
	
	public static DataSource createDefaultDatabase(String name, InputStream in) throws SQLException, IOException {
		String sqlStr = StreamUtils.toString(in);
		
		return createDefaultDatabase(name, sqlStr);
	}

	public static DataSource createDefaultDatabase(String name, String sqlStr) throws SQLException {
		DataSource ds = createDefaultDatabase(name);
		
		Connection conn = ds.getConnection();
		try {
			conn.createStatement().executeUpdate(sqlStr);
		} finally {
			conn.close();
		}
		
//		try {
//			Connection c = ds.getConnection();
//			System.out.println(listTables(c));
//		} finally {
//			conn.close();
//		}
		
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
	
	/*
	 * Dummy view definition factory used e.g. for R2RML export
	 */
	public static ViewDefinitionFactory createDummyViewDefinitionFactory(Map<String, String> typeAlias) {
		
		ConfigParser parser = new ConfigParser();
		
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		SchemaProvider schemaProvider = new SchemaProviderDummy(typeSystem, typeAlias);
		SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);
		
		ViewDefinitionFactory result = new ViewDefinitionFactory(parser, syntaxBridge);
		
		return result;
	}
	
	
	public static SqlExprSerializerSystem createSerializerSystem(TypeSystem typeSystem) {
		DatatypeToStringPostgres typeSerializer = new DatatypeToStringPostgres();

		SqlLiteralMapper sqlLiteralMapper = new SqlLiteralMapperDefault(
				typeSerializer);
		SqlExprSerializerSystem result = new SqlExprSerializerSystemImpl(
				typeSerializer, sqlLiteralMapper);
		
		FunctionModel<TypeToken> sqlModel = typeSystem.getSqlFunctionModel();
		
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("+");
			result.addSerializer(sqlModel.getIdsByName("numericPlus"), serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("-");
			result.addSerializer(sqlModel.getIdsByName("numericMinus"), serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("*");
			result.addSerializer(sqlModel.getIdsByName("numericMultiply"), serializer);
		}
		
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("/");
			result.addSerializer(sqlModel.getIdsByName("numericDivide"), serializer);
		}

		
		
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("=");
			result.addSerializer(sqlModel.getIdsByName("equal"), serializer);
		}
		
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializer_Join(" || ");
			result.addSerializer("concat@str", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2(">");
			result.addSerializer(sqlModel.getIdsByName("greaterThan"), serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2(">=");
			result.addSerializer(sqlModel.getIdsByName("greaterThanOrEqual"), serializer);
		}
		
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("<");
			result.addSerializer(sqlModel.getIdsByName("lessThan"), serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("<=");
			result.addSerializer(sqlModel.getIdsByName("lessThanOrEqual"), serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("AND");
			result.addSerializer(sqlModel.getIdsByName("logicalAnd"), serializer);
			result.addSerializer("logicalAnd", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp2("OR");
			result.addSerializer(sqlModel.getIdsByName("logicalOr"), serializer);
			result.addSerializer("logicalOr", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1("NOT");
			result.addSerializer(sqlModel.getIdsByName("logicalNot"), serializer);
			result.addSerializer("logicalNot", serializer);
		}

		
		// HACK: When isNotNull contraints are added based on the schema,
		// these expressions are not passed through the SQL rewriting process
		// Therefore we need this entry
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix(" IS NOT NULL");
			result.addSerializer("isNotNull", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix(" IS NOT NULL");
			result.addSerializer(sqlModel.getIdsByName("isNotNull"), serializer);
		}


		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::float8");
			result.addSerializer("double@str", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::text");
			result.addSerializer("str@double", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerPassThrough();
			result.addSerializer("str@str", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerOp1Prefix("::text");
			result.addSerializer("str@int", serializer);
		}

		
		// Cast is built in
//		{
//			SqlFunctionSerializer serializer = new SqlFunctionSerializerCast();
//			result.addSerializer("cast", serializer);
//		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerWhen();
			//result.addSerializer(sqlModel.getIdsByName("when"), serializer);
			result.addSerializer("when", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerCase();
			//result.addSerializer(sqlModel.getIdsByName("case"), serializer);
			result.addSerializer("case", serializer);
		}

		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerElse();
			//result.addSerializer(sqlModel.getIdsByName("else"), serializer);
			result.addSerializer("else", serializer);
		}
		
		
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializerPassThrough();
			result.addSerializer(SparqlifyConstants.urlEncode, serializer);
		}
		
		{
			SqlFunctionSerializer serializer = new SqlFunctionSerializer() {
				@Override
				public String serialize(List<String> args) {
					return "COUNT(*)";
				}
			};
			
			result.addSerializer("org.aksw.sparqlify.algebra.sql.exprs2.S_AggCount", serializer);
		}


		return result;
	}
	
	
	public static SqlTranslator createSqlRewriter() {
		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		RdfTermEliminator rdfTermEliminator = SqlTranslationUtils.createDefaultTransformer();
		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
	
		
		SqlTranslator result = createSqlRewriter(typeSystem, rdfTermEliminator, exprTransformer);
		return result;
	}

		
	public static SqlTranslator createSqlRewriter(TypeSystem datatypeSystem, RdfTermEliminator rdfTermEliminator, ExprEvaluator exprTransformer) {

		ExprBindingSubstitutor exprBindingSubstitutor = new ExprBindingSubstitutorImpl();
		


		// Computes types for Expr, thereby yielding SqlExpr
		TypedExprTransformer typedExprTransformer = new TypedExprTransformerImpl(datatypeSystem);

		
		//SqlTranslator sqlTranslator = new SqlTranslatorImpl(datatypeSystem);
		SqlTranslator result = new SqlTranslatorImpl2(exprBindingSubstitutor, rdfTermEliminator, exprTransformer, typedExprTransformer);
		
		return result;
	}
	
	
	
	public static Config readConfig(File file) throws IOException, RecognitionException {
		InputStream in = new FileInputStream(file);
		return readConfig(in, null);
	}
	
	public static Config readConfig(InputStream in) throws IOException, RecognitionException {
		return readConfig(in, null);
	}
	
	public static Config readConfig(InputStream in, Logger logger) throws IOException, RecognitionException {
		ConfigParser parser = new ConfigParser();

		try {
			Config result = parser.parse(in, logger);
			return result;
		} finally {
			in.close();
		}
	}
	
	public static Config createConfig(String str, Logger logger) throws IOException, RecognitionException {
		InputStream in = new ByteArrayInputStream(str.getBytes());
		
		Config result = readConfig(in, logger);
		
		return result;
	}
	
	
	public static List<String> listTables(Connection conn) throws SQLException {
		//java.sql.ResultSet rs = conn.createStatement().executeQuery("Select name from STUDENT");
		java.sql.ResultSet rs = conn.createStatement().executeQuery("SELECT table_name FROM information_schema.tables");
		String query = "SELECT table_name FROM information_schema.tables";
		List<String> result = SqlUtils.executeList(conn, query, String.class);
		return result;
	}
	
	
	public static MappingOps createDefaultMappingOps(TypeSystem typeSystem) {
		RdfTermEliminator rdfTermEliminator = SqlTranslationUtils.createDefaultTransformer();
		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
		SqlTranslator sqlTranslator = createSqlRewriter(typeSystem, rdfTermEliminator, exprTransformer);
		
		
		ExprDatatypeNorm exprNormalizer = new ExprDatatypeNorm();
				
		
		MappingOps mappingOps = new MappingOpsImpl(sqlTranslator, exprNormalizer);
		
		return mappingOps;
	}

	public static OpMappingRewriter createDefaultOpMappingRewriter(TypeSystem typeSystem) {
		MappingOps mappingOps = createDefaultMappingOps(typeSystem);
		
		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);

		return opMappingRewriter;
	}

	//public static QueryExecutionFactory
	
	
	public static QueryExecutionFactoryEx createDefaultSparqlifyEngine(DataSource dataSource, Config config, Long maxResultSetSize, Integer maxQueryExecutionTime) throws SQLException, IOException {
		SparqlSqlStringRewriterImpl rewriter = createDefaultSparqlSqlStringRewriter(dataSource, config, maxResultSetSize, maxQueryExecutionTime);
		
		SparqlSqlOpRewriter ssoRewriter = rewriter.getSparqlSqlOpRewriter();
		SqlOpSerializer sqlOpSerializer = rewriter.getSqlOpSerializer();
		
		QueryExecutionFactory qefDefault = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);
		
		if(maxQueryExecutionTime != null) {
			qefDefault = QueryExecutionFactoryTimeout.decorate(qefDefault, maxQueryExecutionTime * 1000);
		}
		
		if(maxResultSetSize != null) {
			qefDefault = QueryExecutionFactoryLimit.decorate(qefDefault, false, maxResultSetSize);
		}
		
		
		QueryExecutionFactory qefExplain = new QueryExecutionFactorySparqlifyExplain(dataSource, ssoRewriter, sqlOpSerializer);
		
		
		QueryExecutionFactoryEx result = new QueryExecutionFactoryExImpl(qefDefault, qefExplain);
		
		return result;
	
	}


	public static SparqlSqlStringRewriterImpl createDefaultSparqlSqlStringRewriter(DataSource dataSource, Config config, Long maxResultSetSize, Integer maxQueryExecutionTime) throws SQLException, IOException {
		RdfViewSystemOld.initSparqlifyFunctions();

		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		//TypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();
		
		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");


		Connection conn = dataSource.getConnection();

		MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(typeSystem);
		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
		//CandidateViewSelectorImpl cvs = new CandidateViewSelectorImpl(mappingOps);

		CandidateViewSelector<ViewDefinition> candidateViewSelector;
		Schema databaseSchema; 
		try {
			SchemaProvider schemaProvider = new SchemaProviderImpl(conn, typeSystem, typeAlias);
			SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

			candidateViewSelector = new CandidateViewSelectorImpl(mappingOps, new ViewDefinitionNormalizerImpl());

		
			//	RdfViewSystem system = new RdfViewSystem2();
			ConfiguratorCandidateSelector.configure(config, syntaxBridge, candidateViewSelector, null);
			
			databaseSchema = Schema.create(conn);
		} finally {
			conn.close();
		}

		

		SparqlSqlOpRewriter ssoRewriter = SparqlifyUtils.createSqlOpRewriter(candidateViewSelector, opMappingRewriter, typeSystem, databaseSchema);
		
		SqlExprSerializerSystem serializerSystem = SparqlifyUtils.createSerializerSystem(typeSystem);
		SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(serializerSystem);

		
		SparqlSqlStringRewriterImpl rewriter = new SparqlSqlStringRewriterImpl(ssoRewriter, sqlOpSerializer);//SparqlifyUtils.createSparqlSqlStringRewriter(ssoRewriter);

		return rewriter;
		
		//SparqlSqlStringRewriter rewriter = SparqlifyUtils.createTestRewriter(candidateViewSelector, opMappingRewriter, typeSystem);

		//SparqlSqlRewriter rewriter = new SparqlSqlRewriterImpl(candidateViewSelector, opMappingRewriter, sqlOpSelectBlockCollector, sqlOpSerializer);

		
	}
	
	public static QueryExecutionFactory createDefaultSparqlifyEngineOld(DataSource dataSource, Config config, Long maxResultSetSize, Long maxQueryExecutionTime) throws SQLException, IOException {
		RdfViewSystemOld.initSparqlifyFunctions();
		
		
		//Connection conn = dataSource.getConnection();

		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		//TypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();
		
		// typeAliases for the H2 datatype
		Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");


		Connection conn = dataSource.getConnection();

		
/*		
		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
		SqlTranslator sqlTranslator = createSqlRewriter(typeSystem, exprTransformer);
		
		
		ExprDatatypeNorm exprNormalizer = new ExprDatatypeNorm();
				
		
		MappingOps mappingOps = new MappingOpsImpl(sqlTranslator, exprNormalizer);
		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
*/
		//OpMappingRewriter opMappingRewriter = createDefaultOpMappingRewriter(typeSystem);
		MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(typeSystem);
		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
		//CandidateViewSelectorImpl cvs = new CandidateViewSelectorImpl(mappingOps);

		CandidateViewSelector<ViewDefinition> candidateViewSelector;
		Schema databaseSchema;
		try {
			SchemaProvider schemaProvider = new SchemaProviderImpl(conn, typeSystem, typeAlias);
			SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

			candidateViewSelector = new CandidateViewSelectorImpl(mappingOps, new ViewDefinitionNormalizerImpl());

		
			//	RdfViewSystem system = new RdfViewSystem2();
			ConfiguratorCandidateSelector.configure(config, syntaxBridge, candidateViewSelector, null);
			
			databaseSchema = Schema.create(conn);
		} finally {
			conn.close();
		}

		SparqlSqlStringRewriter rewriter = SparqlifyUtils.createTestRewriter(candidateViewSelector, opMappingRewriter, typeSystem, databaseSchema);

		QueryExecutionFactory qef = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);
		
		if(maxQueryExecutionTime != null) {
			qef = QueryExecutionFactoryTimeout.decorate(qef, maxQueryExecutionTime * 1000);
		}
		
		if(maxResultSetSize != null) {
			qef = QueryExecutionFactoryLimit.decorate(qef, false, maxResultSetSize);
		}
		
		return qef;
	}
	
	
	public static SparqlSqlStringRewriter createSparqlSqlStringRewriter(SparqlSqlOpRewriter ssoRewriter, TypeSystem typeSystem)  {

		SqlExprSerializerSystem serializerSystem = createSerializerSystem(typeSystem);
		SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(serializerSystem);

		SparqlSqlStringRewriter result = new SparqlSqlStringRewriterImpl(ssoRewriter, sqlOpSerializer);

		return result;
	}
	
	public static SparqlSqlOpRewriter createSqlOpRewriter(CandidateViewSelector<ViewDefinition> candidateViewSelector, OpMappingRewriter opMappingRewriter, TypeSystem datatypeSystem, Schema databaseSchema) throws SQLException, IOException {
		//DatatypeSystem datatypeSystem = TestUtils.createDefaultDatatypeSystem();
		//ExprTransformer exprTransformer = new ExprTransformerMap();

		// Eliminates rdf terms from Expr (this is datatype independent)		
		
		//TypedExprTransformer sqlTranslatorTmp = new TypedExprTransformerImpl(datatypeSystem);
		
		//SqlExprSerializer exprSerializer = new SqlExprSerializerPostgres(); //null /* da */);
		
		
		DatatypeToStringPostgres typeSerializer = new DatatypeToStringPostgres(); 
		SqlLiteralMapper sqlLiteralMapper = new SqlLiteralMapperDefault(typeSerializer);
		//SqlExprSerializerSystem serializerSystem = new SqlExprSerializerSystemImpl(typeSerializer, sqlLiteralMapper);

		
		
		SqlOpSelectBlockCollector collector = new SqlOpSelectBlockCollectorImpl();
		
		SparqlSqlOpRewriter result = new SparqlSqlOpRewriterImpl(candidateViewSelector, opMappingRewriter, collector, databaseSchema);
		
		return result;
	}
	
	/**
	 * Creates a full blown SPARQL SQL rewriter object, comprised of: - a
	 * candidate view selector (TODO rename: actually this object actually
	 * returnes a transformed algebra expression; rather than just selecting
	 * candidates. - a op
	 * 
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	@Deprecated
	public static SparqlSqlStringRewriter createTestRewriter(
			CandidateViewSelector<ViewDefinition> candidateViewSelector,
			OpMappingRewriter opMappingRewriter, TypeSystem datatypeSystem,
			Schema databaseSchema) throws SQLException, IOException {

		SparqlSqlOpRewriter ssoRewriter = createSqlOpRewriter(
				candidateViewSelector, opMappingRewriter, datatypeSystem,
				databaseSchema);
		SparqlSqlStringRewriter result = createSparqlSqlStringRewriter(
				ssoRewriter, datatypeSystem);

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
	
	
	public static Node getNode(Binding binding, Var var, Node fallbackNode) {
		Node result = binding.get(var);
		if(result == null) {
			result = fallbackNode;
		}
		return result;
	}
	
	
	public static final Var vg = Var.alloc("g");
	public static final Var vs = Var.alloc("s");
	public static final Var vp = Var.alloc("p");
	public static final Var vo = Var.alloc("o");
	
	
	public static Set<Quad> createDumpNQuads(QueryExecutionFactory qef) {
		String queryStr = "Select ?g ?s ?p ?o { Graph ?g { ?s ?p ?o } }";
		QueryExecution qe = qef.createQueryExecution(queryStr);
		ResultSet rs = qe.execSelect();
		
		/*
		Var vg = Var.alloc("g");
		Var vs = Var.alloc("s");
		Var vp = Var.alloc("p");
		Var vo = Var.alloc("o");
		*/
		
		Set<Quad> result = new HashSet<Quad>();
		while(rs.hasNext()) {
			Binding binding = rs.nextBinding();
//			Node g = getNode(binding, vg, Quad.defaultGraphNodeGenerated);
//			Node s = getNode(binding, vs, Quad.defaultGraphNodeGenerated);
//			Node p = getNode(binding, vp, Quad.defaultGraphNodeGenerated);
//			Node o = getNode(binding, vo, Quad.defaultGraphNodeGenerated);

			Node g = binding.get(vg);
			Node s = binding.get(vs);
			Node p = binding.get(vp);
			Node o = binding.get(vo);
			
			Quad quad = new Quad(g, s, p, o);
			result.add(quad);
		}
		
		return result;
	}
	
	public static QueryExecutionFactoryExImpl createQueryExecutionFactoryEx(DataSource dataSource, SparqlSqlOpRewriter sparqlSqlOpRewriter, SqlOpSerializer sqlOpSerializer) {
		
		SparqlSqlStringRewriter sssRewriter = new SparqlSqlStringRewriterImpl(sparqlSqlOpRewriter, sqlOpSerializer);

		QueryExecutionFactory qefDefault = new QueryExecutionFactorySparqlifyDs(sssRewriter, dataSource);
		QueryExecutionFactory qefExplain = new QueryExecutionFactorySparqlifyExplain(dataSource, sparqlSqlOpRewriter, sqlOpSerializer);
		
		
		QueryExecutionFactoryExImpl result = new QueryExecutionFactoryExImpl(qefDefault, qefExplain);
		
		return result;
	}

}