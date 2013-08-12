package org.aksw.sparqlify.batch;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.aksw.commons.util.MapReader;
import org.aksw.commons.util.strings.StringUtils;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpQuery;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.RdfViewSystemOld;
import org.aksw.sparqlify.core.algorithms.SqlOpSelectBlockCollectorImpl;
import org.aksw.sparqlify.core.algorithms.SqlOpSerializerImpl;
import org.aksw.sparqlify.core.cast.NewWorldTest;
import org.aksw.sparqlify.core.cast.SqlExprSerializerSystem;
import org.aksw.sparqlify.core.cast.TypeSystem;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.RestrictedExpr;
import org.aksw.sparqlify.core.domain.input.ViewDefinition;
import org.aksw.sparqlify.core.interfaces.SqlOpSelectBlockCollector;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.aksw.sparqlify.core.sparql.ItemProcessorSparqlify;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactoryEx;
import org.aksw.sparqlify.core.sparql.RowMapperSparqlifyBinding;
import org.aksw.sparqlify.util.QuadPatternUtils;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.aksw.sparqlify.validation.LoggerCount;
import org.aksw.sparqlify.web.Main;
import org.aksw.sparqlify.web.SparqlifyCliHelper;
import org.antlr.runtime.RecognitionException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.FlowJob;
import org.springframework.batch.core.job.flow.FlowStep;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.job.flow.support.StateTransition;
import org.springframework.batch.core.job.flow.support.state.EndState;
import org.springframework.batch.core.job.flow.support.state.SplitState;
import org.springframework.batch.core.job.flow.support.state.StepState;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;


@Configuration
class DumpConfigProvider {
	public static DumpConfig dumpConfig = null; 
	
	@Bean(name="dumpConfig")
	DumpConfig create() {
		return dumpConfig;
	}
	
	@Bean(name="jobDataSource")
	DataSource createJobDatasource() {
		return dumpConfig.getJobDataSource();
	}
	
	@Bean
	TaskExecutor taskExecutor() {
		//return new SyncTaskExecutor();

		SimpleAsyncTaskExecutor result = new SimpleAsyncTaskExecutor();
		result.setDaemon(true);
		Integer threadCount = dumpConfig.getThreadCount();
		if(threadCount != null) {
			result.setConcurrencyLimit(threadCount);
		}

		return result;
		/*
		ThreadPoolTaskExecutor result = new ThreadPoolTaskExecutor();
		result.setCorePoolSize(4);
		result.setMaxPoolSize(4);
		result.setQueueCapacity(0);
		result.setRejectedExecutionHandler(new CallerRunsPolicy());
		return result;
		*/
	}
}

class DumpConfig {
	private Config config;
	
	private DataSource jobDataSource;
	private DataSource userDataSource;

	private String outBaseDir;
	
	private List<ViewDefinitionStr> viewDefinitionStrs;
	private Integer threadCount;
	
	public DumpConfig(DataSource jobDataSource, DataSource userDataSource, String outBaseDir,
			List<ViewDefinitionStr> viewDefinitionStrs, Integer threadCount) {
		super();
		this.jobDataSource = jobDataSource;
		this.userDataSource = userDataSource;
		this.outBaseDir = outBaseDir;
		this.viewDefinitionStrs = viewDefinitionStrs;
		this.threadCount = threadCount;
	}

	public Config getConfig() {
		return config;
	}
	
	public DataSource getJobDataSource() {
		return jobDataSource;
	}

	public DataSource getUserDataSource() {
		return userDataSource;
	}

	public String getOutBaseDir() {
		return outBaseDir;
	}

	public List<ViewDefinitionStr> getViewDefinitionStrs() {
		return viewDefinitionStrs;
	}
	
	public Integer getThreadCount() {
		return threadCount;
	}
}


class MyItemProcessor
	implements ItemProcessor<Binding, String>
{
	private Multimap<Var, RestrictedExpr> sparqlVarMap;
	private QuadPattern template;
	
	public MyItemProcessor(QuadPattern template, Multimap<Var, RestrictedExpr> sparqlVarMap) {
		this.template = template;
		this.sparqlVarMap = sparqlVarMap;
	}
	
	@Override
	public String process(Binding binding) throws Exception {
		Binding a = ItemProcessorSparqlify.process(sparqlVarMap, binding);
		QuadPattern b = RowMapperSparqlify.map(template, a);
		String c = QuadPatternUtils.toNTripleString(b);
		
		String result = c.isEmpty() ? null : c;
		
		return result;
	}
}

