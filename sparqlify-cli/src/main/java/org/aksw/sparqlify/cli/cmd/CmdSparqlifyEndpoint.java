package org.aksw.sparqlify.cli.cmd;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.sql.DataSource;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.jena_sparql_api.core.GraphQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.core.utils.QueryExecutionUtils;
import org.aksw.jena_sparql_api.limit.QueryExecutionFactoryLimit;
import org.aksw.jena_sparql_api.model.QueryExecutionFactoryModel;
import org.aksw.jena_sparql_api.server.utils.FactoryBeanSparqlServer;
import org.aksw.jena_sparql_api.utils.SparqlFormatterUtils;
import org.aksw.jena_sparql_api.views.CandidateViewSelector;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.BasicTableInfoProvider;
import org.aksw.sparqlify.config.v0_2.bridge.BasicTableProviderJdbc;
import org.aksw.sparqlify.config.v0_2.bridge.ConfiguratorCandidateSelector;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProvider;
import org.aksw.sparqlify.config.v0_2.bridge.SchemaProviderImpl;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorSparqlify;
import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.core.algorithms.ViewDefinitionNormalizerImpl;
import org.aksw.sparqlify.core.builder.FluentSparqlifyFactory;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.MappingOps;
import org.aksw.sparqlify.core.sparql.QueryEx;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryEx;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryExWrapper;
import org.aksw.sparqlify.core.sparql.QueryFactoryEx;
import org.aksw.sparqlify.util.ExprRewriteSystem;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.util.SqlBackendConfig;
import org.aksw.sparqlify.util.SqlBackendRegistry;
import org.aksw.sparqlify.web.SparqlifyCliHelper;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.writer.NQuadsWriter;
import org.apache.jena.riot.writer.NTriplesWriter;
import org.apache.jena.sparql.core.Quad;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name="endpoint", versionProvider = VersionProviderSparqlify.class, description = "Sparqlify Endpoint Subcommands")
public class CmdSparqlifyEndpoint
    implements Callable<Integer>
{
    private static final Logger logger = LoggerFactory.getLogger(CmdSparqlifyEndpoint.class);

    @Mixin
    public DataSourceOptions dataSourceOptions = new DataSourceOptions();

    @Option(names = { "P", "port" }, description = "Server port", defaultValue = "7531")
    public int port;

    @Option(names = { "-C", "--context" }, description = "Context e.g. /sparqlify" )
    public String context;

    @Option(names = { "-m", "--mapping" }, arity = "0..*", description = "Sparqlify mapping file (can be specified multiple times)")
    public List<String> mappingSources = new ArrayList<>();

    @Option(names = { "-Q", "--query" }, description = "Execute a single query")
    public String queryString;

    @Option(names = { "-D", "dump" }, description = "Create a dump (analoguos to -Q 'CONSTRUCT WHERE { GRAPH ?g { ?s ?p ?o } }')")
    public boolean isDump;

    @Option(names = { "-t" , "--timeout" }, description = "Maximum query execution timeout in seconds")
    public Integer maxQueryExecutionTime = null;

    @Option(names = { "-n", "--resultsetsize" }, description = "Maximum result set size")
    public Long maxResultSetSize = null;

    @Option(names = { "o", "format" }, description = "Output format; currently only applies to dump (-D). Values: ntriples, nquads")
    public String outputFormat;

    @Option(names = { "-1", "--sparql11" }, description = "Use jena for sparql 11 (supports property paths but may be slow)")
    public boolean useSparql11Wrapper;


    @Override
    public Integer call() throws Exception {

        LoggerCount loggerCount = new LoggerCount(logger);

        List<Resource> sources = SparqlifyCliHelper.resolveFiles(mappingSources, true, loggerCount);

        Config config = SparqlifyCliHelper.parseSmlConfigs(sources, loggerCount);
        if(loggerCount.getErrorCount() != 0) {
            throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
        }

        /*
         * Connection Pool
         */
        DataSource dataSource = SparqlifyCliHelper.configDataSource(dataSourceOptions, loggerCount);
        if(loggerCount.getErrorCount() != 0) {
            throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
        }


        SparqlifyCoreInit.initSparqlifyFunctions();


        ExprRewriteSystem ers = SparqlifyUtils.createDefaultExprRewriteSystem();
        TypeSystem typeSystem = ers.getTypeSystem();

        // typeAliases for the H2 datatype
        Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");



        String dbProductName;
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData dbMeta = conn.getMetaData();
            dbProductName = dbMeta.getDatabaseProductName();
        }
        logger.info("Database product: " + dbProductName);

        SqlBackendRegistry backendRegistry = SqlBackendRegistry.get();

        SqlBackendConfig backendConfig = backendRegistry.apply(dbProductName);
        if(backendConfig == null) {
            throw new RuntimeException("Could not find backend for " + dbProductName);
        }


        SqlCodec sqlEscaper = backendConfig.getSqlEscaper();
        DatatypeToString typeSerializer = backendConfig.getTypeSerializer();

        try (Connection conn = dataSource.getConnection()) {
            BasicTableInfoProvider basicTableInfoProvider = new BasicTableProviderJdbc(conn);
            SchemaProvider schemaProvider = new SchemaProviderImpl(
                    basicTableInfoProvider,
                    typeSystem,
                    typeAlias,
                    sqlEscaper);
            SyntaxBridge syntaxBridge = new SyntaxBridge(schemaProvider);

            //OpMappingRewriter opMappingRewriter = SparqlifyUtils.createDefaultOpMappingRewriter(typeSystem);
            //MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(typeSystem);
            MappingOps mappingOps = SparqlifyUtils.createDefaultMappingOps(ers);
            //OpMappingRewriter opMappingRewriter = new OpMappingRewriterImpl(mappingOps);


            CandidateViewSelector<ViewDefinition> candidateViewSelector = new CandidateViewSelectorSparqlify(mappingOps, new ViewDefinitionNormalizerImpl());


            //RdfViewSystem system = new RdfViewSystem2();
            ConfiguratorCandidateSelector.configure(config, syntaxBridge, candidateViewSelector, loggerCount);
        }

        logger.info("Errors: " + loggerCount.getErrorCount() + ", Warnings: " + loggerCount.getWarningCount());

        if(loggerCount.getErrorCount() > 0) {
            throw new RuntimeException("Encountered " + loggerCount.getErrorCount() + " errors that need to be fixed first.");
        }

        // boolean useSparql11Wrapper = commandLine.hasOption("1");

        Long mrs = useSparql11Wrapper ? null : maxResultSetSize;


        QueryExecutionFactoryEx qef = FluentSparqlifyFactory.newEngine()
                .setDataSource(dataSource)
                .setConfig(config)
                .setDatatypeToString(new DatatypeToStringPostgres())
                .setSqlEscaper(SqlCodecUtils.createSqlCodecDefault())
                .setMaxQueryExecutionTime(maxQueryExecutionTime)
                .setMaxResultSetSize(mrs)
                .create();

        if(useSparql11Wrapper) {
            Graph graph = new GraphQueryExecutionFactory(qef);
            Model model = ModelFactory.createModelForGraph(graph);
            QueryExecutionFactory tmp = new QueryExecutionFactoryModel(model);
            if(maxResultSetSize != null) {
                tmp = QueryExecutionFactoryLimit.decorate(tmp, true, maxResultSetSize);
            }

            qef = new QueryExecutionFactoryExWrapper(tmp);
        }

        if(isDump) {
            if(outputFormat.equals("nquads")) {
                Iterator<Quad> itQuad = QueryExecutionUtils.createIteratorDumpQuads(qef);
                NQuadsWriter.write(System.out, itQuad);

            }
            else {
                Iterator<Triple> itTriple = QueryExecutionUtils.createIteratorDumpTriples(qef);
                NTriplesWriter.write(System.out, itTriple);
            }

            return 0;
        }
        else if(queryString != null) {
            QueryEx queryEx = QueryFactoryEx.create(queryString);

            if(queryEx.isSelectType()) {
                QueryExecution qe = qef.createQueryExecution(queryEx);
                ResultSet rs = qe.execSelect();
                System.out.println(ResultSetFormatter.asText(rs));
            }
            else if(queryEx.isConstructType()) {
                QueryExecution qe = qef.createQueryExecution(queryString);
                Iterator<Triple> it = qe.execConstructTriples();
                SparqlFormatterUtils.writeText(System.out, it);
                //model.write(System.out, "N-TRIPLES");
            }
            else {
                throw new RuntimeException("Query type not supported: " + queryString);
            }


            return 0;
        }

        Server server = FactoryBeanSparqlServer.newInstance().setPort(port).setSparqlServiceFactory(qef).create();
        server.start();

        return 0;
    }
}
