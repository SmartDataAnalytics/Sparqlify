package org.aksw.sparqlify.admin.web.main;

import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

@Configuration
@ComponentScan("org.aksw.sparqlify.admin.web.api")
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
	private static final String PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN = "entitymanager.packages.to.scan";

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

	@Bean
	public LocalSessionFactoryBean sessionFactory() {
		LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
		sessionFactoryBean.setDataSource(dataSource());
		sessionFactoryBean
				.setPackagesToScan(env
						.getRequiredProperty(PROPERTY_NAME_ENTITYMANAGER_PACKAGES_TO_SCAN));
		sessionFactoryBean.setHibernateProperties(hibProperties());
		return sessionFactoryBean;
	}

	private Properties hibProperties() {
		Properties properties = new Properties();
		properties.put(HIBERNATE_DIALECT, env.getRequiredProperty(HIBERNATE_DIALECT));
		properties.put(HIBERNATE_SHOW_SQL, env.getRequiredProperty(HIBERNATE_SHOW_SQL));
		properties.put(HIBERNATE_HBM2DDL_AUTO, env.getRequiredProperty(HIBERNATE_HBM2DDL_AUTO));
		
		properties.put("hibernate.current_session_context_class", "thread");
		
		return properties;
	}

	@Bean
	public HibernateTransactionManager transactionManager() {

		HibernateTransactionManager transactionManager = new HibernateTransactionManager();
		transactionManager.setSessionFactory(sessionFactory().getObject());

		// Force creation of the schema
		Session session = transactionManager.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		tx.commit();
		
		session.close();
		
		
		return transactionManager;
	}

	@Bean
	public UrlBasedViewResolver setupViewResolver() {
		UrlBasedViewResolver resolver = new UrlBasedViewResolver();
		resolver.setPrefix("/WEB-INF/pages/");
		resolver.setSuffix(".jsp");
		resolver.setViewClass(JstlView.class);
		return resolver;
	}
	
	/**
	 * 
	 * TODO We must ensure that the schema exists prior to mapping it
	 * 
	 * @return
	 * @throws Exception
	 */
	@Bean
	@DependsOn("transactionManager")
	public QueryExecutionFactory managerApiQef()
		throws Exception
	{
		LoggerCount loggerCount = new LoggerCount(logger);
		InputStream in = this.getClass().getResourceAsStream("/sparqlify-web-manager.sml");
		Config config = SparqlifyUtils.readConfig(in, loggerCount);
		
		if(loggerCount.getErrorCount() != 0 || loggerCount.getWarningCount() != 0) {
			throw new RuntimeException("Errors reading mapping encountered");
		}
		
		QueryExecutionFactory result = SparqlifyUtils.createDefaultSparqlifyEngine(dataSource(), config, 1000l, 60);
		return result;
	}
}
