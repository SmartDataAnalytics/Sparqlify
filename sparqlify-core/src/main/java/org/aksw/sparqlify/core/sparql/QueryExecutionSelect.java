package org.aksw.sparqlify.core.sparql;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aksw.jenax.arq.util.execution.QueryExecutionAdapter;
import org.aksw.sparqlify.core.domain.input.SparqlSqlStringRewrite;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.iterator.QueryIterNullIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;

/**
 * A query execution that can only do execSelect
 * on a SparqlSqlRewriter and a Connection
 *
 * @author raven
 *
 */
public class QueryExecutionSelect
    extends QueryExecutionAdapter //Timeout//Decorator // TODO Replace with QueryExecutionAdapter
{
    private static final Logger logger = LoggerFactory.getLogger(QueryExecutionSelect.class);

    private SparqlSqlStringRewriter rewriter;
    private final Connection conn;
    private boolean closeConnWhenDone;

    private Query query;

    private Statement stmt;

    private ResultSet rs;


    public QueryExecutionSelect(SparqlSqlStringRewriter rewriter, Connection conn, Query query, boolean closeConnWhenDone) {
        this.rewriter = rewriter;
        this.conn = conn;
        this.query = query;
        this.closeConnWhenDone = closeConnWhenDone;
    }

    public ResultSet execSelect() {
        try {
            return _execSelect();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void write(PrintStream out, Map<?, ?> map)
    {
        for(Entry<?, ?> entry : map.entrySet()) {
            out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    public static String spaces(int n) {
        String result = "";
        for(int i = 0; i < n; ++i) {
            result += " ";
        }
        return result;
    }

    public static <K, V >void write(PrintStream out, Multimap<K, V> map)
    {
        for(Entry<K, Collection<V>> entry : map.asMap().entrySet()) {

            boolean keyPrint = false;
            String key = entry.getKey() == null ? "(null)" : entry.getKey().toString();

            String sp = spaces(key.length());


            for(V value : entry.getValue()) {
                if(!keyPrint) {
                    out.println(key + ": " + value);
                    keyPrint = true;
                } else {
                    out.println(sp + "  " + value);
                }
            }

            if(!keyPrint) {
                out.println(key + ": (empty)");
            }
        }
    }


    // For streaming large result sets with postgres, it seems to only option is to set
    // the fetch size
    public static Statement createStatement(Connection conn)
            throws SQLException
    {
        Statement stmt = conn.createStatement();
        stmt.setFetchSize(50000);

        return stmt;
    }

    private ResultSet createEmptyResultSet() {
        List<String> vars = new ArrayList<String>();
        for(Var v : query.getProjectVars()) {
            vars.add(v.getName());
        }
        return org.apache.jena.query.ResultSetFactory.create(new QueryIterNullIterator(null), vars);
    }

    public ResultSet _execSelect() throws SQLException {
        /*
        if (!query.isSelectType()) {
            throw new RuntimeException("SELECT query expected. Got: ["
                    + query.toString() + "]");
        }
        */

        SparqlSqlStringRewrite rewrite = rewriter.rewrite(query);

        /*
        if(logger.isInfoEnabled()) {
            logger.info("Final sparql var mapping:");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            write(ps, sqlNode.getSparqlVarToExprs());

            logger.info(baos.toString());
        }
        */

        /*
        boolean enableCostChecking = false;
        if(enableCostChecking) {

            double cost = RdfViewDatabase.getCostPostgres(conn, sqlQuery);

            if (cost > 4000) {
                System.out.println("TODO abort due to high query cost: " + cost);
                //System.out.println("Aborted due to high query cost: " + cost);
                //return null;
            }

            logger.debug("Query cost ok (" + cost + ")");
            } else {
                logger.info("Cost estimates disabled.");
            }

        synchronized(this) {
            // Statements may be cancelled asynchronously
             *

        } */
        stmt = createStatement(conn);
        logger.debug("My connection: " + conn + " Query is " + query);

        //timeoutHelper.startExecutionTimer();

        String queryString = rewrite.isEmptyResult() ? null : rewrite.getSqlQueryString();


        rs = ResultSetFactory.create(
                conn,
                stmt, queryString,
                rewrite.getVarDefinition().getMap(), rewrite.getProjectionOrder());

        //final QueryExecutionSelect self = this;

//		ResultSetClosable result = new ResultSetClosable(rs, new IClosable() {
//
//			@Override
//			public void close() {
//				self.close();
//			}
//		});

        return rs;
    }


    @Override
    public void abort() {
        synchronized(this) {
            if(stmt != null) {
                try {
                    stmt.cancel();
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        close();
    }

    @Override
    public void close() {
        synchronized(this) {
            if(closeConnWhenDone) {
                try {
                    conn.close();
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }

            if(stmt != null) {
                try {
                    stmt.close();
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}