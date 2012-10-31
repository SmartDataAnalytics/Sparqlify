package org.aksw.sparqlify.core.algorithms;


import java.util.ArrayList;
import java.util.List;

import org.aksw.sparqlify.restriction.RestrictionManagerImpl;

/***
 * A complete binding consists of a list of views and an overall binding
 * 
 * Can be seen as a conjunction of views
 * 
 * @author raven
 *
 */
public class ViewInstanceJoin
{
	private List<ViewInstance> viewInstances;		// Maybe set of views???
	private RestrictionManagerImpl restrictions;
	
	//private TwoWayBinding completeBinding; // Not sure if that is needed - maybe the restrictions already contain all information
	
	public List<String> getViewNames() {
		List<String> result = new ArrayList<String>(viewInstances.size());
		
		for(ViewInstance instance : viewInstances) {
			result.add(instance.getViewDefinition().getName());
		}
		
		return result;
	}

	public ViewInstanceJoin(List<ViewInstance> viewBindings, 
			RestrictionManagerImpl restrictions) //, TwoWayBinding completeBinding)
	{
		super();
		this.viewInstances = viewBindings;
		this.restrictions = restrictions;
		//this.completeBinding = completeBinding;
	}
		
	public List<ViewInstance> getViewInstances()
	{
		return viewInstances;
	}
	
	public RestrictionManagerImpl getRestrictions() {
		return restrictions;
	}
	
	@Override
	public String toString()
	{
		return "" + viewInstances;
	}	
}
