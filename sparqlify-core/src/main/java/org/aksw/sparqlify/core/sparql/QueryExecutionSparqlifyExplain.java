package org.aksw.sparqlify.core.sparql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.aksw.jena_sparql_api.utils.query_execution.QueryExecutionAdapter;
import org.aksw.sparqlify.algebra.sql.exprs2.S_Agg;
import org.aksw.sparqlify.algebra.sql.exprs2.S_AggCount;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOp;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpSelectBlock;
import org.aksw.sparqlify.algebra.sql.nodes.SqlOpUnionN;
import org.aksw.sparqlify.core.domain.input.SparqlSqlOpRewrite;
import org.aksw.sparqlify.core.interfaces.SparqlSqlOpRewriter;
import org.aksw.sparqlify.core.sql.algebra.serialization.SqlOpSerializer;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
            long elapsed = end - start;
            remaining = this.timeOutInMillis - elapsed;

            try {
                synchronized(this) {
                    if(remaining > 0) {
                        this.wait(remaining);
                    }
                }
            } catch(Exception e) {

            }

        } while (!isCancelled && (remaining > 0));


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

        List<SqlOp> sqlOps = null;

        if(sqlOp instanceof SqlOpSelectBlock) {
            SqlOpSelectBlock b = (SqlOpSelectBlock)sqlOp;
            SqlOp subOp = b.getSubOp();
            if(subOp instanceof SqlOpUnionN) {
                SqlOpUnionN u = (SqlOpUnionN)subOp;

                sqlOps = u.getSubOps();
            }
        }

        if(sqlOps == null) {
            sqlOps = Arrays.asList(sqlOp);
        }

        for(SqlOp member : sqlOps) {
            SqlOpSelectBlock m = (SqlOpSelectBlock)member;

            // Turn the member into a SELECT COUNT(*) query
            SqlOpSelectBlock block = SqlOpSelectBlock.create(m.getSubOp());
            //block.getProjection().put("cnt", new S_Agg(new S_AggCount()));

            //block.getProjection().put("v", new S_Constant(SqlValue.TRUE));
            //block.set

            block.getConditions().addAll(m.getConditions());
            block.setLimit(100l);
            block.setAliasName("a");

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
                Thread thread = null;
                WatchDog watchDog = null;
                try {
                    conn = dataSource.getConnection();
                    conn.setAutoCommit(false);
                    Statement stmt = QueryExecutionSelect.createStatement(conn);

                    try {
                        stmt.setQueryTimeout(queryTimeOutInSeconds);
                    } catch(Exception e) {
                        logger.warn("Query time out not natively supported - falling back to custom solution");

                        watchDog = new WatchDog(stmt, queryTimeOutInMillis);

                        thread = new Thread(watchDog);
                        thread.start();
                    }


                    sqlRs = stmt.executeQuery(sqlQueryString);

                } finally {
                    if(thread != null) {
                        timeOut = false;
                        watchDog.cancel();
                    }

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

                if(elapsedTimeInMillis >= queryTimeOutInMillis) {
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
            if(errorMsgNode != null) { binding.add(errorMsgVar, errorMsgNode); }
            binding.add(queryStringVar, NodeValue.makeString(sqlQueryString).asNode());

            resultBindings.add(binding);
        }

        QueryIterator queryIterator = new QueryIterPlainWrapper(resultBindings.iterator());
        ResultSet result = ResultSetFactory.create(queryIterator, resultVars);
        return result;
    }


}
