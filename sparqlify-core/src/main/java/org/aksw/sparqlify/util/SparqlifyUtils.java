package org.aksw.sparqlify.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.StreamUtils;
import org.aksw.commons.util.jdbc.Schema;
import org.aksw.commons.util.jdbc.SqlUtils;
import org.aksw.jena_sparql_api.limit.QueryExecutionFactoryLimit;
import org.aksw.jena_sparql_api.timeout.QueryExecutionFactoryTimeout;
import org.aksw.jena_sparql_api.views.CandidateViewSelector;
import org.aksw.jena_sparql_api.views.ExprEvaluator;
import org.aksw.jena_sparql_api.views.SqlTranslationUtils;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.BasicTableInfoProvider;
import org.aksw.sparqlify.config.v0_2.bridge.BasicTableProviderJdbc;
import org.aksw.sparqlify.config.v0_2.bridge.ConfiguratorCandidateSelector;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderDummy;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.config.xml.SparqlifyConfig;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorSparqlify;
import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.core.algorithms.ExprDatatypeNorm;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SparqlSqlStringRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.algorithms.ViewDefinitionNormalizerImpl;
import org.aksw.sparqlify.core.cast.ExprBindingSubstitutor;
import org.aksw.sparqlify.core.cast.ExprBindingSubstitutorImpl;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.cast.TypedExprTransformer;
import org.aksw.sparqlify.core.cast.TypedExprTransformerImpl;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.interfaces.OpMappingRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriterImpl;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.core.rewrite.expr.transform.RdfTermEliminator;
import org.aksw.sparqlify.core.rewrite.expr.transform.RdfTermEliminatorWriteable;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryEx;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryExImpl;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyExplain;
import org.aksw.sparqlify.core.sql.algebra.serialization.SqlOpSerializer;
import org.aksw.sparqlify.core.sql.algebra.transform.SqlOpSelectBlockCollector;
import org.aksw.sparqlify.core.sql.algebra.transform.SqlOpSelectBlockCollectorImpl;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaper;
import org.aksw.sparqlify.core.sql.common.serialization.SqlEscaperDoubleQuote;
import org.antlr.runtime.RecognitionException;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.h2.jdbcx.JdbcDataSource;
import org.slf4j.Logger;



public class SparqlifyUtils {


