package org.aksw.sparqlify.update;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.aksw.commons.collections.CacheSet;
import org.apache.jena.sparql.core.Quad;

/**
 * A listener that avoids reporting duplicate insertions or removals
 * 
 * A clean listener only notifies about changes to a graph.
 * Inserts events require the store to be in the state before the insert occurres,
 * Removes are reported BEFORE the remove occurrs
 * 
 * 
 * @author raven
 *
 */
public class CleanListener
	implements GraphListener
{
	private Set<GraphListener> graphListeners = new HashSet<GraphListener>();
	
	private Set<Quad> insertCache = new CacheSet<Quad>(10000, true);
	private Set<Quad> deleteCache = new CacheSet<Quad>(10000, true);

	private Set<Quad> safeInserts = new HashSet<Quad>();
	private Set<Quad> safeDeletes = new HashSet<Quad>();

	private Set<Quad> verifyInserts = new HashSet<Quad>();
	private Set<Quad> verifyDeletes = new HashSet<Quad>();

	
	private ModelSparqlEndpoint endpoint;
	
	
	public CleanListener(ModelSparqlEndpoint endpoint)
	{
		this.endpoint = endpoint;
	}
	
	public Set<Quad> verify(Collection<Quad> quads, boolean shouldExist) {
		Set<Quad> result = new HashSet<Quad>();
				
		for(Quad quad : quads) {
			String askQuery = FilterCompiler.askForQuad(quad);
			System.out.println(askQuery);
			boolean exists = endpoint.createQueryExecution(askQuery).execAsk();

			/*
			 TODO Can we set the caches here? I guess not
			if(exists) {
				insertCache.add(quad);
			} else {
				deleteCache.add(quad);
			}*/
			
			if(exists == shouldExist) {
				result.add(quad);
			}
		}
		
		return result;
	}

	/*
	public void doInsert() {
		for(GraphListener listener : graphListeners) {
			listener.onPreBatchStart();
			
			for(Quad quad : safeInserts) {
				listener.onPreInsert(quad);
			}
			
			listener.onPreBatchEnd();
		}
		
		safeInserts.clear();
	}
	
	public void doDelete() {
		for(GraphListener listener : graphListeners) {
			listener.onPreBatchStart();
			
			for(Quad quad : safeDeletes) {
				listener.onPreDelete(quad);
			}
			
			listener.onPreBatchEnd();
		}
		
		safeDeletes.clear();
	}
	*/

	public void verifyInserts() {
		Set<Quad> verified = verify(verifyInserts, false);
		safeInserts.addAll(verified);
		verifyInserts.clear();
	}
	
	public void verifyDeletes() {
		Set<Quad> verified = verify(verifyDeletes, true);
		safeDeletes.addAll(verified);
		verifyDeletes.clear();
	}
	
	
	@Override
	public void onPreInsert(Quad quad) {
		if(insertCache.contains(quad)) {
			return;
		}
		
		if(deleteCache.contains(quad)) {
			safeInserts.add(quad);
			deleteCache.remove(quad);
			insertCache.add(quad);
		} else {
			verifyInserts.add(quad);
		}
	}

	@Override
	public void onPreDelete(Quad quad) {
		if(deleteCache.contains(quad)) {
			return;
		}

		if(insertCache.contains(quad)) {
			safeDeletes.add(quad);
			insertCache.remove(quad);
			deleteCache.add(quad);
		} else {
			verifyDeletes.add(quad);
		}
	}


	public Set<GraphListener> getGraphListeners() 
	{
		return graphListeners;
	}

	@Override
	public void onPreBatchStart() {
	}

	@Override
	public void onPreBatchEnd() {
		verifyDeletes();
		verifyInserts();
		
		for(GraphListener listener : graphListeners) {
			listener.onPreBatchStart();
			
			for(Quad item : safeDeletes) {
				listener.onPreDelete(item);
			}

			for(Quad item : safeInserts) {
				listener.onPreInsert(item);
			}
			
			listener.onPreBatchEnd();
		}
	}

	@Override
	public void onPostBatchStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostInsert(Quad quad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostDelete(Quad quad) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPostBatchEnd() {
		for(GraphListener listener : graphListeners) {
			listener.onPostBatchStart();
		
			for(Quad item : safeDeletes) {
				listener.onPostDelete(item);
			}
	
			for(Quad item : safeInserts) {
				listener.onPostInsert(item);
			}

			listener.onPostBatchEnd();
		}
	
		safeInserts.clear();
		safeDeletes.clear();
	}
}