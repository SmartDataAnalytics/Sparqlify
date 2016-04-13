package org.aksw.sparqlify.admin.web.main;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.service_framework.core.ServiceLauncherRdb2Rdf;
import org.aksw.service_framework.core.SparqlService;
import org.aksw.service_framework.jpa.core.ServiceRepositoryJpaImpl;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.admin.model.Rdb2RdfExecution;
import org.aksw.sparqlify.admin.web.api.ServiceEventListenerRegister;
import org.aksw.sparqlify.admin.web.api.ServiceManager;
import org.aksw.sparqlify.admin.web.api.ServiceManagerImpl;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.algorithms.CandidateViewSelectorImpl;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriterImpl;
import org.aksw.sparqlify.core.interfaces.SqlTranslator;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMapper;
import org.aksw.sparqlify.inverse.SparqlSqlInverseMapperImpl;
import org.aksw.sparqlify.jpa.EntityInverseMapper;
import org.aksw.sparqlify.jpa.EntityInverseMapperImplHibernate;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.hibernate.SessionFactory;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.jena.shared.PrefixMapping;

@Configuration
@ComponentScan("org.aksw.sparqlify.admin.web")
@EnableTransactionManagement
@PropertySource("classpath:config/jdbc/jdbc.properties")
public class AppConfig
{
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

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

    /**
     * When starting the server from the command line,
     * this attribute can be set to override any other means of creating a data source
     */
    public static DataSource cliDataSource = null;



    //@Bean
//	public DataSource dataSource2() {
//		JndiObjectFactoryBean jndiObjectFactory = new JndiObjectFactoryBean();
//
//	}

    @Bean
    public DataSource dataSource() throws IllegalArgumentException, ClassNotFoundException {

        // TODO Somehow allow loading drivers dynamically
        Class.forName("org.postgresql.Driver");

        DataSource result = null;

        try {
            String jndiName = "java:comp/env/jdbc/sparqlifyDs";
            Context ctx = new InitialContext();
            result = (DataSource)ctx.lookup(jndiName);
        } catch (NamingException e) {
            logger.info("Exception on retrieving initial JNDI context - trying a different method");
        }

        if(result != null) {
            return result;
        }


//		JndiObjectFactoryBean jndiFactory = new JndiObjectFactoryBean();
//		jndiFactory.setResourceRef(true);
//		jndiFactory.setJndiName(jndiName);
//		//jndiFactory.setJndiName("jdbc/sparqlifyDs");
//		jndiFactory.setLookupOnStartup(true);
//		jndiFactory.afterPropertiesSet();
//
//
//		result = (DataSource)jndiFactory.getObject();



//		URL location = AppConfig.class.getProtectionDomain().getCodeSource().getLocation();
//        System.out.println(location.getFile());

        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName(env.getRequiredProperty(JDBC_DRIVER));
        dataSource.setUrl(env.getRequiredProperty(JDBC_URL));
        dataSource.setUsername(env.getRequiredProperty(JDBC_USERNAME));
        dataSource.setPassword(env.getRequiredProperty(JDBC_PASSWORD));

        return dataSource;
    }

//	@Bean
//
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
//
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

//	@Bean
//	public UrlBasedViewResolver setupViewResolver() {
//		UrlBasedViewResolver resolver = new UrlBasedViewResolver();
//		resolver.setPrefix("/WEB-INF/pages/");
//		resolver.setSuffix(".jsp");
//		resolver.setViewClass(JstlView.class);
//		return resolver;
//	}

    @Bean
    public HibernateExceptionTranslator hibernateExceptionTranslator() {
        return new HibernateExceptionTranslator();
    }

