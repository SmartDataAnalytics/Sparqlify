package cornercases;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.aksw.sparqlify.algebra.sparql.expr.old.ExprSqlBridge;
import org.aksw.sparqlify.algebra.sql.exprs.SqlExprColumn;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.DatatypeSystem;
import org.aksw.sparqlify.core.DatatypeSystemCustom;
import org.aksw.sparqlify.core.algorithms.DatatypeAssigner;
import org.aksw.sparqlify.core.algorithms.DatatypeAssignerMap;
import org.aksw.sparqlify.core.algorithms.ExprDatatypeNorm;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.SqlExprSerializerPostgres;
import org.aksw.sparqlify.core.algorithms.SqlOpSelectBlockCollector;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.domain.Mapping;
import org.aksw.sparqlify.core.domain.VarBinding;
import org.aksw.sparqlify.core.domain.ViewDefinition;
import org.aksw.sparqlify.core.domain.ViewInstance;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.SqlExprSerializer;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.util.MapReader;
import org.antlr.runtime.RecognitionException;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingOpsImplTest {

	private static final Logger logger = LoggerFactory.getLogger(MappingOpsImplTest.class);

	public DatatypeSystem createDefaultDatatypeSystem() throws IOException {
		
		Map<String, String> typeNameToClass = MapReader.readFile(new File("src/main/resources/type-class.tsv"));
		Map<String, String> typeNameToUri = MapReader.readFile(new File("src/main/resources/type-uri.tsv"));
		Map<String, String> typeHierarchy = MapReader.readFile(new File("src/main/resources/type-hierarchy.default.tsv"));
		
		DatatypeSystem result = DatatypeSystemCustom.create(typeNameToClass, typeNameToUri, typeHierarchy, logger);

		return result;
	}
	
	@Test
	public void creationTest() throws RecognitionException, SQLException, IOException {

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
		
		
		/*
		 * Parsing
		 * 
		 */
		String testView = "Create View testview As Construct { ?s a ?t } With ?s = uri(?ID) ?t = uri(?NAME) From person";
		
		ConfigParser parser = new ConfigParser();
		Config config = parser.parse(testView, null);
		
		List<org.aksw.sparqlify.config.syntax.ViewDefinition> vds = config.getViewDefinitions();

		org.aksw.sparqlify.config.syntax.ViewDefinition vd = vds.get(0);

		/*
		 * Bridge from syntax to core domain objects
		 * 
		 */
		Map<String, String> typeAlias = MapReader.readFile(new File("src/main/resources/type-map.h2.tsv"));
		DatatypeSystem datatypeSystem = createDefaultDatatypeSystem();

		//System.out.println(typeAlign);
		
		SchemaProvider schemaProvider = new SchemaProviderImpl(conn, datatypeSystem, typeAlias);
		SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);
		
		ViewDefinition coreVd = syntaxBridge.create(vd);
		Mapping m1 = coreVd.getMapping();
		
		
		VarBinding binding = new VarBinding();
		ViewInstance vi = new ViewInstance(coreVd, binding);

		DatatypeAssigner da = DatatypeAssignerMap.createDefaultAssignments(datatypeSystem);
		//ExprDatatypeNorm exprNormalizer = new ExprDatatypeNorm(da);
		
		MappingOps ops = new MappingOpsImpl(da);
		
		Mapping m2 = ops.join(m1, m1);
		Mapping m3 = ops.join(m2, m1);
		Mapping m4 = ops.union(Arrays.asList(m1, m3));
		
		Mapping mTest = m4;
		//System.out.println(m2);
		
		System.out.println(mTest.getSqlOp());
		
		SqlExprColumn x;
		
//		 Context ctx = new InitialContext();
//		 ctx.bind("jdbc/dsName", ds);		

		ExprSqlBridge b;

		System.out.println(coreVd.getMapping().getSqlOp().getSchema());
		
		SqlOp block = SqlOpSelectBlockCollector._makeSelect(mTest.getSqlOp());
		System.out.println(block);
		
		
		SqlExprSerializer exprSerializer = new SqlExprSerializerPostgres(da);
		
		SqlOpSerializer serializer = new SqlOpSerializerImpl(exprSerializer);
		
		String sqlQueryString = serializer.serialize(block);
		
		System.out.println(sqlQueryString);
		
		//SqlSelectBlock x;
		
		//ViewDefinition vd = new ViewDefinition(name, );
		
	}
}
