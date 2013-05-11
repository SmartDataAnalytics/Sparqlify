package org.aksw.sparqlify.core.interfaces;

import java.util.List;

import org.aksw.commons.util.jdbc.Schema;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.core.algorithms.SparqlSqlStringRewriterImpl;
import org.aksw.sparqlify.core.algorithms.SqlOptimizerImpl;
import org.aksw.sparqlify.core.domain.input.Mapping;
import org.aksw.sparqlify.core.domain.input.SparqlSqlOpRewrite;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;


/**
 * Rewrites a SPARQL query object into an SQL op
 * 
 * @author raven
 *
 */
public class SparqlSqlOpRewriterImpl
	implements SparqlSqlOpRewriter
{
	private static final Logger logger = LoggerFactory.getLogger(SparqlSqlStringRewriterImpl.class);
	
	private CandidateViewSelector<? extends IViewDef> candidateViewSelector;
	private OpMappingRewriter opMappingRewriter;
	private SqlOpSelectBlockCollector sqlOpSelectBlockCollector;
	
	// TODO Replace with an SQL optimizer object ; or more general an SqlOp post transformer
	//private Schema databaseSchema;
	
	private SqlOptimizerImpl sqlOptimizer;
	
	public SparqlSqlOpRewriterImpl(
			CandidateViewSelector<? extends IViewDef> candidateViewSelector,
			OpMappingRewriter opMappingRewriter,
			SqlOpSelectBlockCollector sqlOpSelectBlockCollector,
			Schema databaseSchema)
	{
		this.candidateViewSelector = candidateViewSelector;
		this.opMappingRewriter = opMappingRewriter;
		this.sqlOpSelectBlockCollector = sqlOpSelectBlockCollector;
		
		this.sqlOptimizer = new SqlOptimizerImpl(databaseSchema);
	}
	
	
	@Override
	public SparqlSqlOpRewrite rewrite(Query query) {

		StopWatch sw = new StopWatch();
		
		sw.start();
		logger.info("[" + sw.getTime() + "] Rewrite started.");
		
		// Expand the query
		Op opViewInstance = candidateViewSelector.getApplicableViews(query);		
	
		
		logger.info("[" + sw.getTime() + "] Candidate selection completed");
		
		
		//logger.trace("View Instance Op: " + opViewInstance);
		
	
		// Get the projection order right in the result set
		List<Var> projectionOrder = null;
		if(query.isSelectType() && query.isQueryResultStar()) {
			projectionOrder = query.getProjectVars();
		} else {
			projectionOrder = query.getProjectVars();
		}
	
		Mapping mapping = opMappingRewriter.rewrite(opViewInstance);

		logger.info("[" + sw.getTime() + "] Mapping rewrite completed");

		//logger.info("Variable Definitions:\n" + mapping.getVarDefinition().toPrettyString());
		
		// FIXME Make the collector configurable
		//SqlOp block = SqlOpSelectBlockCollector._makeSelect(mapping.getSqlOp());
		
		SqlOp sqlOp = mapping.getSqlOp();
		
		logger.info("[" + sw.getTime() + "] Sql translation completed");

		if(sqlOpSelectBlockCollector != null) {
			sqlOp = sqlOpSelectBlockCollector.transform(sqlOp);
			
			sqlOptimizer.optimize(sqlOp);
			
			//SqlOptimizerImpl.optimize(sqlOp);

			logger.info("[" + sw.getTime() + "] Sql optimization completed");
		}

		sw.stop();
		logger.info("[" + sw.getTime() + "] Done (excluding serialization)");
	
		/*
		boolean debugPerformance = true;
		if(debugPerformance) {
			if(block instanceof SqlOpSelectBlock) {
				SqlOpSelectBlock b = (SqlOpSelectBlock)block;
				SqlOp subOp = b.getSubOp(); 
				if(subOp instanceof SqlOpUnionN) {
					SqlOpUnionN u = (SqlOpUnionN)subOp;
	
					for(SqlOp member : u.getSubOps()) {
					
						String sqlQueryString = sqlOpSerializer.serialize(member);
						logger.info("Query String:\n" + sqlQueryString);
					}					
				}
			}
		}
		*/		
		
		SparqlSqlOpRewrite result = new SparqlSqlOpRewrite(sqlOp, mapping.isEmpty(), mapping.getVarDefinition(), projectionOrder);
		
		return result;
	}
}