    @Bean
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
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager txManager = new JpaTransactionManager();
        JpaDialect jpaDialect = new HibernateJpaDialect();
        txManager.setEntityManagerFactory(entityManagerFactory);
        txManager.setJpaDialect(jpaDialect);
        return txManager;
    }



    /**
     *
     * Note: We must ensure that the schema exists prior to mapping it
     *
     * @return
     * @throws Exception
     */
    @Bean
    @DependsOn("entityManagerFactory")
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
    public SparqlSqlInverseMapper sparqlSqlInverseMapper(CandidateViewSelectorImpl candidateViewSelector, SqlTranslator sqlTranslator) {
        SparqlSqlInverseMapper result = new SparqlSqlInverseMapperImpl(candidateViewSelector, sqlTranslator);

        return result;
    }

    @Bean
    public EntityInverseMapper entityInverseMapper(SessionFactory sessionFactory, SparqlSqlInverseMapper inverseMapper) {
        EntityInverseMapperImplHibernate result = EntityInverseMapperImplHibernate.create(inverseMapper, sessionFactory);
        return result;
    }


    @Bean
    public ServiceRepositoryJpaImpl<Rdb2RdfConfig, Rdb2RdfExecution, SparqlService> sparqlServiceRepo(JpaTransactionManager txManager) {

        EntityManagerFactory emf = txManager.getEntityManagerFactory();

        ServiceRepositoryJpaImpl<Rdb2RdfConfig, Rdb2RdfExecution, SparqlService> serviceRepo =
            ServiceRepositoryJpaImpl.create(
                    emf,
                    Rdb2RdfConfig.class,
                    Rdb2RdfExecution.class,
                    new ServiceLauncherRdb2Rdf()
                    );

        return serviceRepo;
    }


    @Bean
    public Map<String, SparqlService> sparqlServiceMap(ServiceRepositoryJpaImpl<Rdb2RdfConfig, Rdb2RdfExecution, SparqlService> serviceRepo) {
        Map<String, SparqlService> result = Collections.synchronizedMap(new HashMap<String, SparqlService>());

        ServiceEventListenerRegister listener = new ServiceEventListenerRegister(result);
        serviceRepo.getServiceEventListeners().add(listener);

        serviceRepo.startAll();

        return result;
    }


//	@Bean
//	public Map<String, QueryExecutionFactory> sparqlServiceMap(@Resource(name="tmpSparqlServiceMap") Map<String, SparqlService> tmpSparqlServiceMap) {
//	    Map<String, QueryExecutionFactory> result = Maps.transformValues(tmpSparqlServiceMap, new Function<SparqlService, QueryExecutionFactory>() {
//            @Override
//            public QueryExecutionFactory apply(SparqlService sparqlService) {
//                return sparqlService.getSparqlService();
//            }
//	    });
//
//	    return result;
//	}
//
//    @Bean
//    public Map<String, PrefixMapping> sparqlNamespaceMap(Map<String, SparqlService> sparqlServiceMap) {
//        Map<String, PrefixMapping> result = Maps.transformValues(sparqlServiceMap, new Function<SparqlService, PrefixMapping>() {
//            @Override
//            public PrefixMapping apply(SparqlService sparqlService) {
//                PrefixMapping tmp = null;
//
//                Object c = sparqlService.getConfig();
//                if(c instanceof Config) {
//                    Config config = (Config)c;
//                    tmp = config.getPrefixMapping();
//                }
//
//                return tmp;
//            }
//        });
//
//        return result;
//    }
//

    @Bean
    public ServiceManager sparqlServiceManager(ServiceRepositoryJpaImpl<Rdb2RdfConfig, Rdb2RdfExecution, SparqlService> serviceRepo, EntityInverseMapper entityInverseMapper) {
        ServiceManager serviceManager = ServiceManagerImpl.create(serviceRepo, entityInverseMapper);

        return serviceManager;
    }


    @Bean
    public SessionFactory sessionFactory(JpaTransactionManager txManager) {
        EntityManagerFactory emf = txManager.getEntityManagerFactory();
        SessionFactory result = ((HibernateEntityManagerFactory)emf).getSessionFactory();
        return result;
    }


    // TODO Possibly replace this ugly unwrapping by creating the Sparqlify Query Execution
    // in spring bean style

    @Bean
    public SparqlSqlOpRewriterImpl sparqlSqlOpRewriter(QueryExecutionFactory qef) {
        SparqlSqlOpRewriterImpl result = SparqlifyUtils.unwrapOpRewriter(qef);
        return result;
    }

    @Bean
    public SqlTranslator sqlTranslator(SparqlSqlOpRewriterImpl opRewriter) {
        SqlTranslator result = SparqlifyUtils.unwrapSqlTransformer(opRewriter);
        return result;
    }

    @Bean
    public CandidateViewSelectorImpl candidateViewSelector(SparqlSqlOpRewriterImpl opRewriter) {
        CandidateViewSelectorImpl result = SparqlifyUtils.unwrapCandidateViewSelector(opRewriter);
        return result;
    }


}


