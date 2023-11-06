package org.aksw.service_framework.core;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.aksw.commons.sql.codec.api.SqlCodec;
import org.aksw.commons.sql.codec.util.SqlCodecUtils;
import org.aksw.commons.util.slf4j.LoggerCount;
import org.aksw.jenax.dataaccess.sparql.factory.execution.query.QueryExecutionFactory;
import org.aksw.service_framework.jpa.core.ServiceProvider;
import org.aksw.service_framework.jpa.core.ServiceProviderJpaRdbRdf;
import org.aksw.service_framework.utils.LogUtils;
import org.aksw.sparqlify.admin.model.JdbcDataSource;
import org.aksw.sparqlify.admin.model.LogMessage;
import org.aksw.sparqlify.admin.model.Rdb2RdfConfig;
import org.aksw.sparqlify.admin.model.Rdb2RdfExecution;
import org.aksw.sparqlify.admin.web.common.ContextStateFlags;
import org.aksw.sparqlify.admin.web.common.LoggerMem;
import org.aksw.sparqlify.admin.web.common.ServiceProviderRdb2Rdf;
import org.aksw.sparqlify.backend.postgres.DatatypeToStringPostgres;
import org.aksw.sparqlify.config.syntax.Config;
import org.aksw.sparqlify.core.algorithms.DatatypeToString;
import org.aksw.sparqlify.util.SparqlifyCoreInit;
import org.aksw.sparqlify.util.SparqlifyUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.jena.query.QueryExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ServiceLauncherRdb2Rdf
    implements ServiceLauncher<Rdb2RdfConfig, Rdb2RdfExecution, SparqlService>
{
    private static final Logger logger = LoggerFactory.getLogger(ServiceLauncherRdb2Rdf.class);


    @Override
    //public ServiceExecution<QueryExecutionFactory> launch(EntityManagerFactory emf, Rdb2RdfConfig serviceConfig, Rdb2RdfExecution context, boolean isRestart) {
    public ServiceProvider<SparqlService> launch(EntityManagerFactory emf, Rdb2RdfConfig serviceConfig, Rdb2RdfExecution context, boolean isRestart) {

//		String serviceName = serviceConfig.getContextPath();
//		ServiceExecution<?> serviceExecution = nameToExecution.get(serviceName);
//		if(serviceExecution != null) {
//			throw new RuntimeException("A service with the name " + serviceName + " is already executing");
//		}
        String serviceName = "foobar";

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Object configId = emf.getPersistenceUnitUtil().getIdentifier(serviceConfig);
        Object executionContextId = emf.getPersistenceUnitUtil().getIdentifier(context);


        serviceConfig = em.find(Rdb2RdfConfig.class, configId);
        context = em.find(Rdb2RdfExecution.class, executionContextId);

//		em.merge(context);
//		em.merge(serviceConfig);

        //Rdb2RdfExecution serviceState = context.getEntity();

        //serviceState.setName(serviceName);
        context.setStatus(ContextStateFlags.STARTING);
        context.getLogMessages().clear();
        context.getLogMessages().add(new LogMessage("info", "Starting service " + serviceName));
        context.setConfig(serviceConfig);

        em.getTransaction().commit();
        em.getTransaction().begin();
        //context.commit();

        //context.openSession();


        ServiceProvider<SparqlService> result = null;

        try {
            //

            JdbcDataSource dsConfig = serviceConfig.getJdbcDataSource();

            //LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
            //LoggerFactory.getLogger(this.getClass()).


            HikariConfig c = new HikariConfig();
            c.setUsername(dsConfig.getUsername());
            c.setPassword(dsConfig.getPassword());
            c.setJdbcUrl(dsConfig.getJdbcUrl());

            HikariDataSource dataSource = new HikariDataSource(c);


            LoggerMem loggerMem = new LoggerMem(logger);
            LoggerCount loggerCount = new LoggerCount(loggerMem);

            String smlConfigStr = serviceConfig.getTextResource().getData();

            Config smlConfig = SparqlifyUtils.parseSmlConfig(smlConfigStr, loggerCount);

            List<LogMessage> lm = Lists.transform(loggerMem.getLogEvents(), LogUtils.convertLog);



            if(loggerCount.getErrorCount() != 0) {
                context.getLogMessages().addAll(lm);
                throw new RuntimeException("Errors encountered during parsing of the mapping");
            }

            Integer maxResultSetRows = serviceConfig.getMaxResultSetRows();
            Integer maxExecutionTimeInSeconds = serviceConfig.getMaxExecutionTimeInSeconds();

            if(maxResultSetRows == null || maxResultSetRows <= 0) {
                maxResultSetRows = 1000;
            }

            if(maxExecutionTimeInSeconds == null || maxExecutionTimeInSeconds <= 0) {
                maxExecutionTimeInSeconds = 30;
            }

            // SqlEscaper sqlEscaper = new SqlEscaperDoubleQuote();
            SqlCodec sqlEscaper = SqlCodecUtils.createSqlCodecDefault();
            DatatypeToString typeSerializer = new DatatypeToStringPostgres();
            QueryExecutionFactory qef = SparqlifyUtils.createDefaultSparqlifyEngine(dataSource, smlConfig, typeSerializer, sqlEscaper, maxResultSetRows.longValue(), maxExecutionTimeInSeconds,
                    SparqlifyCoreInit.loadSqlFunctionDefinitions("functions.xml"));

            // A Test Query
            QueryExecution qe = qef.createQueryExecution("Prefix ex: <http://example.org/> Ask { ?s ex:b ex:c }");
            qe.execAsk();

            SparqlService sparqlService = new SparqlServiceImpl<Config>(smlConfig, qef);

            result = new ServiceProviderRdb2Rdf(serviceName, dataSource, () -> dataSource.close(), sparqlService);

            result = new ServiceProviderJpaRdbRdf<SparqlService>(result, emf, context);

            //nameToExecution.put(serviceName, sparqlServiceExecution);
            context.setStatus(ContextStateFlags.RUNNING);
            context.getLogMessages().add(new LogMessage("info", "Service successfully started."));

        } catch(Exception e) {
            context.setStatus(ContextStateFlags.STOPPED);
            context.getLogMessages().add(new LogMessage("error", ExceptionUtils.getFullStackTrace(e)));
            context.getLogMessages().add(new LogMessage("info", "Service failed to start."));
        }
        finally {
            em.getTransaction().commit();
            em.close();
            //context.commit();
        }

        return result;
    }
}
