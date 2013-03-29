package org.aksw.sparqlify.sparqlview;

import java.util.Map.Entry;

import sparql.TwoWayBinding;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.QuadPattern;
import com.hp.hpl.jena.sparql.core.Var;


public class ViewInstanceOld {
	protected Quad queryQuad; // The quad in the query this view was originally instanced for
	protected Quad viewQuad;  // The quad in the parent view that originally matched
	
	protected QuadPattern queryQuads = new QuadPattern(); // The quad pattern that the view now answeres
	protected QuadPattern viewQuads  = new QuadPattern(); // The quad pattern that the view now answeres

	protected int instanceId; // id of the view instance (instance for a quad)
	protected int subId; // sub-id of the instance (in the process the "same" quad) TODO Not sure what i mean here 
	
	protected BiMap<Node, Node> renamer; // The substitution that was used
	
	protected TwoWayBinding binding; // The binding from the variables of the view (actually one of the quads)
	                               // to the quad of the query
	
	//private ConstraintContainer constraints;
	
	/*
	public ViewInstance copy() {
		return new ViewInstance(queryQuad, viewQuad, instanceId, subId, parent, binding.copySubstitute(null));
	}
	*/
	
	public ViewInstanceOld(Quad queryQuad, Quad viewQuad, int instanceId, int subId, TwoWayBinding binding)
	{
		super();
		this.queryQuad = queryQuad;
		this.viewQuad = viewQuad;
		
		queryQuads.add(queryQuad);
		viewQuads.add(viewQuad);
		
		this.instanceId = instanceId;
		this.subId = subId;
		//this.parent = parent;

		//this.renamer = RdfViewSystemOld.createVariableMappingInstance(parent, instanceId);		
	}

	
	
	public boolean isViewVariable(Var var) {
		return var.getName().startsWith("view");
	}
	
	
	/**
	 * Returns a map from query level to the parent, so the instance level
	 * is omitted.
	 * 
	 * 
	 * @return
	 */
	public SetMultimap<Var, Var> getQueryToParentBinding()
	{			
		SetMultimap<Var, Var> result = HashMultimap.create();
		for(Entry<Var, Var> entry : binding.getEquiMap().getEquivalences().entries()) {
			Var back = (Var)renamer.inverse().get(entry.getValue());
			if(back == null) {
				continue;
			}
			
			result.put(entry.getKey(), back);
		}
		
		return result;
	}

	/**
	 * How the query variables map to the parent view
	 * Given a query with var a, and a virtual graph with ?s:
	 * 
	 * e.g. {?view1_s=[?a]}
	 * 
	 * 
	 * @return
	 */
	public SetMultimap<Var, Var> getParentToQueryBinding()
	{			
		SetMultimap<Var, Var> result = HashMultimap.create();
		for(Entry<Var, Var> entry : binding.getEquiMap().getEquivalences().entries()) {
			Var back = (Var)renamer.inverse().get(entry.getValue());
			if(back == null) {
				continue;
			}
			
			result.put(back, entry.getKey());
		}
		
		return result;
	}
	
	
	public QuadPattern getQueryQuads()
	{
		return queryQuads;
	}

	public QuadPattern getViewQuads()
	{
		return viewQuads;
	}

	public Quad getQueryQuad()
	{
		return queryQuad;
	}
	public Quad getViewQuad()
	{
		return queryQuad;
	}
	public int getInstanceId()
	{
		return instanceId;
	}
	
	/*
	public T getInstance()
	{
		return instance;
	}
	public T getParent()
	{
		return parent;
	}*/
	
	
	public BiMap<Node, Node> getRenamer()
	{
		return renamer;
	}
	public TwoWayBinding getBinding()
	{
		return binding;
	}
	
}


