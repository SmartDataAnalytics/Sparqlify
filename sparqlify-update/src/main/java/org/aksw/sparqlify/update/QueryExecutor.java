package org.aksw.sparqlify.update;

import java.util.Collection;
import java.util.Set;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.Quad;

/**
 * A query executor can be seen as a sparql endpoint with
 * a fixed, preconfigured state.
 * 
 * @author raven
 *
 */
interface QueryExecutor
{
	Set<GraphListener> getGraphListeners();

	void addGraphListener(GraphListener graphListener);
	void removeGraphListener(GraphListener graphListener);
	
	void executeUpdate(String query);

	ResultSet executeSelect(String query);
	
	Model executeConstruct(String query);
	void executeConstruct(String query, Model out);
	
	boolean executeAsk(String query);

	Model executeDescribe(String query);
	
	// Simple insert/delete. A bulk update handler would be nice to have
	void insert(Collection<Quad> quads);
	void remove(Collection<Quad> quads);
}