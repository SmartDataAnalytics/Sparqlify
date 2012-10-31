package org.aksw.sparqlify.sparqlview;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.restriction.RestrictionManagerImpl;

import sparql.TwoWayBinding;

public class SparqlViewConjunction {
	private List<SparqlViewInstance> viewBindings;		// Maybe set of views???
	private RestrictionManagerImpl restrictions;
	
	private TwoWayBinding completeBinding; // Not sure if that is needed - maybe the restrictions already contain all information
	
	public List<String> getViewNames() {
		List<String> result = new ArrayList<String>(viewBindings.size());
		
		for(SparqlViewInstance instance : viewBindings) {
			result.add(instance.getParent().getName());
		}
		
		return result;
	}
	
	@Deprecated
	public SparqlViewConjunction(List<SparqlViewInstance> viewBindings, 
			RestrictionManagerImpl restrictions, TwoWayBinding completeBinding) {
		super();
		this.viewBindings = viewBindings;
		this.restrictions = restrictions;
		this.completeBinding = completeBinding;
		
//		throw new RuntimeException("Deprecated");
	}
	
	public SparqlViewConjunction(List<SparqlViewInstance> viewBindings, 
			RestrictionManagerImpl restrictions) //, TwoWayBinding completeBinding)
	{
		super();
		this.viewBindings = viewBindings;
		this.restrictions = restrictions;
		//this.completeBinding = completeBinding;
	}
		
	public List<SparqlViewInstance> getViewBindings()
	{
		return viewBindings;
	}
	
	public RestrictionManagerImpl getRestrictions() {
		return restrictions;
	}

	@Deprecated
	public TwoWayBinding getCompleteBinding()
	{
		//throw new RuntimeException("Deprecated");
		return completeBinding;
	}
	
	@Override
	public String toString()
	{
		return "" + viewBindings;
	}
	
}

