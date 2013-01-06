package org.aksw.sparqlify.core.algorithms;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.restriction.RestrictionManagerImpl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

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
	//private List<ViewInstance> viewInstances;		// Maybe set of views???

	/**
	 * The multimap data structure groups view instances by their parent view,
	 * which enables more efficient self join elimination
	 * (There can only be self joins if the instance's parent views are the same) 
	 */
	private ListMultimap<String, ViewInstance> nameToInstances = ArrayListMultimap.create();

	
	private RestrictionManagerImpl restrictions;
	
	//private TwoWayBinding completeBinding; // Not sure if that is needed - maybe the restrictions already contain all information
	
	public List<String> getViewNames() {
		Collection<ViewInstance> viewInstances = getViewInstances();
		
		List<String> result = new ArrayList<String>(viewInstances.size());
		
		for(ViewInstance instance : viewInstances) {
			result.add(instance.getViewDefinition().getName());
		}
		
		return result;
	}

	public static ListMultimap<String, ViewInstance> toMap(Collection<ViewInstance> viewInstances) {
		ListMultimap<String, ViewInstance> result = ArrayListMultimap.create();
		
		for(ViewInstance viewInstance : viewInstances) {
			String viewName = viewInstance.getViewDefinition().getName();
			result.put(viewName, viewInstance);
		}
		
		return result;
	}
	
	public ListMultimap<String, ViewInstance> getInstancesGroupedByParent() {
		return nameToInstances;
	}
	
	public ViewInstanceJoin(List<ViewInstance> viewInstances, 
			RestrictionManagerImpl restrictions)
	{
		this(toMap(viewInstances), restrictions);
	}

	public ViewInstanceJoin(ListMultimap<String, ViewInstance> nameToInstances, 
			RestrictionManagerImpl restrictions)
	{
		super();
		this.nameToInstances = nameToInstances;
		this.restrictions = restrictions;
		//this.completeBinding = completeBinding;
	}

	
	public Collection<ViewInstance> getViewInstances()
	{
		return nameToInstances.values();
	}
	
	public RestrictionManagerImpl getRestrictions() {
		return restrictions;
	}
	
	@Override
	public String toString()
	{
		return "" + getViewInstances();
	}	
}