class ViewDefinitionStr
{
	private String name;
	private QuadPattern template;
	private Multimap<Var, RestrictedExpr> sparqlVarMap;
	private String sqlQueryString;
	
	public ViewDefinitionStr(String name, QuadPattern template,
			Multimap<Var, RestrictedExpr> sparqlVarMap, String sqlQueryString) {
		super();
		this.name = name;
		this.template = template;
		this.sparqlVarMap = sparqlVarMap;
		this.sqlQueryString = sqlQueryString;
	}

	public String getName() {
		return name;
	}

	public QuadPattern getTemplate() {
		return template;
	}

	public Multimap<Var, RestrictedExpr> getSparqlVarMap() {
		return sparqlVarMap;
	}

	public String getSqlQueryString() {
		return sqlQueryString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((template == null) ? 0 : template.hashCode());
		result = prime * result
				+ ((sparqlVarMap == null) ? 0 : sparqlVarMap.hashCode());
		result = prime * result
				+ ((sqlQueryString == null) ? 0 : sqlQueryString.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ViewDefinitionStr other = (ViewDefinitionStr) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		if (sparqlVarMap == null) {
			if (other.sparqlVarMap != null)
				return false;
		} else if (!sparqlVarMap.equals(other.sparqlVarMap))
			return false;
		if (sqlQueryString == null) {
			if (other.sqlQueryString != null)
				return false;
		} else if (!sqlQueryString.equals(other.sqlQueryString))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ViewDefinitionStr [name=" + name + ", template ="
				+ template + ", sparqlVarMap=" + sparqlVarMap
				+ ", sqlQueryString=" + sqlQueryString + "]";
	}	
}


class ViewDefinitionStrFactory
{
	private SqlOpSelectBlockCollector sqlOpSelectBlockCollector;
	private SqlOpSerializer sqlOpSerializer;
	
	public ViewDefinitionStrFactory(
			SqlOpSelectBlockCollector sqlOpSelectBlockCollector,
			SqlOpSerializer sqlOpSerializer) {
		super();
		this.sqlOpSelectBlockCollector = sqlOpSelectBlockCollector;
		this.sqlOpSerializer = sqlOpSerializer;
	}

	public ViewDefinitionStr createView(ViewDefinition viewDefinition) {
		
		String name = viewDefinition.getName();
		QuadPattern template = viewDefinition.getTemplate();
		
		Mapping mapping = viewDefinition.getMapping();
		SqlOp sqlOp = mapping.getSqlOp();
		
		Multimap<Var, RestrictedExpr> sparqlVarMap = viewDefinition.getVarDefinition().getMap();
		
		
		// TODO HACK The select block collector assigns an alias to any SqlOpQuery, which breaks if the initial sqlOp is already of type SqlOpQuery
		SqlOp tmp;
		if(sqlOp instanceof SqlOpQuery) {
			tmp = sqlOp;
		} else {
			tmp = sqlOpSelectBlockCollector.transform(sqlOp);
		}
		
		String sqlQueryString = sqlOpSerializer.serialize(tmp);
		
		//sqlQueryString = "Select * from nodes";
		
		ViewDefinitionStr result = new ViewDefinitionStr(name, template, sparqlVarMap, sqlQueryString);

		return result;
	}
}


@Configuration
@EnableBatchProcessing
//@Import(DataSourceConfig.class)
public class MainSparqlifyBatchDumper {
	@Autowired
	private JobBuilderFactory jobs;
	
	@Autowired
	private StepBuilderFactory steps;

	@Autowired
	private DumpConfig dumpConfig;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	//@Autowired
	//private JobExplorer jobExplorer;
	
	@Autowired
	private JobRepository jobRepository;
	
	@Autowired
	private TaskExecutor taskExecutor;
	
//	@Autowired
//	private Logger logger;
	
	
	public SimpleFlow create(List<Step> steps) throws Exception {
		
		List<Flow> flows = new ArrayList<Flow>();
		int i = 1;
		for(Step step : steps) {
			List<StateTransition> stateTransitions = new ArrayList<StateTransition>();
			
			
			String nextStateName = "state-" + i;			
			
			StepState stepState = new StepState(step);
			
			stateTransitions.add(StateTransition.createStateTransition(stepState, nextStateName));
			stateTransitions.add(StateTransition.createEndStateTransition(new EndState(FlowExecutionStatus.COMPLETED, nextStateName)));
			//StateTransition.createStateTransition(stepState, next);

			//StateTransition startToSuccess = StateTransition.createEndStateTransition(new EndState(FlowExecutionStatus.COMPLETED, ));
			//StateTransition stateToFail = StateTransition.createEndStateTransition(new EndState(FlowExecutionStatus.FAILED, "end3"));

			//stateTransitions.add(stateToFail);

			
			SimpleFlow flow = new SimpleFlow("flow-" + i);
			
			flow.setStateTransitions(stateTransitions);
			flow.afterPropertiesSet();

			flows.add(flow);
		}
		SplitState splitState = new SplitState(flows, "splitState");
		splitState.setTaskExecutor(taskExecutor);

		
		
		SimpleFlow outerFlow = new SimpleFlow("main");
		List<StateTransition> outerTransitions = new ArrayList<StateTransition>();
		outerTransitions.add(StateTransition.createStateTransition(splitState, "fullEnd"));
		outerTransitions.add(StateTransition.createEndStateTransition(new EndState(FlowExecutionStatus.COMPLETED, "fullEnd")));

		outerFlow.setStateTransitions(outerTransitions);
		outerFlow.afterPropertiesSet();
		return outerFlow;
	}

	
	
	@Bean
	public Job job() throws Exception {

		//SimpleJobLauncher simpleJobLauncher = (SimpleJobLauncher) jobLauncher;
		//simpleJobLauncher.setJobRepository(jobRepository);
		//simpleJobLauncher.setTaskExecutor(taskExecutor);
		//jobLauncher = simpleJobLauncher;
		
	
		JobExplorerFactoryBean jobExplorerFactory = new JobExplorerFactoryBean();
		jobExplorerFactory.setDataSource(dumpConfig.getJobDataSource());
		jobExplorerFactory.afterPropertiesSet();
		
		JobExplorer jobExplorer = (JobExplorer)jobExplorerFactory.getObject();
		
		
		
		//System.out.println(jobLauncher);
		
		Date endTime = new Date();

		List<String> jobNames = jobExplorer.getJobNames();
		for(String jobName : jobNames) {
			List<JobInstance> jobInstances = jobExplorer.getJobInstances(jobName, 0, 1000000);
			
			for(JobInstance jobInstance : jobInstances) {
				List<JobExecution> jobExecutions = jobExplorer.getJobExecutions(jobInstance);
				
				for(JobExecution jobExecution : jobExecutions) {
					
					Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
					for(StepExecution stepExecution : stepExecutions) {
						BatchStatus stepStatus = stepExecution.getStatus();
						
						if(stepStatus.equals(BatchStatus.STARTED)) {
							stepExecution.setStatus(BatchStatus.STOPPED);
							stepExecution.setEndTime(endTime);
							jobRepository.update(stepExecution);
						}
					}
					
					BatchStatus jobStatus = jobExecution.getStatus();
					if(jobStatus.equals(BatchStatus.STARTED)) {
						jobExecution.setStatus(BatchStatus.STOPPED);
						jobExecution.setEndTime(endTime);
						jobRepository.update(jobExecution);
					}
				}
			}
		}
		
		
		String jobName = "dumpJob";

		List<ViewDefinitionStr> viewDefinitionStrs = new ArrayList<ViewDefinitionStr>(dumpConfig.getViewDefinitionStrs());
		Collections.sort(viewDefinitionStrs, new Comparator<ViewDefinitionStr>() {

			@Override
			public int compare(ViewDefinitionStr a, ViewDefinitionStr b) {
				return a.getName().compareTo(b.getName());
			}

		});
	

		
		DataSource userDataSource = dumpConfig.getUserDataSource();
		
		JobBuilder jobBuilder = jobs.get(jobName);		
		SimpleJobBuilder sjb = null;
		
		FlowJob flowJob = new FlowJob(jobName);
		
		List<StateTransition> transitions = new ArrayList<StateTransition>();
		
		//SplitBuilder<FlowJobBuilder> sjb = null;
		
		boolean isSequentialStepExecution = false;


		List<Step> steps = new ArrayList<Step>();
		for(ViewDefinitionStr vds : viewDefinitionStrs) {

			String baseName = StringUtils.urlEncode(vds.getName());
			String flowName = "flow-" + baseName;
			
			//String taskletName = "dump-" + baseName;

			//loggerCount.info("Processing view [" + viewDefinition.getName() + "]");
			//Tasklet tasklet = taskletFactory.createTasklet(viewDefinition) ;
			
			System.out.println("Creating step for " + vds);
			
			Step step = createStep("foobar-", userDataSource, vds, dumpConfig.getOutBaseDir()); 

			
			
			if(isSequentialStepExecution) {

				if(sjb == null) {
					sjb = jobBuilder.start(step);
					//sjb = jobBuilder.flow(step).split(taskExecutor);
				} else {
					sjb.next(step);
					//Flow x = jobBuilder.flow(step).end().build().
				}
				
			} else {

				steps.add(step);
				
			}
		}
		

		Job result;
		if(isSequentialStepExecution) {
			result  = sjb.build(); 
		} else {
			SimpleFlow flow = create(steps);
			FlowStep flowStep = new FlowStep(flow);
			flowStep.setFlow(flow);
			flowStep.setJobRepository(jobRepository);
			flowStep.afterPropertiesSet();
		
			result = jobBuilder.start(flowStep).build();			
		}
		
		
		
		//Job result = jobBuilder.

		JobParameters jobParameters = new JobParameters();
		jobLauncher.run(result, jobParameters);
		
		return result;
	}

	//@Bean
	protected Step createStep(String stepName, DataSource dataSource, ViewDefinitionStr vds, String outBaseDir) throws Exception {

		String baseName = StringUtils.urlEncode(vds.getName());
		String outFileName = outBaseDir + "/" + baseName + ".nt";
		Resource outResource = new FileSystemResource(outFileName);

		
		RowMapper<Binding> rowMapper = new RowMapperSparqlifyBinding();
		
		JdbcCursorItemReader<Binding> itemReader = new JdbcCursorItemReader<Binding>();
		itemReader.setFetchSize(50000);
		itemReader.setSaveState(false);
		itemReader.setVerifyCursorPosition(false);
		//itemReader.setSaveState(true);
		itemReader.setSql(vds.getSqlQueryString());
		itemReader.setDataSource(dataSource);
		itemReader.setRowMapper(rowMapper);
		itemReader.afterPropertiesSet();
		//itemReader.setSaveState(true);
		//itemReader.afterPropertiesSet();

		
		// DataSourceTransactionManager
		
		FlatFileItemWriter<String> itemWriter = new FlatFileItemWriter<String>();
		//itemWriter.set
		itemWriter.setLineAggregator(new PassThroughLineAggregator<String>());
		itemWriter.setResource(outResource);
		itemWriter.setShouldDeleteIfExists(false);
		itemWriter.setForceSync(false);
		itemWriter.setTransactional(false);
		itemWriter.afterPropertiesSet();
		
		
		//itemWriter.setSaveState(true);
		//itemWriter.setAppendAllowed(true);
		//itemWriter.afterPropertiesSet();
		
		
//		int commitInterval = 50000;
//		CompletionPolicy completionPolicy = new SimpleCompletionPolicy(commitInterval);
//		RepeatTemplate repeatTemplate = new RepeatTemplate();
//		repeatTemplate.setCompletionPolicy(completionPolicy);
//		//repeatTemplate.set
//
//		RepeatOperations repeatOperations = repeatTemplate;
//		ChunkProvider<QuadPattern> chunkProvider = new SimpleChunkProvider<QuadPattern>(itemReader, repeatOperations);
//		//JobStep
		

		ItemProcessor<Binding, String> itemProcessor = new MyItemProcessor(vds.getTemplate(), vds.getSparqlVarMap());

		Step result = steps.get(vds.getName())
				.<Binding, String>chunk(10000)
				.reader(itemReader)
				.processor(itemProcessor)
				.writer(itemWriter)
				.build();

		return result;
	}
//
//	@Bean
//	protected Step step2(Tasklet tasklet) {
//		return steps.get("step2")
//		.tasklet(tasklet)
//		.build();
//	}
	
	private static final Logger logger = LoggerFactory.getLogger(MasterDumper.class);

	private static final Options cliOptions = new Options();

	static {
		cliOptions.addOption("t", "type", true,
				"Database type (posgres, mysql,...)");
		cliOptions.addOption("d", "database", true, "Database name");
		cliOptions.addOption("u", "username", true, "");
		cliOptions.addOption("p", "password", true, "");
		cliOptions.addOption("h", "hostname", true, "");
		cliOptions.addOption("c", "class", true, "JDBC driver class");
		cliOptions.addOption("j", "jdbcurl", true, "JDBC URL");
		cliOptions.addOption("o", "outfolder", true, "Folder where to write the output");
		cliOptions.addOption("T", "threads", true, "Number of threads to use for dumping views in parallel");
		
		// TODO Rename to m for mapping file soon
		cliOptions.addOption("m", "mapping", true, "Sparqlify mapping file");

		RdfViewSystemOld.initSparqlifyFunctions();
	}
	
	public static DumpConfig parseCliArgs(String[] args) throws ClassNotFoundException, ParseException, IOException, RecognitionException, SQLException {
		
		LoggerCount loggerCount = new LoggerCount(logger);
		Class.forName("org.postgresql.Driver");
		
		CommandLineParser cliParser = new GnuParser();



		CommandLine commandLine = cliParser.parse(cliOptions, args);
		
		
		
		File outBaseDir = SparqlifyCliHelper.parseFile(commandLine, "o", false, loggerCount);
		String outBaseDirName = outBaseDir.getAbsolutePath();
		
		if(outBaseDir.exists() && !outBaseDir.isDirectory()) {
			loggerCount.error("Folder required; " + outBaseDirName + " is refers to file.");
		} else if(!outBaseDir.exists()) {
			outBaseDir.mkdirs();
		}

		
		Integer threadCount = SparqlifyCliHelper.parseInt(commandLine, "T", false, loggerCount);
		
		DataSource userDataSource = SparqlifyCliHelper.parseDataSource(commandLine, loggerCount);
		Config config = SparqlifyCliHelper.parseSmlConfig(commandLine, loggerCount);
		
		Main.onErrorPrintHelpAndExit(cliOptions, loggerCount, -1);
		
		
		//DataSource dataSource = SparqlifyUtils.createDefaultDatabase("batchtest");

		TypeSystem typeSystem = NewWorldTest.createDefaultDatatypeSystem();
		SqlExprSerializerSystem serializerSystem = SparqlifyUtils.createSerializerSystem(typeSystem);
		
		SqlOpSelectBlockCollector sqlOpSelectBlockCollector = new SqlOpSelectBlockCollectorImpl();
		
		SqlOpSerializer sqlOpSerializer = new SqlOpSerializerImpl(serializerSystem);
		

		
		Map<String, String> typeAlias = MapReader.readFromResource("/type-map.h2.tsv");

		List<ViewDefinition> viewDefinitions = SparqlifyCliHelper.extractViewDefinitions(config.getViewDefinitions(), userDataSource, typeSystem, typeAlias, loggerCount);
		Main.onErrorPrintHelpAndExit(cliOptions, loggerCount, -1);


		ViewDefinitionStrFactory vdsFactory = new ViewDefinitionStrFactory(sqlOpSelectBlockCollector, sqlOpSerializer);

		List<ViewDefinitionStr> viewDefinitionStrs = new ArrayList<ViewDefinitionStr>();
		for(ViewDefinition viewDefinition : viewDefinitions) {
			ViewDefinitionStr vds = vdsFactory.createView(viewDefinition);
			
			viewDefinitionStrs.add(vds);
		}
		
		
		
		// TODO Check if a database file already exists
		// (alternatively: if a DB server is already running???)
		DataSource jobDataSource = MasterDumper.createJobDataSource("sparqlify-dump");
		MasterDumper.populateSpringBatchH2(jobDataSource);
		
		DumpConfig dumpConfig = new DumpConfig(jobDataSource, userDataSource, outBaseDirName, viewDefinitionStrs, threadCount);

		return dumpConfig;
	}
	
	public static void main(String[] args) throws Exception {


		DumpConfig dumpConfig = parseCliArgs(args);
		DumpConfigProvider.dumpConfig = dumpConfig;
		
		//classpath:
		Resource springBatchSml = new ClassPathResource("org/springframework/batch/rdb2rdf/rdf-mapping-h2.sml");
		Config config = SparqlifyUtils.parseSmlConfig(springBatchSml.getInputStream(), logger);
		
		
		logger.info("Processing init parameters complete, preparing launch ...");
		
		DataSource jobDataSource = dumpConfig.getJobDataSource();

		QueryExecutionFactoryEx qef = SparqlifyUtils.createDefaultSparqlifyEngine(jobDataSource, config, 1000l, 30);

		final Server server = Main.createSparqlEndpoint(qef, 5544); 
		
		try {
			Thread thread = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						server.start();
					} catch(Exception e) {
						throw new RuntimeException(e);
					}
				}
				
			});
			thread.start();
	
			Thread.sleep(2000);


			ApplicationContext context = new AnnotationConfigApplicationContext(DumpConfigProvider.class, MainSparqlifyBatchDumper.class);
			context.getBean("job");
		}
		finally {
			server.stop();
		}


		//context.
	}
}
