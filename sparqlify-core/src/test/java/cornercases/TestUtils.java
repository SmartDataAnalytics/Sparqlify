package cornercases;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.DatatypeSystem;
import org.aksw.sparqlify.core.DatatypeSystemCustom;
import org.aksw.sparqlify.core.algorithms.DatatypeAssigner;
import org.aksw.sparqlify.core.algorithms.DatatypeAssignerMap;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SparqlSqlRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SqlExprSerializerPostgres;
import org.aksw.sparqlify.core.algorithms.SqlOpSelectBlockCollectorImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.interfaces.CandidateViewSelector;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlRewriter;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;
import org.aksw.sparqlify.core.interfaces.SqlOpSelectBlockCollector;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.util.MapReader;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestUtils {

	private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

	public static DatatypeSystem createDefaultDatatypeSystem() throws IOException {
		
		Map<String, String> typeNameToClass = MapReader.readFile(new File("src/main/resources/type-class.tsv"));
		Map<String, String> typeNameToUri = MapReader.readFile(new File("src/main/resources/type-uri.tsv"));
		Map<String, String> typeHierarchy = MapReader.readFile(new File("src/main/resources/type-hierarchy.default.tsv"));
		
		DatatypeSystem result = DatatypeSystemCustom.create(typeNameToClass, typeNameToUri, typeHierarchy, TestUtils.logger);
	
		return result;
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
		
		String testTable = "CREATE TABLE person (id INT, name VARCHAR)";
		
		conn.createStatement().executeUpdate(testTable);
		
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
		

		DatatypeAssigner da = DatatypeAssignerMap.createDefaultAssignments(datatypeSystem);

		MappingOps mappingOps = new MappingOpsImpl(da);
		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
		
		SqlExprSerializer exprSerializer = new SqlExprSerializerPostgres(da);		
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