package org.aksw.sparqlify.core.sparql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.aksw.commons.sparql.api.core.QueryExecutionAdapter;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Agg;
import org.aksw.sparqlify.algebra.sql.exprs2.S_AggCount;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Constant;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;
import org.aksw.sparqlify.core.cast.SqlValue;
import org.aksw.sparqlify.core.domain.input.SparqlSqlOpRewrite;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriter;
import org.aksw.sparqlify.core.interfaces.SqlOpSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.expr.NodeValue;


class WatchDog implements Runnable {

	
	private static final Logger logger = LoggerFactory.getLogger(WatchDog.class);
	
	private Statement stmt;
	private long timeOutInMillis;
	
	private long remaining;
	private boolean isCancelled = false;
	
	public WatchDog(Statement stmt, long timeOutInMillis)
	{
		this.stmt = stmt;
		this.timeOutInMillis = timeOutInMillis;
	}
	
	@Override
	public synchronized void run() {
		long start = System.currentTimeMillis();

		do {
			long end = System.currentTimeMillis();
			int elapsed = (int)(end - start);
			remaining = this.timeOutInMillis - elapsed;

			try {
				synchronized(this) {
					this.wait(remaining);
				}
			} catch(Exception e) {
				
			}
			
		} while (!isCancelled && (remaining > 0));;	
		
		
		if(!isCancelled) {
			try {
				logger.error("Cancelling statement due to timeout");
				stmt.cancel();
				stmt.close();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}
	
	public synchronized void cancel() {
		this.isCancelled = true;
		this.notifyAll();
	}
	
}

public class QueryExecutionSparqlifyExplain
	extends QueryExecutionAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(QueryExecutionSparqlifyExplain.class);
	
	private Query query;
	private SparqlSqlOpRewriter sparqlSqlOpRewriter;
	private SqlOpSerializer sqlOpSerializer;

	private DataSource dataSource;
	
	// TODO Make the time out configurable
	private int queryTimeOutInSeconds = 3;
	
	public QueryExecutionSparqlifyExplain(Query query, SparqlSqlOpRewriter sparqlSqlOpRewriter, SqlOpSerializer sqlOpSerializer, DataSource dataSource) {
		
		this.query = query;
		
		this.sparqlSqlOpRewriter = sparqlSqlOpRewriter;
		this.sqlOpSerializer = sqlOpSerializer;
		this.dataSource = dataSource;
	}


	@Override
	public ResultSet execSelect() {
		
		Var idVar = Var.alloc("id");
		Var executionTimeVar = Var.alloc("executionTime");
		Var timeOutVar = Var.alloc("timeOut");
		Var resultSetSizeVar = Var.alloc("resultSetSize");
		Var isErrorVar = Var.alloc("isError");
		Var errorMsgVar = Var.alloc("errorMsg");
		Var queryStringVar = Var.alloc("queryString");
		
		List<String> resultVars = Arrays.asList(idVar.getVarName(), executionTimeVar.getVarName(), timeOutVar.getVarName(), resultSetSizeVar.getVarName(), isErrorVar.getVarName(), errorMsgVar.getVarName(), queryStringVar.getVarName());
		
		List<Binding> resultBindings = new ArrayList<Binding>(); 
		
		
		SparqlSqlOpRewrite ssoRewrite = sparqlSqlOpRewriter.rewrite(query);
		SqlOp sqlOp = ssoRewrite.getSqlOp();
		
		int id = 0;
		
		
		if(sqlOp instanceof SqlOpSelectBlock) {
			SqlOpSelectBlock b = (SqlOpSelectBlock)sqlOp;
			SqlOp subOp = b.getSubOp(); 
			if(subOp instanceof SqlOpUnionN) {
				SqlOpUnionN u = (SqlOpUnionN)subOp;

				for(SqlOp member : u.getSubOps()) {
					SqlOpSelectBlock m = (SqlOpSelectBlock)member;
					
					// Turn the member into a SELECT COUNT(*) query
					SqlOpSelectBlock block = SqlOpSelectBlock.create(m.getSubOp());
					//block.getProjection().put("cnt", new S_Agg(new S_AggCount()));
					
					block.getProjection().put("dummy", new S_Constant(SqlValue.TRUE));
					
					block.getConditions().addAll(m.getConditions());
					block.setLimit(100l);
					block.setAliasName("abc");
					
					SqlOpSelectBlock wrapper = SqlOpSelectBlock.create(block);
					wrapper.getProjection().put("cnt", new S_Agg(new S_AggCount()));
					
					++id;
					
					String sqlQueryString = sqlOpSerializer.serialize(wrapper);
					logger.info("Query String:\n" + sqlQueryString);
					
					
					// Send the query to the database and measure how long it takes
					
					long start = System.currentTimeMillis();
					
					long queryTimeOutInMillis = queryTimeOutInSeconds * 1000;
					
					// If we hit a time out, the elapsed time is max
					//long elapsedTimeInMillis = queryTimeOutInMillis;
					boolean timeOut = true;
					
					Node resultSetSizeNode = null;
					Node errorMsgNode = null;
					boolean isError = false;
					
					Connection conn = null;
					java.sql.ResultSet sqlRs = null;
					long elapsedTimeInMillis = 0;
					try {
						try {
							conn = dataSource.getConnection();
							conn.setAutoCommit(false);
							Statement stmt = QueryExecutionSelect.createStatement(conn);
							
							Thread thread = null;
							WatchDog watchDog = null;
							try {
								stmt.setQueryTimeout(queryTimeOutInSeconds);
							} catch(Exception e) {
								logger.warn("Query time out not natively supported - falling back to custom solution");
								
								watchDog = new WatchDog(stmt, queryTimeOutInMillis);
								
								thread = new Thread(watchDog);
								thread.start();
							}
						

							sqlRs = stmt.executeQuery(sqlQueryString);
							
							if(thread != null) {
								timeOut = false;
								watchDog.cancel();
							}

						} finally {
							long end = System.currentTimeMillis();
							elapsedTimeInMillis = end - start;							
						}
						
						sqlRs.next();
						long resultSetSize = sqlRs.getLong("cnt");
						resultSetSizeNode = NodeValue.makeInteger(resultSetSize).asNode();
						
						
					} catch (SQLTimeoutException e) {
						timeOut = true;
					} catch (Exception e) {
						String errorMsg = e.getMessage();
						errorMsgNode = NodeValue.makeString(errorMsg).asNode();
						isError = true;
						
						if(elapsedTimeInMillis >= queryTimeOutInSeconds) {
							timeOut = true;
						}
						
					} finally {
						
						if(sqlRs != null) {
							try {
								sqlRs.close();
							} catch (SQLException e) {
								throw new RuntimeException(e);
							}
						}
						
						if(conn != null) {
							try {
								conn.close();
							} catch (SQLException e) {
								throw new RuntimeException(e);
							}
						}
					}
					
					BindingHashMap binding = new BindingHashMap();
					binding.add(idVar, NodeValue.makeInteger(id).asNode());
					binding.add(executionTimeVar, NodeValue.makeInteger(elapsedTimeInMillis).asNode());
					binding.add(timeOutVar, NodeValue.makeBoolean(timeOut).asNode());
					binding.add(resultSetSizeVar, resultSetSizeNode);
					binding.add(isErrorVar, NodeValue.makeBoolean(isError).asNode());
					binding.add(errorMsgVar, errorMsgNode);
					binding.add(queryStringVar, NodeValue.makeString(sqlQueryString).asNode());
					
					resultBindings.add(binding);					
				}					
			}
		}
		
		QueryIterator queryIterator = new QueryIterPlainWrapper(resultBindings.iterator());
		ResultSet result = ResultSetFactory.create(queryIterator, resultVars);
		return result;
	}


}
