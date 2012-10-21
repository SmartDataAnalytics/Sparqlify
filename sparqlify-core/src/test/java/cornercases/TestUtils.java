package cornercases;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.sparqlify.algebra.sparql.transform.MethodSignature;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.core.TypeTokenPostgis;
import org.aksw.sparqlify.core.algorithms.ExprDatatypeNorm;
import org.aksw.sparqlify.core.algorithms.ExprEvaluator;
import org.aksw.sparqlify.core.algorithms.FunctionRegistrySql;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SparqlSqlRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SqlExprSerializerPostgres;
import org.aksw.sparqlify.core.algorithms.SqlOpSelectBlockCollectorImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.algorithms.SqlTranslationUtils;
import org.aksw.sparqlify.core.algorithms.SqlTranslatorImpl;
import org.aksw.sparqlify.core.datatypes.DatatypeSystem;
import org.aksw.sparqlify.core.datatypes.DatatypeSystemCustom;
import org.aksw.sparqlify.core.datatypes.DefaultCoercions;
import org.aksw.sparqlify.core.datatypes.SqlExprEvaluator;
import org.aksw.sparqlify.core.datatypes.SqlExprEvaluator_Equals;
import org.aksw.sparqlify.core.datatypes.SqlExprEvaluator_LogicalAnd;
import org.aksw.sparqlify.core.datatypes.SqlExprEvaluator_LogicalNot;
import org.aksw.sparqlify.core.datatypes.SqlExprEvaluator_LogicalOr;
import org.aksw.sparqlify.core.datatypes.XMethod;
import org.aksw.sparqlify.core.datatypes.XMethodImpl;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlRewriter;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;
import org.aksw.sparqlify.core.interfaces.SqlOpSelectBlockCollector;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.util.MapReader;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestUtils {

	private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

	public static DatatypeSystemCustom createDefaultDatatypeSystem() throws IOException {
		
		Map<String, String> typeNameToClass = MapReader.readFile(new File("src/main/resources/type-class.tsv"));
		Map<String, String> typeNameToUri = MapReader.readFile(new File("src/main/resources/type-uri.tsv"));
		Map<String, String> typeHierarchy = MapReader.readFile(new File("src/main/resources/type-hierarchy.default.tsv"));
		
		DatatypeSystemCustom result = DatatypeSystemCustom.create(typeNameToClass, typeNameToUri, typeHierarchy, TestUtils.logger);
	
		initDatatypeSystem(result);
		
		return result;
	}
	
	/**
	 * Declares a set of default operators and functions.
	 * 
	 * @param ds
	 */
	public static void initDatatypeSystem(DatatypeSystem ds) {
		try {
			_initDatatypeSystem(ds);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void _initDatatypeSystem(DatatypeSystem ds) throws SecurityException, NoSuchMethodException
	{
		{
			Method m = DefaultCoercions.class.getMethod("toDouble", Integer.class);
			XMethod x = XMethodImpl.createFromMethod("toDouble", ds, null, m);
			ds.registerCoercion(x);
		}
		
		/*
		 * Methods that can only be rewritten
		 */
		
		{
			MethodSignature<TypeToken> signature = MethodSignature.create(TypeToken.Boolean, Arrays.asList(TypeTokenPostgis.Geometry, TypeTokenPostgis.Geometry));
			
			XMethod x = XMethodImpl.create(ds, "ST_INTERSECTS", signature);
			ds.registerSqlFunction("http://ex.org/fn/intersects", x);
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
			SqlExprEvaluator evaluator = new SqlExprEvaluator_Equals(ds);
			ds.createSparqlFunction("=", evaluator);
		}
		
//		{
//			//MethodSignature<TypeToken> signature = MethodSignature.create(TypeToken.Boolean, Arrays.asList(TypeToken.String, TypeToken.String));
//			
//			//XMethod x = XMethodImpl.create(ds, "equalsIgnoreCase", signature);
//			Method m = Ops.class.getMethod("equalsIgnoreCase", String.class, String.class);
//			XMethod x = XMethodImpl.createFromMethod("EQUALS_IGNORE_CASE", ds, null, m);
//			ds.registerSqlFunction("http://ex.org/fn/equalsIgnoreCase", x);
//		}

		
		/*
		{
			Method m = Ops.class.getMethod("myTestFunc", String.class, Double.class);
			XMethod x = XMethodImpl.createFromMethod("myTestFunc", ds, null, m);
			ds.register(x);
		}*/
		
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

//		{
//			/**
//			 * Actually it is like that:
//			 * We have an abstract sparql function, which can be overloaded by several SQL functions.
//			 * E.g. the SPARQL function ogc:intersects can be implemented by ST_Intersects for geometries or for geographies.
//			 * 
//			 */
//			
//			Config config = parser.parse("PREFIX fn:<http://ex.org/fn/> DECLARE FUNCTION boolean ex:intersects(integer ?a, integer ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
//			FunctionDeclarationTemplate fnDecl = config.getFunctionDeclarations().get(0);
//			sqlRegistry.add(fnDecl);
//		}
		{
			/*
			Config config = parser.parse("PREFIX ex:<http://ex.org/> DECLARE FUNCTION boolean ex:intersects(geometry ?a, geometry ?b) AS ST_INTERSECTS(?a, ?b, 1000 * ?a)", logger);
			FunctionDeclaration decl = config.getFunctionDeclarations().get(0);
			sqlRegistry.add(decl);
			*/
		}		
	}
	

	//public Connection 
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
		
		
		conn.createStatement().executeUpdate("CREATE TABLE person (id INT, name VARCHAR)");
		conn.createStatement().executeUpdate("CREATE TABLE dept (id INT, name VARCHAR)");
		conn.createStatement().executeUpdate("CREATE TABLE person_to_dept (person_id INT, dept_id INT)");

		conn.createStatement().executeUpdate("INSERT INTO person VALUES (1, 'Anne')");
		conn.createStatement().executeUpdate("INSERT INTO person VALUES (2, 'Bob')");

		conn.createStatement().executeUpdate("INSERT INTO dept VALUES (5, 'Research')");
		conn.createStatement().executeUpdate("INSERT INTO dept VALUES (6, 'Marketing')");
		
		conn.createStatement().executeUpdate("INSERT INTO person_to_dept VALUES (1, 5)");
		conn.createStatement().executeUpdate("INSERT INTO person_to_dept VALUES (2, 6)");

		return ds;
	}


	public static ViewDefinitionFactory createViewDefinitionFactory(Connection conn, Map<String, String> typeAlias) throws IOException {
		DatatypeSystem datatypeSystem = TestUtils.createDefaultDatatypeSystem();
		
		ViewDefinitionFactory result = createViewDefinitionFactory(conn, datatypeSystem, typeAlias);
		
		return result;
	}
	
	
	public static ViewDefinitionFactory createViewDefinitionFactory(Connection conn, DatatypeSystem datatypeSystem, Map<String, String> typeAlias) throws IOException {
	
		ConfigParser parser = new ConfigParser();

		SchemaProvider schemaProvider = new SchemaProviderImpl(conn, datatypeSystem, typeAlias);
		SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

		ViewDefinitionFactory result = new ViewDefinitionFactory(parser, syntaxBridge);
		
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
	public static SparqlSqlRewriter createTestRewriter(CandidateViewSelector candidateViewSelector, DatatypeSystem datatypeSystem) throws SQLException, IOException {		
		
		//DatatypeSystem datatypeSystem = TestUtils.createDefaultDatatypeSystem();
		//ExprTransformer exprTransformer = new ExprTransformerMap();
		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
		SqlTranslator sqlTranslator = new SqlTranslatorImpl(datatypeSystem);
		ExprDatatypeNorm exprNormalizer = new ExprDatatypeNorm(datatypeSystem);
				
		
		MappingOps mappingOps = new MappingOpsImpl(exprTransformer, sqlTranslator, exprNormalizer);
		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
		
		SqlExprSerializer exprSerializer = new SqlExprSerializerPostgres(); //null /* da */);		
		SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(exprSerializer);
		
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