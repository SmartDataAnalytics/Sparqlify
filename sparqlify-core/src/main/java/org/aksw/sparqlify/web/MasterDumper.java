package org.aksw.sparqlify.web;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.config.lang.ConfigParser;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.config.v0_2.bridge.SyntaxBridge;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.cast.NewWorldTest;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.core.sparql.RowMapperSparqlifyBinding;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.validation.LoggerCount;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.item.ChunkOrientedTasklet;
import org.springframework.batch.core.step.item.ChunkProcessor;
import org.springframework.batch.core.step.item.ChunkProvider;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProvider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.RepeatOperations;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.modify.TemplateLib;


class RowMapperSparqlify
	implements RowMapper<QuadPattern>
{
	private RowMapper<Binding> rowMapper;
	private QuadPattern quadPattern;
	
	public RowMapperSparqlify(RowMapper<Binding> rowMapper, QuadPattern quadPattern) {
		this.rowMapper = rowMapper;
		this.quadPattern = quadPattern;
	}
	
	//public 
	
	@Override
	public QuadPattern mapRow(ResultSet rs, int rowNumber) throws SQLException {
		Binding binding = rowMapper.mapRow(rs, rowNumber);
		Map<Node, Node> bNodeMap = Collections.emptyMap();
		
		QuadPattern result = new QuadPattern();
		
		for(Quad quad : quadPattern.getList()) {
			Quad tmp = TemplateLib.subst(quad, binding, bNodeMap);
			result.add(tmp);
		}
		
		return result;
	}
}

class TaskletFactoryDump {
//	private JobRepository jobRepository;
//	private AbstractPlatformTransactionManager transactionManager;
	private DataSource dataSource;
	
	private SqlOpSerializer sqlOpSerializer;
	private String outBaseDir = "/tmp/";

//	public TaskletFactoryDump(JobRepository jobRepository, AbstractPlatformTransactionManager transactionManager, DataSource dataSource, SqlOpSerializer sqlOpSerializer, String outBaseDir) {
//		this.jobRepository = jobRepository;
//		this.transactionManager = transactionManager;
//		this.dataSource = dataSource;
//		this.sqlOpSerializer = sqlOpSerializer;
//		this.outBaseDir = outBaseDir;
//	}

	public TaskletFactoryDump(DataSource dataSource, SqlOpSerializer sqlOpSerializer, String outBaseDir) {
		this.dataSource = dataSource;
		this.sqlOpSerializer = sqlOpSerializer;
		this.outBaseDir = outBaseDir;
	}

	
	public Tasklet createTasklet(ViewDefinition viewDefinition) {
		QuadPattern template = viewDefinition.getTemplate();
		
		Mapping mapping = viewDefinition.getMapping();
		SqlOp sqlOp = mapping.getSqlOp();
		
		Multimap<Var, RestrictedExpr> sparqlVarMap = viewDefinition.getVarDefinition().getMap();
		
		String sqlStr = sqlOpSerializer.serialize(sqlOp);
		
		
		String baseName = StringUtils.urlEncode(viewDefinition.getName());
//		String taskletName = "dump-" + baseName;
		String outFileName = outBaseDir + baseName + ".nt";
		Resource outResource = new FileSystemResource(outBaseDir + outFileName);
		
		Tasklet result = createTasklet(dataSource, template, sparqlVarMap, sqlStr, outResource);
		return result;
	}

	public static Tasklet createTasklet(DataSource dataSource, QuadPattern template, Multimap<Var, RestrictedExpr> sparqlVarMap, String sqlStr, Resource outResource) {
		Tasklet result;
		try {
			result = _createTasklet(dataSource, template, sparqlVarMap, sqlStr, outResource);
		}
		catch(Exception e) {
			throw new RuntimeException(e);
		}
		 
		return result;
	}
	
	public static Tasklet _createTasklet(DataSource dataSource, QuadPattern template, Multimap<Var, RestrictedExpr> sparqlVarMap, String sqlStr, Resource outResource) throws Exception {
		
		QuadPattern quadPattern = new QuadPattern();
		
		RowMapper<Binding> coreRowMapper = new RowMapperSparqlifyBinding(sparqlVarMap); 
		RowMapper<QuadPattern> rowMapper = new RowMapperSparqlify(coreRowMapper, quadPattern);
				
		JdbcCursorItemReader<String> itemReader = new JdbcCursorItemReader<String>();
		itemReader.setSql(sqlStr);
		itemReader.setDataSource(dataSource);
		itemReader.setRowMapper(rowMapper);
		itemReader.afterPropertiesSet();
		
		ExecutionContext executionContext = new ExecutionContext();
		itemReader.open(executionContext);


		//itemReader.setRowMapper(rowMapper);		

		
		FlatFileItemWriter<String> itemWriter = new FlatFileItemWriter<String>();
		itemWriter.setLineAggregator(new PassThroughLineAggregator<String>());
		itemWriter.setResource(outResource);
		itemWriter.afterPropertiesSet();
		
		itemWriter.open(executionContext);
		
		
		int commitInterval = 10;
		CompletionPolicy completionPolicy = new SimpleCompletionPolicy(commitInterval);
		RepeatTemplate repeatTemplate = new RepeatTemplate();
		repeatTemplate.setCompletionPolicy(completionPolicy);
		//repeatTemplate.set

		RepeatOperations repeatOperations = repeatTemplate;
		ChunkProvider<String> chunkProvider = new SimpleChunkProvider<String>(itemReader, repeatOperations);
		//JobStep
		
		ItemProcessor<String, String> itemProcessor = new PassThroughItemProcessor<String>();		
		ChunkProcessor<String> chunkProcessor = new SimpleChunkProcessor<String, String>(itemProcessor, itemWriter);
		
		Tasklet tasklet = new ChunkOrientedTasklet<String>(chunkProvider, chunkProcessor); //new SplitFilesTasklet();
		
		return tasklet;
	}
	
}

