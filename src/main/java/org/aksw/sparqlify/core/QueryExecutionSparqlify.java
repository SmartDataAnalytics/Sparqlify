package org.aksw.sparqlify.core;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import mapping.RdfViewDatabase;

import org.aksw.commons.sparql.api.core.QueryExecutionBaseSelect;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.core.QueryExecutionStreaming;
import org.aksw.commons.sparql.api.core.QueryExecutionTimeoutHelper;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNode;
import org.aksw.sparqlify.algebra.sql.nodes.SqlNodeEmpty;
import org.aksw.sparqlify.compile.sparql.SqlGenerator;
import org.aksw.sparqlify.views.transform.EmptyRewriteException;
import org.aksw.sparqlify.views.transform.ViewRewriter;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterNullIterator;
import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.util.FileManager;

// TODO Replace with class from AKSW commons
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

class QueryExecutionTimeout
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



class QueryExecutionSelect
	extends QueryExecutionAdapter //Timeout//Decorator // TODO Replace with QueryExecutionAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(QueryExecutionSelect.class);
	
	private RdfViewSystem system;
	private Connection conn;
	private Query query;

	private Statement stmt;
	
	private ResultSet rs;

	
	public QueryExecutionSelect(RdfViewSystem system, Connection conn, Query query) {
		this.system = system;
		this.conn = conn;
		this.query = query;
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
	private static Statement createStatement(Connection conn)
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
		return com.hp.hpl.jena.query.ResultSetFactory.create(new QueryIterNullIterator(null), vars);
	}
	
	public ResultSet _execSelect() throws SQLException {
		/*
		if (!query.isSelectType()) {
			throw new RuntimeException("SELECT query expected. Got: ["
					+ query.toString() + "]");
		}
		*/
		
		Op view = system.getApplicableViews(query);


		// Get the projection order right in the result set
		List<Var> projectionOrder = null;
		if(query.isSelectType() && query.isQueryResultStar()) {
			projectionOrder = query.getProjectVars();
		} else {
			projectionOrder = query.getProjectVars();
		}

		
		//
		ViewRewriter sqlRewriter = new ViewRewriter();
		
		SqlNode sqlNode;
		
		try {
			sqlNode = sqlRewriter.rewriteMM(view);
		} catch(EmptyRewriteException e) {
			return createEmptyResultSet();
		}

		if(logger.isInfoEnabled()) {
			logger.info("Final sparql var mapping:");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			write(ps, sqlNode.getSparqlVarToExprs());
			
			logger.debug(baos.toString());
		}

		
		if(sqlNode instanceof SqlNodeEmpty) {
			return createEmptyResultSet();
		}
		
		
		SqlGenerator sqlGenerator = new SqlGenerator();
		String sqlQuery = sqlGenerator.generateMM(sqlNode);
		logger.info(sqlQuery);

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
			//stmt = conn.createStatement();
			stmt = createStatement(conn);
		}

		//timeoutHelper.startExecutionTimer();

		rs = ResultSetFactory.create(stmt, sqlQuery,
				sqlNode.getSparqlVarToExprs(), projectionOrder);

		//timeoutHelper.stopExecutionTimer();

		return rs;
	}
	
	
	@Override
	public void abort() {
		synchronized(this) {
			if(stmt != null) {
				try {
					stmt.cancel();
					close();
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	@Override
	public void close() {
		synchronized(this) {
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

public class QueryExecutionSparqlify
	extends QueryExecutionBaseSelect
{
	private RdfViewSystem system;
	private Connection conn;
	private boolean closeConnWhenDone;
	//private Query query;
	
	// subFactory is needed for DESCRIBE queries right now
	public QueryExecutionSparqlify(RdfViewSystem system, Connection conn, boolean closeConnWhenDone, Query query, QueryExecutionFactory<QueryExecutionStreaming> subFactory) {
		super(query, subFactory);
		this.system = system;
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
		return new QueryExecutionSelect(system, conn, dummy);
	}
	
	
	@Override
	protected ResultSet executeCoreSelect(Query query) {
		timeoutHelper.startExecutionTimer();
		
		try {
			ResultSet result = super.executeCoreSelect(query);
			
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
		if(this.closeConnWhenDone) {
			try {
				conn.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}	

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
