package org.aksw.sparqlify.admin.web.main;

import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.sql.DataSource;

import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.service_framework.core.ServiceLauncherRdb2Rdf;
import org.aksw.service_framework.core.ServiceRepository;
import org.aksw.service_framework.core.SparqlServiceManager;
import org.aksw.service_framework.jpa.core.ServiceRepositoryJpaImpl;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.admin.model.Rdb2RdfExecution;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.algorithms.MappingOpsImpl;
import org.aksw.sparqlify.core.algorithms.OpMappingRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SparqlSqlStringRewriterImpl;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriterImpl;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.core.sparql.QueryExecutionFactorySparqlifyDs;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMapper;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMapperImpl;
import org.aksw.sparqlify.jpa.EntityInverseMapper;
import org.aksw.sparqlify.jpa.EntityInverseMapperImplHibernate;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateExceptionTranslator;
import org.springframework.orm.jpa.JpaDialect;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaDialect;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

@Configuration
@ComponentScan("org.aksw.sparqlify.admin.web") //.api
@EnableWebMvc
@EnableTransactionManagement
@PropertySource("classpath:config/jdbc/jdbc.properties")
public class WebAppConfig {
	
	
	private static final Logger logger = LoggerFactory.getLogger(WebAppConfig.class);
	
	private static final String JDBC_DRIVER = "jdbc.driver";
	private static final String JDBC_PASSWORD = "jdbc.password";
	private static final String JDBC_URL = "jdbc.url";
	private static final String JDBC_USERNAME = "jdbc.username";

	private static final String HIBERNATE_DIALECT = "hibernate.dialect";
	private static final String HIBERNATE_SHOW_SQL = "hibernate.showSql";
	private static final String HIBERNATE_HBM2DDL_AUTO = "hibernate.hbm2ddl.auto";
	//private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";

	@Resource
	private Environment env;
	
	@Bean
	public DataSource dataSource() {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();

		dataSource.setDriverClassName(env.getRequiredProperty(JDBC_DRIVER));
		dataSource.setUrl(env.getRequiredProperty(JDBC_URL));
		dataSource.setUsername(env.getRequiredProperty(JDBC_USERNAME));
		dataSource.setPassword(env.getRequiredProperty(JDBC_PASSWORD));

		return dataSource;
	}

//	@Bean
//	@Autowired
//	public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
//		LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
//		sessionFactoryBean.setDataSource(dataSource);
//		sessionFactoryBean
//				.setPackagesToScan(env
//						.getRequiredProperty(PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN));
//		sessionFactoryBean.setHibernateProperties(hibProperties());
//		return sessionFactoryBean;
//	}
//
	private Properties getHibernateProperties() {
		Properties properties = new Properties();
		properties.put(HIBERNATE_DIALECT, env.getRequiredProperty(HIBERNATE_DIALECT));
		properties.put(HIBERNATE_SHOW_SQL, env.getRequiredProperty(HIBERNATE_SHOW_SQL));
		properties.put(HIBERNATE_HBM2DDL_AUTO, env.getRequiredProperty(HIBERNATE_HBM2DDL_AUTO));
		
		properties.put("hibernate.current_session_context_class", "thread");
		
		return properties;
	}

//	@Bean
//	@Autowired
//	public HibernateTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory) {
//
//		HibernateTransactionManager transactionManager = new HibernateTransactionManager();
//		transactionManager.setSessionFactory(sessionFactory.getObject());
//
//		// Force creation of the schema
//		Session session = transactionManager.getSessionFactory().openSession();
//		Transaction tx = session.beginTransaction();
//		tx.commit();
//		
//		session.close();
//		
//		
//		return transactionManager;
//	}

	@Bean
	public UrlBasedViewResolver setupViewResolver() {
		UrlBasedViewResolver resolver = new UrlBasedViewResolver();
		resolver.setPrefix("/WEB-INF/pages/");
		resolver.setSuffix(".jsp");
		resolver.setViewClass(JstlView.class);
		return resolver;
	}
	
	
	
    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }
 
    @Bean
    @Autowired
    public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(true);
        vendorAdapter.setShowSql(false);
        //vendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQL5InnoDBDialect");
        vendorAdapter.setDatabasePlatform("org.hibernate.dialect.PostgreSQLDialect");
        vendorAdapter.setDatabase(Database.POSTGRESQL);
 
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setJpaVendorAdapter(vendorAdapter);
        factory.setPackagesToScan("org.aksw.service_framework.jpa.model", "org.aksw.sparqlify.admin.model");
        factory.setDataSource(dataSource);

        Properties properties = getHibernateProperties();
