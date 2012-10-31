package org.aksw.sparqlify.core;

import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.restriction.RestrictionManagerImpl;

import sparql.TwoWayBinding;

/***
 * A complete binding consists of a list of views and an overall binding
 * 
 * Can be seen as a conjunction of views
 * 
 * @author raven
 *
 */
public class RdfViewConjunction {
	private List<RdfViewInstance> viewBindings;		// Maybe set of views???
	private RestrictionManagerImpl restrictions;
	
	//private TwoWayBinding completeBinding; // Not sure if that is needed - maybe the restrictions already contain all information
	
	public List<String> getViewNames() {
		List<String> result = new ArrayList<String>(viewBindings.size());
		
		for(RdfViewInstance instance : viewBindings) {
			result.add(instance.getParent().getName());
		}
		
		return result;
	}
	
	@Deprecated
	public RdfViewConjunction(List<RdfViewInstance> viewBindings, 
			TwoWayBinding completeBinding) {
		throw new RuntimeException("Deprecated");
	}
	
	public RdfViewConjunction(List<RdfViewInstance> viewBindings, 
			RestrictionManagerImpl restrictions) //, TwoWayBinding completeBinding)
	{
		super();
		this.viewBindings = viewBindings;
		this.restrictions = restrictions;
		//this.completeBinding = completeBinding;
	}
		
	public List<RdfViewInstance> getViewBindings()
	{
		return viewBindings;
	}
	
	public RestrictionManagerImpl getRestrictions() {
		return restrictions;
	}

	@Deprecated
	public TwoWayBinding getCompleteBinding()
	{
		throw new RuntimeException("Deprecated");
		//return completeBinding;
	}
	
	@Override
	public String toString()
	{
		return "" + viewBindings;
	}
	
}