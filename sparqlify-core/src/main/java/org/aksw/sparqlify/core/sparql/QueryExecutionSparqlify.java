package org.aksw.sparqlify.core.sparql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.aksw.jena_sparql_api.core.QueryExecutionBaseSelect;
import org.aksw.jena_sparql_api.core.QueryExecutionTimeoutHelper;
import org.aksw.jenax.arq.connection.core.QueryExecutionFactory;
import org.aksw.sparqlify.core.interfaces.SparqlSqlStringRewriter;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO Replace with class from AKSW commons
/*
class QueryExecutionAdapter
    implements QueryExecution
{
    @Override
    public void setFileManager(FileManager fm) {
        throw new NotImplementedException();
    }

    @Override
    public void setInitialBinding(QuerySolution binding) {
        throw new NotImplementedException();
    }

    @Override
    public Dataset getDataset() {
        throw new NotImplementedException();
    }

    @Override
    public Context getContext() {
        throw new NotImplementedException();
    }

    @Override
    public ResultSet execSelect() {
        throw new NotImplementedException();
    }

    @Override
    public Model execConstruct() {
        throw new NotImplementedException();
    }

    @Override
    public Model execConstruct(Model model) {
        throw new NotImplementedException();
    }

    @Override
    public Model execDescribe() {
        throw new NotImplementedException();
    }

    @Override
    public Model execDescribe(Model model) {
        throw new NotImplementedException();
    }

    @Override
    public boolean execAsk() {
        throw new NotImplementedException();
    }

    @Override
    public void abort() {
    }

    @Override
    public void close() {
    }

    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        throw new NotImplementedException();
    }

    @Override
    public void setTimeout(long timeout) {
        throw new NotImplementedException();
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2,
            TimeUnit timeUnit2) {
        throw new NotImplementedException();
    }

    @Override
    public void setTimeout(long timeout1, long timeout2) {
        throw new NotImplementedException();
    }

    @Override
    public Query getQuery() {
        throw new NotImplementedException();
    }

}
*/
/*
public class QueryExecutionTimeout
    extends QueryExecutionAdapter
{
    protected QueryExecutionTimeoutHelper timeoutHelper = new QueryExecutionTimeoutHelper(this);

    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        timeoutHelper.setTimeout(timeout, timeoutUnits);
    }

    @Override
    public void setTimeout(long timeout) {
        timeoutHelper.setTimeout(timeout);
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2,
            TimeUnit timeUnit2) {
        timeoutHelper.setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
    }

    @Override
    public void setTimeout(long timeout1, long timeout2) {
        timeoutHelper.setTimeout(timeout1, timeout2);
    }
}
*/


public class QueryExecutionSparqlify
    extends QueryExecutionBaseSelect
{
    public static Logger logger = LoggerFactory.getLogger(QueryExecutionSparqlify.class);

    private SparqlSqlStringRewriter rewriter;
    private Connection conn;
    private boolean closeConnWhenDone;
    //private Query query;

    // subFactory is needed for DESCRIBE queries right now
    public QueryExecutionSparqlify(SparqlSqlStringRewriter rewriter, Connection conn, boolean closeConnWhenDone, Query query, QueryExecutionFactory subFactory) {
        super(query, subFactory);
        this.rewriter = rewriter;
        this.conn = conn;
        this.closeConnWhenDone = closeConnWhenDone;
        //this.query = query;
    }

    /*
    @Override
    public Object getId() {
        // Transient id means, that after recreation of the object with the same
        // configuration, the id might be different.
        // Caches can be cleared once such object gets destroyed.
        // TODO Think about how to implement this properly
        return "transient-id-" + this.hashCode();
    }*/


    @Override
    protected QueryExecution executeCoreSelectX(Query dummy) {
        // TODO This object hardly closes the connection again....
        QueryExecution result = new QueryExecutionSelect(rewriter, conn, dummy, false);
        return result;
    }


    @Override
    protected ResultSetCloseable executeCoreSelect(Query query) {
        timeoutHelper.startExecutionTimer();

        try {
            ResultSetCloseable result = super.executeCoreSelect(query);

            return result;

        } finally {
            timeoutHelper.stopExecutionTimer();
        }
    }

    protected QueryExecutionTimeoutHelper timeoutHelper = new QueryExecutionTimeoutHelper(this);

    @Override
    public void setTimeout(long timeout, TimeUnit timeoutUnits) {
        timeoutHelper.setTimeout(timeout, timeoutUnits);
    }

    @Override
    public void setTimeout(long timeout) {
        timeoutHelper.setTimeout(timeout);
    }

    @Override
    public void setTimeout(long timeout1, TimeUnit timeUnit1, long timeout2,
            TimeUnit timeUnit2) {
        timeoutHelper.setTimeout(timeout1, timeUnit1, timeout2, timeUnit2);
    }

    @Override
    public void setTimeout(long timeout1, long timeout2) {
        timeoutHelper.setTimeout(timeout1, timeout2);
    }

    @Override
    public void close() {
        // super.close();

        if(this.closeConnWhenDone) {
            try {
                logger.trace("Closed connection: [" + conn + "]");
                conn.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public long getTimeout1() {
        long result = timeoutHelper.getExecutionTime();
        return result;
    }


    @Override
    public long getTimeout2() {
        long result = timeoutHelper.getRetrievalTime();
        return result;
    }
//
    /*
    protected ResultSet executeSelectImpl(Query query) throws SQLException {
        /*
        if (!query.isSelectType()) {
            throw new RuntimeException("SELECT query expected. Got: ["
                    + query.toString() + "]");
        }
        * /

        Op view = system.getApplicableViews(query);

        //
        ViewRewriter sqlRewriter = new ViewRewriter();
        SqlNode sqlNode = sqlRewriter.rewriteMM(view);

        System.out.println("Final sparql var mapping = "
                + sqlNode.getSparqlVarToExprs());

        SqlGenerator sqlGenerator = new SqlGenerator();
        String sqlQuery = sqlGenerator.generateMM(sqlNode);
        System.out.println(sqlQuery);

        double cost = RdfViewDatabase.getCostPostgres(conn, sqlQuery);

        if (cost > 4000) {
            System.out.println("Aborted due to high query cost: " + cost);
            return null;
        }

        System.out.println("Query cost ok (" + cost + ")");

        ResultSet rs = ResultSetFactory.create(conn, sqlQuery,
                sqlNode.getSparqlVarToExprs());

        return rs;
    }

    //@Override
    public void abort() {
    }
    */
}
