package org.aksw.sparqlify.core.algorithms;

import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.core.domain.input.SparqlSqlOpRewrite;
import org.aksw.sparqlify.core.domain.input.SparqlSqlStringRewrite;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriter;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;

public class SparqlSqlStringRewriterImpl
	implements SparqlSqlStringRewriter
{
	private static final Logger logger = LoggerFactory.getLogger(SparqlSqlStringRewriterImpl.class);
	
	private SparqlSqlOpRewriter sparqlSqlOpRewriter;
	private SqlOpSerializer sqlOpSerializer;
	
	public SparqlSqlStringRewriterImpl(
			SparqlSqlOpRewriter sparqlSqlOpRewriter,
			SqlOpSerializer sqlOpSerializer)
	{
		this.sparqlSqlOpRewriter = sparqlSqlOpRewriter;
		this.sqlOpSerializer = sqlOpSerializer;
	}
	
	
	@Override
	public SparqlSqlStringRewrite rewrite(Query query) {

		SparqlSqlOpRewrite rewrite = sparqlSqlOpRewriter.rewrite(query);
		SqlOp sqlOp = rewrite.getSqlOp();
		
		String sqlQueryString = sqlOpSerializer.serialize(sqlOp);
		SparqlSqlStringRewrite result = new SparqlSqlStringRewrite(sqlQueryString, rewrite.isEmptyResult(), rewrite.getVarDefinition(), rewrite.getProjectionOrder());

		logger.debug("Sql Query:\n" + result.getSqlQueryString());
		
		return result;
	}	
}
