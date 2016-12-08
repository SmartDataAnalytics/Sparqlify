package org.aksw.sparqlify.update;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.util.reflect.MultiMethod;
import org.aksw.jena_sparql_api.utils.QueryUtils;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;


public class UpdateGraph
{
	private Set<GraphListener> graphListeners = new HashSet<GraphListener>();

	public Set<GraphListener> getPreUpdateListeners()
	{
		return graphListeners;
	}


	private ModelSparqlEndpoint endpoint;
	
	private int batchSize = 128;
	
	public UpdateGraph(ModelSparqlEndpoint endpoint)
	{
		this.endpoint = endpoint;
	}
	
    public static Map<Node, Graph> quadsToGraphs(Collection<Quad> quads) {
    	Map<Node, Graph> result = new HashMap<Node, Graph>();
    	
    	
    	for(Quad quad : quads) {
    		Triple triple = quad.asTriple();

    		Node graphName = quad.getGraph();
    		
    		Graph graph = result.get(graphName);
    		if(graph == null) {
    			graph = GraphFactory.createDefaultGraph();
    			result.put(graphName, graph);
    		}
    		
    		graph.add(triple);
    	}
    	
    	return result;
    }
    
    private void insert(Collection<Quad> quads) {
    	if(quads.isEmpty()) {
    		return;
    	}
    	
    	notifyPreUpdate(quads, null);
    	
    	for(Entry<Node, Graph> entry : quadsToGraphs(quads).entrySet()) {
    		Model model = ModelFactory.createModelForGraph(entry.getValue());
    		endpoint.insert(model, entry.getKey().toString());
    	}

    	notifyPostUpdate(quads, null);
    }

    private void notifyPreUpdate(Collection<Quad> inserts, Collection<Quad> deletes) {
    	
    	for(GraphListener graphListener : graphListeners) {
    		graphListener.onPreBatchStart();

    		if(inserts != null) {
	    		for(Quad quad : inserts) {
	    			graphListener.onPreInsert(quad);
	    		}
    		}
    		
    		if(deletes != null) {
	    		for(Quad quad : deletes) {
	    			graphListener.onPreDelete(quad);
	    		}
    		}
    		
    		graphListener.onPreBatchEnd();
    	}
    }

    private void notifyPostUpdate(Collection<Quad> inserts, Collection<Quad> deletes) {
    	
    	for(GraphListener graphListener : graphListeners) {
    		graphListener.onPostBatchStart();

    		if(inserts != null) {
	    		for(Quad quad : inserts) {
	    			graphListener.onPostInsert(quad);
	    		}
    		}
    		
    		if(deletes != null) {
	    		for(Quad quad : deletes) {
	    			graphListener.onPostDelete(quad);
	    		}
    		}
    		
    		graphListener.onPostBatchEnd();
    	}
    }
    
    
    
    private void delete(Collection<Quad> quads) {
    	if(quads.isEmpty()) {
    		return;
    	}
    	
    	notifyPreUpdate(null, quads);

    	for(Entry<Node, Graph> entry : quadsToGraphs(quads).entrySet()) {
    		Model model = ModelFactory.createModelForGraph(entry.getValue());
    		endpoint.remove(model, entry.getKey().toString());
    	}

    	notifyPostUpdate(null, quads);
    }

    /*
    protected void startBatch()
    {
    	for(GraphListener graphListener : preChangeGraphListeners) {
    		graphListener.onBatchStart();
    	}
    }

    protected void endBatch()
    {
    	for(GraphListener graphListener : preChangeGraphListeners) {
    		graphListener.onBatchEnd();
    	}
    }*/

    
	@SuppressWarnings("unused")
	private void _update(UpdateModify update)
	{
		Element wherePattern = update.getWherePattern();
		Query query = QueryUtils.elementToQuery(wherePattern);
		
		
		String queryStr = query.toString();
		
		// TODO Limit and offset...
		ResultSet rs = endpoint.createQueryExecution(queryStr).execSelect();
	
		Set<Quad> inserts = new HashSet<Quad>();
		Set<Quad> deletes = new HashSet<Quad>();
		
		while(rs.hasNext()) {
			Binding binding = rs.nextBinding();

			Set<Quad> i = QueryUtils.instanciate(update.getInsertQuads(), binding);
			Set<Quad> d = QueryUtils.instanciate(update.getDeleteQuads(), binding);

			inserts.addAll(i);
			deletes.addAll(d);

	    	if(inserts.size() > batchSize) {
	    		insert(inserts);
	    		inserts.clear();
	    	}

	    	if(deletes.size() > batchSize) {
	    		delete(deletes);
	    		deletes.clear();
	    	}
		}
		insert(inserts);
		delete(deletes);
	}
	
	@SuppressWarnings("unused")
	private void _update(UpdateDataInsert update)
	{
		insert(update.getQuads());
	}
	
	@SuppressWarnings("unused")
	private void _update(UpdateDataDelete update)
	{
		delete(update.getQuads());
	}
	
	public void executeUpdate(String queryStr)
	{
		UpdateRequest request = new UpdateRequest();
		UpdateFactory.parse(request, queryStr);
	
		for(Update update : request.getOperations()) {
			MultiMethod.invoke(this, "_update", update);
		}
	}
}