    public static String toUtf8String(ByteArrayOutputStream baos) {
        String result;

        try {
            result = baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

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
        conn.createStatement().executeUpdate("DROP TABLE IF EXISTS \"person_to_dept\";");
        conn.createStatement().executeUpdate("DROP TABLE IF EXISTS \"dept\";");
        conn.createStatement().executeUpdate("DROP TABLE IF EXISTS \"person\";");


        conn.createStatement().executeUpdate("CREATE TABLE \"person\" (\"id\" INT PRIMARY KEY, \"name\" VARCHAR, \"age\" INT)");
        conn.createStatement().executeUpdate("CREATE TABLE \"dept\" (\"id\" INT PRIMARY KEY , \"name\" VARCHAR)");
        conn.createStatement().executeUpdate("CREATE TABLE \"person_to_dept\" (\"person_id\" INT, \"dept_id\" INT, UNIQUE(\"person_id\", \"dept_id\"))");

        conn.createStatement().executeUpdate("INSERT INTO \"person\" VALUES (1, 'Anne', 20)");
        conn.createStatement().executeUpdate("INSERT INTO \"person\" VALUES (2, 'Bob', 22)");

        conn.createStatement().executeUpdate("INSERT INTO \"dept\" VALUES (5, 'Research')");
        conn.createStatement().executeUpdate("INSERT INTO \"dept\" VALUES (6, 'Marketing')");

        conn.createStatement().executeUpdate("INSERT INTO \"person_to_dept\" VALUES (1, 5)");
        conn.createStatement().executeUpdate("INSERT INTO \"person_to_dept\" VALUES (2, 6)");
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





    @Deprecated
    public static ViewDefinitionFactory createViewDefinitionFactory(Connection conn, Map<String, String> typeAlias) throws IOException {
        // SqlEscaper sqlEscaper = new SqlEscaperDoubleQuote();
        SqlCodec sqlEscaper = SqlCodecUtils.createSqlCodecDefault();
        TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();

        ViewDefinitionFactory result = createViewDefinitionFactory(conn, typeSystem, typeAlias, sqlEscaper);

        return result;
    }


    /**
     * Util method to quickly set up a default ViewDefinitionFactory - used in test cases
     *
     * @param conn
     * @param typeAlias
     * @param sqlEscaper
     * @return
     * @throws IOException
     */
    public static ViewDefinitionFactory createViewDefinitionFactory(Connection conn, Map<String, String> typeAlias, SqlCodec sqlEscaper) throws IOException {
        TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();


        ViewDefinitionFactory result = createViewDefinitionFactory(conn, typeSystem, typeAlias, sqlEscaper);

        return result;
    }


    public static ViewDefinitionFactory createViewDefinitionFactory(Connection conn, TypeSystem datatypeSystem, Map<String, String> typeAlias, SqlCodec sqlEscaper) throws IOException {

        ConfigParser parser = new ConfigParser();

        BasicTableInfoProvider basicTableInfoProvider = new BasicTableProviderJdbc(conn);
        SchemaProvider schemaProvider = new SchemaProviderImpl(basicTableInfoProvider, datatypeSystem, typeAlias, sqlEscaper);
        SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

        ViewDefinitionFactory result = new ViewDefinitionFactory(parser, syntaxBridge);

        return result;
    }

    /*
     * Dummy view definition factory used e.g. for R2RML export
     */
    public static ViewDefinitionFactory createDummyViewDefinitionFactory(Map<String, String> typeAlias) {

        ConfigParser parser = new ConfigParser();

        SqlEscaper sqlEscaper = new SqlEscaperDoubleQuote();
        TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
        SchemaProvider schemaProvider = new SchemaProviderDummy(typeSystem, typeAlias);
        SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

        ViewDefinitionFactory result = new ViewDefinitionFactory(parser, syntaxBridge);

        return result;
    }


//	@Deprecated
//	public static SqlExprSerializerSystem createSerializerSystem(TypeSystem typeSystem) {
//		SqlExprSerializerSystem result = SparqlifyCoreInit.createSerializerSystem(typeSystem, new SqlEscaperDoubleQuote());
//
//		return result;
//	}


    public static SqlTranslator createSqlRewriter() {
        TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
        RdfTermEliminator rdfTermEliminator = SparqlifyCoreInit.createDefaultTransformer(typeSystem);
        ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();


        SqlTranslator result = createSqlRewriter(typeSystem, rdfTermEliminator, exprTransformer);
        return result;
    }


    public static SqlTranslator createSqlTranslator(ExprRewriteSystem rewriteSystem) {

        TypeSystem typeSystem = rewriteSystem.getTypeSystem();
        RdfTermEliminator rdfTermEliminator = rewriteSystem.getTermEliminator();
        ExprEvaluator exprTransformer = rewriteSystem.getExprEvaluator();

        ExprBindingSubstitutor exprBindingSubstitutor = new ExprBindingSubstitutorImpl();



        // Computes types for Expr, thereby yielding SqlExpr
        TypedExprTransformer typedExprTransformer = new TypedExprTransformerImpl(typeSystem);


        //SqlTranslator sqlTranslator = new SqlTranslatorImpl(datatypeSystem);
        SqlTranslator result = new SqlTranslatorImpl2(exprBindingSubstitutor, rdfTermEliminator, exprTransformer, typedExprTransformer);

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
        //java.sql.ResultSet rs = conn.createStatement().executeQuery("SELECT table_name FROM information_schema.tables");
        String query = "SELECT table_name FROM information_schema.tables";
        List<String> result = SqlUtils.executeList(conn, query, String.class);
        return result;
    }


    public static MappingOps createDefaultMappingOps(ExprRewriteSystem ers) {
        //RdfTermEliminator rdfTermEliminator = SparqlifyCoreInit.createDefaultTransformer(typeSystem);

        SqlTranslator sqlTranslator = createSqlRewriter(ers.getTypeSystem(), ers.getTermEliminator(), ers.getExprEvaluator());


        ExprDatatypeNorm exprNormalizer = new ExprDatatypeNorm();


        MappingOps mappingOps = new MappingOpsImpl(sqlTranslator, exprNormalizer);

        return mappingOps;
    }

    public static OpMappingRewriter createDefaultOpMappingRewriter(ExprRewriteSystem ers) {
        MappingOps mappingOps = createDefaultMappingOps(ers);

        OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);

        return opMappingRewriter;
    }

    //public static QueryExecutionFactory

    public static QueryExecutionFactoryEx createDefaultSparqlifyEngine(
            DataSource dataSource,
            Config config,
            Long maxResultSetSize,
            Integer maxQueryExecutionTimeInSeconds) throws SQLException, IOException {
        DatatypeToString typeSerializer = new DatatypeToStringPostgres();
        // SqlEscaper sqlEscaper = new SqlEscaperDoubleQuote();
        SqlCodec sqlEscaper = SqlCodecUtils.createSqlCodecDefault();
        SparqlifyConfig sqlFunctionMapping = SparqlifyCoreInit.loadSqlFunctionDefinitions("functions.xml");

        //final QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(ds, config, typeSerializer, sqlEscaper, null, null);
        QueryExecutionFactoryEx result = createDefaultSparqlifyEngine(dataSource, config, typeSerializer, sqlEscaper, maxResultSetSize, maxQueryExecutionTimeInSeconds, sqlFunctionMapping);
        return result;
    }

    /**
     * This is the method that does all the wireing. Do not use directly; use the
     * Fluent SparqlifyFactory instead.
     *
     * @param dataSource
     * @param config
     * @param typeSerializer
     * @param sqlEscaper
     * @param maxResultSetSize
     * @param maxQueryExecutionTimeInSeconds
     * @param sqlFunctionMapping
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static QueryExecutionFactoryEx createDefaultSparqlifyEngine(
            DataSource dataSource,
            Config config,
            DatatypeToString typeSerializer,
            SqlCodec sqlEscaper,
            Long maxResultSetSize,
            Integer maxQueryExecutionTimeInSeconds,
            SparqlifyConfig sqlFunctionMapping) throws SQLException, IOException {
        SparqlSqlStringRewriterImpl rewriter = createDefaultSparqlSqlStringRewriter(dataSource, config, typeSerializer, sqlEscaper, sqlFunctionMapping);

        SparqlSqlOpRewriter ssoRewriter = rewriter.getSparqlSqlOpRewriter();
        SqlOpSerializer sqlOpSerializer = rewriter.getSqlOpSerializer();

        QueryExecutionFactory qefDefault = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);

        if(maxQueryExecutionTimeInSeconds != null) {
            qefDefault = QueryExecutionFactoryTimeout.decorate(qefDefault, maxQueryExecutionTimeInSeconds * 1000);
        }

        if(maxResultSetSize != null) {
            qefDefault = QueryExecutionFactoryLimit.decorate(qefDefault, false, maxResultSetSize);
        }


        QueryExecutionFactory qefExplain = new QueryExecutionFactorySparqlifyExplain(dataSource, ssoRewriter, sqlOpSerializer);


        QueryExecutionFactoryEx result = new QueryExecutionFactoryExImpl(qefDefault, qefExplain);

        return result;

    }


    /**
     * This method creates all required intermediary objects
     *
     *
     * @param dataSource
     * @param config
     * @param maxResultSetSize
     * @param maxQueryExecutionTime
     * @return
     * @throws SQLException
     * @throws IOException
     */
    public static SparqlSqlStringRewriterImpl createDefaultSparqlSqlStringRewriter(
            DataSource dataSource,
            Config config,
            DatatypeToString typeSerializer,
            SqlCodec sqlEscaper,
            SparqlifyConfig sqlFunctionMapping) throws SQLException, IOException {
        SparqlSqlStringRewriterImpl result;
        try(Connection conn = dataSource.getConnection()) {
            BasicTableInfoProvider basicTableInfoProvider = new BasicTableProviderJdbc(conn);
            // Schema databaseSchema = Schema.create(conn);
            Schema databaseSchema = null;

            result = createDefaultSparqlSqlStringRewriter(basicTableInfoProvider, databaseSchema, config, typeSerializer, sqlEscaper, sqlFunctionMapping);
        }

        return result;
    }

    public static SparqlSqlStringRewriterImpl createDefaultSparqlSqlStringRewriter(
            BasicTableInfoProvider basicTableInfoProvider,
            Schema databaseSchema,
            Config config,
            DatatypeToString typeSerializer,
            SqlCodec sqlEscaper,
            SparqlifyConfig sqlFunctionMapping) throws SQLException, IOException {
        SparqlifyCoreInit.initSparqlifyFunctions();

        //DatatypeToString typeSerializer = new DatatypeToStringPostgres();
        ExprRewriteSystem ers = createExprRewriteSystem(typeSerializer, sqlEscaper, sqlFunctionMapping);


        TypeSystem typeSystem = ers.getTypeSystem();


        //TypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();

        // typeAliases for the H2 datatype
        Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");


        //Connection conn = dataSource.getConnection();

        MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(ers);
        OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
        //CandidateViewSelectorImpl cvs = new CandidateViewSelectorImpl(mappingOps);

        CandidateViewSelector<ViewDefinition> candidateViewSelector;

        SchemaProvider schemaProvider = new SchemaProviderImpl(basicTableInfoProvider, typeSystem, typeAlias, sqlEscaper);
        SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

        candidateViewSelector = new CandidateViewSelectorSparqlify(mappingOps, new ViewDefinitionNormalizerImpl());


        //	RdfViewSystem system = new RdfViewSystem2();
        ConfiguratorCandidateSelector.configure(config, syntaxBridge, candidateViewSelector, null);


        SparqlSqlOpRewriter ssoRewriter = SparqlifyUtils.createSqlOpRewriter(candidateViewSelector, opMappingRewriter, typeSystem, databaseSchema);

        //SqlExprSerializerSystem serializerSystem = SparqlifyUtils.createSerializerSystem(typeSystem);
        SqlExprSerializerSystem serializerSystem = ers.getSerializerSystem();

        SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(sqlEscaper, serializerSystem);


        SparqlSqlStringRewriterImpl rewriter = new SparqlSqlStringRewriterImpl(ssoRewriter, sqlOpSerializer);//SparqlifyUtils.createSparqlSqlStringRewriter(ssoRewriter);

        return rewriter;

        //SparqlSqlStringRewriter rewriter = SparqlifyUtils.createTestRewriter(candidateViewSelector, opMappingRewriter, typeSystem);

        //SparqlSqlRewriter rewriter = new SparqlSqlRewriterImpl(candidateViewSelector, opMappingRewriter, sqlOpSelectBlockCollector, sqlOpSerializer);


    }

//	public static QueryExecutionFactory createDefaultSparqlifyEngineOld(DataSource dataSource, Config config, Long maxResultSetSize, Long maxQueryExecutionTime) throws SQLException, IOException {
//		RdfViewSystemOld.initSparqlifyFunctions();
//
//
//		//Connection conn = dataSource.getConnection();
//
//		TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
//		//TypeSystem datatypeSystem = SparqlifyUtils.createDefaultDatatypeSystem();
//
//		// typeAliases for the H2 datatype
//		Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");
//
//
//		Connection conn = dataSource.getConnection();
//
//
///*
//		ExprEvaluator exprTransformer = SqlTranslationUtils.createDefaultEvaluator();
//		SqlTranslator sqlTranslator = createSqlRewriter(typeSystem, exprTransformer);
//
//
//		ExprDatatypeNorm exprNormalizer = new ExprDatatypeNorm();
//
//
//		MappingOps mappingOps = new MappingOpsImpl(sqlTranslator, exprNormalizer);
//		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
//*/
//		//OpMappingRewriter opMappingRewriter = createDefaultOpMappingRewriter(typeSystem);
//		MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(typeSystem);
//		OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);
//		//CandidateViewSelectorImpl cvs = new CandidateViewSelectorImpl(mappingOps);
//
//		CandidateViewSelector<ViewDefinition> candidateViewSelector;
//		Schema databaseSchema;
//		try {
//			SchemaProvider schemaProvider = new SchemaProviderImpl(conn, typeSystem, typeAlias);
//			SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);
//
//			candidateViewSelector = new CandidateViewSelectorImpl(mappingOps, new ViewDefinitionNormalizerImpl());
//
//
//			//	RdfViewSystem system = new RdfViewSystem2();
//			ConfiguratorCandidateSelector.configure(config, syntaxBridge, candidateViewSelector, null);
//
//			databaseSchema = Schema.create(conn);
//		} finally {
//			conn.close();
//		}
//
//		SparqlSqlStringRewriter rewriter = SparqlifyUtils.createTestRewriter(candidateViewSelector, opMappingRewriter, typeSystem, databaseSchema);
//
//		QueryExecutionFactory qef = new QueryExecutionFactorySparqlifyDs(rewriter, dataSource);
//
//		if(maxQueryExecutionTime != null) {
//			qef = QueryExecutionFactoryTimeout.decorate(qef, maxQueryExecutionTime * 1000);
//		}
//
//		if(maxResultSetSize != null) {
//			qef = QueryExecutionFactoryLimit.decorate(qef, false, maxResultSetSize);
//		}
//
//		return qef;
//	}



//	@Deprecated
//	public static SparqlSqlStringRewriter createSparqlSqlStringRewriter(SparqlSqlOpRewriter ssoRewriter, TypeSystem typeSystem)  {
//
//
//		SqlExprSerializerSystem serializerSystem = createSerializerSystem(typeSystem);
//		SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(new SqlEscaperDoubleQuote(), serializerSystem);
//
//		SparqlSqlStringRewriter result = new SparqlSqlStringRewriterImpl(ssoRewriter, sqlOpSerializer);
//
//		return result;
//	}

    public static SparqlSqlOpRewriter createSqlOpRewriter(CandidateViewSelector<ViewDefinition> candidateViewSelector, OpMappingRewriter opMappingRewriter, TypeSystem datatypeSystem, Schema databaseSchema) throws SQLException, IOException {
        //DatatypeSystem datatypeSystem = TestUtils.createDefaultDatatypeSystem();
        //ExprTransformer exprTransformer = new ExprTransformerMap();

        // Eliminates rdf terms from Expr (this is datatype independent)

        //TypedExprTransformer sqlTranslatorTmp = new TypedExprTransformerImpl(datatypeSystem);

        //SqlExprSerializer exprSerializer = new SqlExprSerializerPostgres(); //null /* da */);


        DatatypeToStringPostgres typeSerializer = new DatatypeToStringPostgres();
        //SqlLiteralMapper sqlLiteralMapper = new SqlLiteralMapperDefault(typeSerializer);
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
//	@Deprecated
//	public static SparqlSqlStringRewriter createTestRewriter(
//			CandidateViewSelector<ViewDefinition> candidateViewSelector,
//			OpMappingRewriter opMappingRewriter, TypeSystem datatypeSystem,
//			Schema databaseSchema) throws SQLException, IOException {
//
//		SparqlSqlOpRewriter ssoRewriter = createSqlOpRewriter(
//				candidateViewSelector, opMappingRewriter, datatypeSystem,
//				databaseSchema);
//		SparqlSqlStringRewriter result = createSparqlSqlStringRewriter(
//				ssoRewriter, datatypeSystem);
//
//		return result;
//
//	}

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

    public static Config parseSmlConfig(String str, Logger logger) throws IOException, RecognitionException {
        ByteArrayInputStream in = new ByteArrayInputStream(str.getBytes());
        Config result = parseSmlConfig(in, logger);

        return result;
    }

    public static Config parseSmlConfig(InputStream in, Logger logger) throws IOException, RecognitionException {
        ConfigParser parser = new ConfigParser();

        Config config = null;
        try {
            config = parser.parse(in, logger);
        } finally {
            in.close();
        }

        return config;
    }

    public static Node getNode(Binding binding, Var var, Node fallbackNode) {
        Node result = binding.get(var);
        if(result == null) {
            result = fallbackNode;
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




    public static ExprRewriteSystem createDefaultExprRewriteSystem() {
        // SqlEscaper sqlEscaper = new SqlEscaperDoubleQuote();
        SqlCodec sqlEscaper = SqlCodecUtils.createSqlCodecDefault();
        DatatypeToString typeSerializer = new DatatypeToStringPostgres();
        SparqlifyConfig sqlFunctionDefinitions = SparqlifyCoreInit.loadSqlFunctionDefinitions("functions.xml");
        ExprRewriteSystem result = createExprRewriteSystem(typeSerializer, sqlEscaper, sqlFunctionDefinitions);
        return result;
    }

    public static ExprRewriteSystem createExprRewriteSystem(DatatypeToString typeSerializer, SqlCodec sqlEscaper, SparqlifyConfig sqlFunctionMapping) {

        SparqlifyCoreInit.initSparqlifyFunctions();

        TypeSystem typeSystem = SparqlifyCoreInit.createDefaultDatatypeSystem();
        RdfTermEliminatorWriteable exprTransformer = SparqlifyCoreInit.createDefaultTransformer(typeSystem);
        SqlExprSerializerSystem serializerSystem = SparqlifyCoreInit.createSerializerSystem(typeSystem, typeSerializer, sqlEscaper);
        ExprEvaluator exprEvaluator = SqlTranslationUtils.createDefaultEvaluator();

        ExprRewriteSystem result = new ExprRewriteSystem(typeSystem, exprTransformer, exprEvaluator, serializerSystem);


        SparqlifyCoreInit.loadExtensionFunctions(typeSystem, exprTransformer, serializerSystem, sqlFunctionMapping);

        return result;
    }

    public static SparqlSqlOpRewriterImpl unwrapOpRewriter(QueryExecutionFactory qef) {
        QueryExecutionFactoryExImpl tmp = qef.unwrap(QueryExecutionFactoryExImpl.class);
        QueryExecutionFactorySparqlifyDs q = tmp.getDefaultQef().unwrap(QueryExecutionFactorySparqlifyDs.class);

        //QueryExecutionFactorySparqlifyExplain q = qef.unwrap(QueryExecutionFactorySparqlifyExplain.class);

        SparqlSqlStringRewriterImpl sssRewriter = (SparqlSqlStringRewriterImpl)q.getRewriter();
        SparqlSqlOpRewriterImpl result = (SparqlSqlOpRewriterImpl)sssRewriter.getSparqlSqlOpRewriter();
        //SparqlSqlOpRewriterImpl result = (SparqlSqlOpRewriterImpl)q.getRewriter();

        //QueryExecutionFactorySparqlifyDs q = qef.unwrap(QueryExecutionFactorySparqlifyDs.class);
//		SparqlSqlStringRewriterImpl strRewriter = (SparqlSqlStringRewriterImpl)q.getRewriter();
//		SparqlSqlOpRewriterImpl result = (SparqlSqlOpRewriterImpl)strRewriter.getSparqlSqlOpRewriter();

        return result;
    }

    public static SqlTranslator unwrapSqlTransformer(SparqlSqlOpRewriterImpl opRewriter) {

        OpMappingRewriterImpl opMappingRewriter = (OpMappingRewriterImpl)opRewriter.getOpMappingRewriter();
        MappingOpsImpl mappingOps = (MappingOpsImpl)opMappingRewriter.getMappingOps();
        SqlTranslator result = mappingOps.getSqlTranslator();

        return result;
    }

    public static CandidateViewSelectorSparqlify unwrapCandidateViewSelector(SparqlSqlOpRewriterImpl opRewriter) {
        CandidateViewSelectorSparqlify result = (CandidateViewSelectorSparqlify) opRewriter.getCandidateViewSelector();
        return result;
    }

}