// This seems to be an example of what I try to accomplish: https://github.com/wbotelhos/spring-batch-database-flat-file
public class MasterDumper {
	
	private static final Logger logger = LoggerFactory.getLogger(MasterDumper.class);

	
	public void createTasks(InputStream inSpec) throws IOException, RecognitionException {
		Logger loggerCount = null;
		SyntaxBridge bridge = null;

		ConfigParser parser = new ConfigParser();

		//InputStream in = new FileInputStream();
		Config config = parser.parse(inSpec, loggerCount);
		
		
		List<ViewDefinition> viewDefinitions = SyntaxBridge.bridge(bridge, config.getViewDefinitions(), loggerCount);
		
		// Create a DumpTask for each view definition
		for(ViewDefinition viewDefinition : viewDefinitions) {
			String viewName = viewDefinition.getName();
		}

	}
	
	

	private static final Options cliOptions = new Options();
	
	public static void main(String[] args) throws Exception {
		
		LoggerCount loggerCount = new LoggerCount(logger);
		Class.forName("org.postgresql.Driver");
		
		CommandLineParser cliParser = new GnuParser();

		cliOptions.addOption("t", "type", true,
				"Database type (posgres, mysql,...)");
		cliOptions.addOption("d", "database", true, "Database name");
		cliOptions.addOption("u", "username", true, "");
		cliOptions.addOption("p", "password", true, "");
		cliOptions.addOption("h", "hostname", true, "");
		cliOptions.addOption("c", "class", true, "JDBC driver class");
		cliOptions.addOption("j", "jdbcurl", true, "JDBC URL");

		// TODO Rename to m for mapping file soon
		cliOptions.addOption("m", "mapping", true, "Sparqlify mapping file");

		CommandLine commandLine = cliParser.parse(cliOptions, args);
		
		DataSource userDataSource = SparqlifyCliHelper.parseDataSource(commandLine, loggerCount);
		Config config = SparqlifyCliHelper.parseSmlConfig(commandLine, loggerCount);
		
		Main.onErrorPrintHelpAndExit(cliOptions, loggerCount, -1);
		
		
		//DataSource dataSource = SparqlifyUtils.createDefaultDatabase("batchtest");

		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		SqlExprSerializerSystem serializerSystem = SparqlifyUtils.createSerializerSystem(typeSystem);
		SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(serializerSystem);

		
		Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");

		List<ViewDefinition> viewDefinitions = SparqlifyCliHelper.extractViewDefinitions(config.getViewDefinitions(), userDataSource, typeSystem, typeAlias, loggerCount);
		Main.onErrorPrintHelpAndExit(cliOptions, loggerCount, -1);

		
		
		
		// TODO Check if a database file already exists
		// (alternatively: if a DB server is already running???)

		DataSource jobDataSource = new EmbeddedDatabaseBuilder()
           .setType(EmbeddedDatabaseType.H2)
           .addScript("org/springframework/batch/core/schema-drop-h2.sql")
           .addScript("org/springframework/batch/core/schema-h2.sql")
           .build();
//		<jdbc:initialize-database data-source="dataSource">
//		<jdbc:script location="org/springframework/batch/core/schema-drop-mysql.sql" />
//		<jdbc:script location="org/springframework/batch/core/schema-mysql.sql" />
//	</jdbc:initialize-database>
		
		AbstractPlatformTransactionManager transactionManager = new ResourcelessTransactionManager();
		
		JobRepositoryFactoryBean jobRepositoryFactory = new JobRepositoryFactoryBean();
		jobRepositoryFactory.setDatabaseType("h2");
		jobRepositoryFactory.setTransactionManager(transactionManager);
		jobRepositoryFactory.setDataSource(jobDataSource);
		jobRepositoryFactory.afterPropertiesSet();
		
		JobRepository jobRepository = jobRepositoryFactory.getJobRepository();
		
		
		String outBaseDir = "/tmp/";
		TaskletFactoryDump taskletFactory = new TaskletFactoryDump(userDataSource, sqlOpSerializer, outBaseDir);
		
		// TODO For each viewDefinition ...
		SimpleJob job = new SimpleJob("test");
		job.setJobRepository(jobRepository);

		for(ViewDefinition viewDefinition : viewDefinitions) {

			String baseName = StringUtils.urlEncode(viewDefinition.getName());
			String taskletName = "dump-" + baseName;

			Tasklet tasklet = taskletFactory.createTasklet(viewDefinition) ;

			TaskletStep taskletStep = new TaskletStep(); 
			taskletStep.setName(taskletName);
			taskletStep.setJobRepository(jobRepository);
			taskletStep.setTransactionManager(transactionManager);

			
			taskletStep.setTasklet(tasklet);
			taskletStep.afterPropertiesSet();

			job.addStep(taskletStep); 
		}
		
		
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		//jobParametersBuilder.addString("fileName", fileName);
		
		JobParameters jobParameters = jobParametersBuilder.toJobParameters();


		
		job.afterPropertiesSet();
	
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.afterPropertiesSet();
		
		JobExecution execution = jobLauncher.run(job, jobParameters);
		logger.info("Exit Status : " + execution.getStatus());
		
		//JobExecution jobExecution = jobRepository.createJobExecution("test", jobParameters);
		//jobExecution.
		
		//jobLauncherTestUtils.setJob(job);
		//JobExecution jobExecution = jobLauncherTestUtils.launchJob(parameters);
		//assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}
}
