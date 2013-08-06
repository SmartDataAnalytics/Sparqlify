package org.aksw.sparqlify.web;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

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

import com.hp.hpl.jena.sparql.core.BasicPattern;


class RowMapperSparqlify
	implements RowMapper<BasicPattern>
{
	//public 
	
	@Override
	public BasicPattern mapRow(ResultSet rs, int rowNumber) throws SQLException {
		BasicPattern result = new BasicPattern();
		//result.add(;)
		// TODO Auto-generated method stub
		return result;
	}
	
}


// This seems to be an example of what I try to accomplish: https://github.com/wbotelhos/spring-batch-database-flat-file
public class MasterDumper {
	public static void main(String[] args) throws Exception {
		//DataSource dataSource = SparqlifyUtils.createDefaultDatabase("batchtest");
		

		DataSource dataSource = new EmbeddedDatabaseBuilder()
           .setType(EmbeddedDatabaseType.H2)
           .addScript("org/springframework/batch/core/schema-drop-h2.sql")
           .addScript("org/springframework/batch/core/schema-h2.sql")
           .build();
//		<jdbc:initialize-database data-source="dataSource">
//		<jdbc:script location="org/springframework/batch/core/schema-drop-mysql.sql" />
//		<jdbc:script location="org/springframework/batch/core/schema-mysql.sql" />
//	</jdbc:initialize-database>
		
		String fileName = "foobar.txt";
		
		AbstractPlatformTransactionManager transactionManager = new ResourcelessTransactionManager();
		
		JobRepositoryFactoryBean jobRepositoryFactory = new JobRepositoryFactoryBean();
		jobRepositoryFactory.setDatabaseType("h2");
		jobRepositoryFactory.setTransactionManager(transactionManager);
		jobRepositoryFactory.setDataSource(dataSource);
		jobRepositoryFactory.afterPropertiesSet();
		
		JobRepository jobRepository = jobRepositoryFactory.getJobRepository();
		
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		jobParametersBuilder.addString("fileName", fileName);
		
		JobParameters jobParameters = jobParametersBuilder.toJobParameters();
		
		TaskletStep taskletStep = new TaskletStep(); 
		taskletStep.setName("step1");
		taskletStep.setJobRepository(jobRepository);
		taskletStep.setTransactionManager(transactionManager);
		
		RowMapper rowMapper = new RowMapperSparqlify();
		
		JdbcCursorItemReader<String> itemReader = new JdbcCursorItemReader<String>();
		itemReader.setSql("SELECT 1");
		itemReader.setDataSource(dataSource);
		itemReader.setRowMapper(new RowMapper<String>() {
			@Override
			public String mapRow(ResultSet arg0, int arg1) throws SQLException {
				return "test";
			}
		});		
		itemReader.afterPropertiesSet();
		
		ExecutionContext executionContext = new ExecutionContext();
		itemReader.open(executionContext);

		//itemReader.setRowMapper(rowMapper);
		
		Resource resource = new FileSystemResource("/tmp/test.yahoo");
		
		FlatFileItemWriter<String> itemWriter = new FlatFileItemWriter<String>();
		itemWriter.setLineAggregator(new PassThroughLineAggregator<String>());
		itemWriter.setResource(resource);
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
		taskletStep.setTasklet(tasklet);
		taskletStep.afterPropertiesSet();
		
		SimpleJob job = new SimpleJob("test");
		job.addStep(taskletStep); 
		job.setJobRepository(jobRepository);
		job.afterPropertiesSet();
	
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository);
		jobLauncher.afterPropertiesSet();
		
		JobExecution execution = jobLauncher.run(job, jobParameters);
		System.out.println("Exit Status : " + execution.getStatus());
		
		//JobExecution jobExecution = jobRepository.createJobExecution("test", jobParameters);
		//jobExecution.
		
		//jobLauncherTestUtils.setJob(job);
		//JobExecution jobExecution = jobLauncherTestUtils.launchJob(parameters);
		//assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
	}
}
