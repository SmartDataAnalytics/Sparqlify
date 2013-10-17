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
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Mappings;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
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
	@DependsOn("transactionManager")
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
	@DependsOn("transactionManager")
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
	@DependsOn("transactionManager")
	public Object getTableClassMap(JpaTransactionManager txManager) {

		EntityManagerFactory emf = txManager.getEntityManagerFactory();
		SessionFactory sessionFactory = ((HibernateEntityManagerFactory)emf).getSessionFactory();
		
		
//		//System.out.println("emf class: " + emf.getClass().getName());
		Metamodel metamodel = emf.getMetamodel();
		Set<EntityType<?>> entities = metamodel.getEntities();
		for(EntityType<?> entity : entities) {
			Class<?> entityClass = entity.getJavaType();

			ClassMetadata classMetadata = sessionFactory.getClassMetadata(entityClass.getName());
			if (classMetadata == null) {
				throw new RuntimeException("Could not retrieve metadata for an entity: " + entity.getName());
			}
			if (classMetadata instanceof AbstractEntityPersister) {
			     AbstractEntityPersister persister = (AbstractEntityPersister)classMetadata;
			     String tableName = persister.getTableName();
			     System.out.println("Table name: " + tableName);

			     //persister.
//			     Mappings x;
//			     x.getClass("foobar").getProperty("bar").get
			     //x.getClass(entityName)
			     //persister.getProperty
			     //persister.getPropertyColumnNames(propertyName)
			     
			     //String[] columnNames = persister.getKeyColumnNames();
			}
		}

//			System.out.println("entity info");
//			System.out.println(entity.getName());
//			System.out.println(entity.getJavaType().getName());
//			System.out.println(entity);
//			System.out.println("---");
//			//org.hibernate.ejb.metamodel.EntityTypeImpl<X>
//		}
//		
//		for(ManagedType<?> type : metamodel.getManagedTypes()) {
//			type.
//		}
		
		
		return null;
//		EntityManagerFactory emf;
//		emf.getMetamodel().managedType(null);
	}
}


