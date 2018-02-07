package org.aksw.sparqlify.core.sparql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aksw.jena_sparql_api.views.RestrictedExpr;
import org.aksw.sparqlify.core.ResultSetSparqlify;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import com.google.common.collect.Multimap;


public class ResultSetFactory {

	/**
	 * 
	 * @param conn
	 * @param sqlQuery An SQL query to be sent to the database. null if empty result set.
	 * @param sparqlVarMap
	 * @param projectionVars The variables that should appear in the result set. If null, sparqlVarMap.keySet() is used.
	 * @return
	 * @throws SQLException
	 */
	public static ResultSetSparqlify create(Connection conn, Statement stmt, String sqlQuery, Multimap<Var, RestrictedExpr> sparqlVarMap, List<Var> projectionVars)
		throws SQLException
	{
		List<String> resultVars = new ArrayList<String>();

		if(projectionVars == null) {
			projectionVars = new ArrayList<Var>();
			for(Node var : sparqlVarMap.keySet()) {
				projectionVars.add((Var)var);
			}
		}
		
		for(Var var : projectionVars) {
			resultVars.add(var.getName());
		}
		
		
		
		// TODO ONLY FOR USE WITH MYSQL - MAY HAVE UNDEFINED EFFECTS WITH OTHER DBSs
		// Enables streaming result sets
		/*
		stmt.setFetchSize(Integer.MIN_VALUE);
		*/
		
		Iterator<Binding> it;
		
		if(sqlQuery == null) {
			it = Collections.emptyIterator(); 
		} else {
			ResultSet rs = stmt.executeQuery(sqlQuery);
			// Note: We commit AFTER closing the result set
			// Result sets may not be stable after commit
			
			it = new IteratorResultSetSparqlifyBinding(conn, rs, sparqlVarMap);
		}
		
		ResultSetSparqlify result = new ResultSetSparqlify(it, resultVars, 0);
		
		return result;
	}

}