//        Properties properties = new Properties();
//        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
//        properties.setProperty("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.EhCacheRegionFactory");
//        properties.setProperty("hibernate.cache.use_query_cache", "true");
//        properties.setProperty("hibernate.generate_statistics", "true");
 
        factory.setJpaProperties(properties);
 
        factory.afterPropertiesSet();
 
        return factory.getObject();
    }
 
    @Bean
    @Autowired
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        JpaDialect jpaDialect = new HibernateJpaDialect();
        txManager.setEntityManagerFactory(entityManagerFactory);
        txManager.setJpaDialect(jpaDialect);
        return txManager;
    }
    
	
	
	/**
	 * 
	 * TODO We must ensure that the schema exists prior to mapping it
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@Autowired
	public QueryExecutionFactory managerApiQef(DataSource dataSource)
		throws Exception
	{
		LoggerCount loggerCount = new LoggerCount(logger);
		InputStream in = this.getClass().getResourceAsStream("/sparqlify-web-manager.sml");
		Config config = SparqlifyUtils.readConfig(in, loggerCount);
		
		if(loggerCount.getErrorCount() != 0 || loggerCount.getWarningCount() != 0) {
			throw new RuntimeException("Errors reading mapping encountered");
		}
		
		QueryExecutionFactory result = SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, config, 1000l, 60);
		return result;
	}

	
	
	@Bean
	@Autowired
	public SparqlServiceManager sparqlServiceConfig(JpaTransactionManager txManager) {
		
		
		EntityManagerFactory emf = txManager.getEntityManagerFactory();
		
		ServiceRepository<QueryExecutionFactory> serviceRepo =
			ServiceRepositoryJpaImpl.create(
				emf,
				Rdb2RdfConfig.class,
				Rdb2RdfExecution.class,
				new ServiceLauncherRdb2Rdf()
			);

		serviceRepo.startAll();

		//SparqlServiceManager result = new SparqlServiceManagerImpl(emf);
		return null;
	}


	@Bean
	@Autowired
	public SessionFactory sessionFactory(JpaTransactionManager txManager) {
		EntityManagerFactory emf = txManager.getEntityManagerFactory();
		SessionFactory result = ((HibernateEntityManagerFactory)emf).getSessionFactory();
		return result;
	}

	
	@Bean
	@Autowired
	public SparqlSqlOpRewriterImpl sparqlSqlOpRewriter(QueryExecutionFactory qef) {
		SparqlSqlOpRewriterImpl result = unwrapOpRewriter(qef);
		return result;
	}

	@Bean
	@Autowired
	public SqlTranslator sqlTranslator(SparqlSqlOpRewriterImpl opRewriter) {
		SqlTranslator result = unwrapSqlTransformer(opRewriter);
		return result;
	}

	@Bean
	@Autowired
	public CandidateViewSelectorImpl candidateViewSelector(SparqlSqlOpRewriterImpl opRewriter) {
		CandidateViewSelectorImpl result = unwrapCandidateViewSelector(opRewriter);
		return result;
	}

	
	@Bean
	@Autowired
	public SparqlSqlInverseMapper inverseMapper(CandidateViewSelectorImpl candidateViewSelector, SqlTranslator sqlTranslator) {
		SparqlSqlInverseMapper result = new SparqlSqlInverseMapperImpl(candidateViewSelector, sqlTranslator);
		
		return result;
	}
	
	@Bean
	@Autowired
	public EntityInverseMapper getTableClassMap(SessionFactory sessionFactory, SparqlSqlInverseMapper inverseMapper) {
		EntityInverseMapperImplHibernate result = EntityInverseMapperImplHibernate.create(inverseMapper, sessionFactory);
		return result;
	}
	

	
	
	
	// TODO Replace this ugly unwrapping by creating the Sparqlify Query Execution
	// in spring bean style
	
	public static SparqlSqlOpRewriterImpl unwrapOpRewriter(QueryExecutionFactory qef) {
		QueryExecutionFactorySparqlifyDs q = qef.unwrap(QueryExecutionFactorySparqlifyDs.class);
		SparqlSqlStringRewriterImpl strRewriter = (SparqlSqlStringRewriterImpl)q.getRewriter();
		SparqlSqlOpRewriterImpl result = (SparqlSqlOpRewriterImpl)strRewriter.getSparqlSqlOpRewriter();

		return result;
	}
	
	public static CandidateViewSelectorImpl unwrapCandidateViewSelector(SparqlSqlOpRewriterImpl opRewriter) {
		CandidateViewSelectorImpl result = (CandidateViewSelectorImpl) opRewriter.getCandidateViewSelector();
		return result;
	}
	
	public static SqlTranslator unwrapSqlTransformer(SparqlSqlOpRewriterImpl opRewriter) {

		OpMappingRewriterImpl opMappingRewriter = (OpMappingRewriterImpl)opRewriter.getOpMappingRewriter();
		MappingOpsImpl mappingOps = (MappingOpsImpl)opMappingRewriter.getMappingOps();
		SqlTranslator result = mappingOps.getSqlTranslator();

		return result;
	}

}


