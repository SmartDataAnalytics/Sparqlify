package org.aksw.sparqlify.core.algorithms;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.sparqlify.core.interfaces.IViewDef;
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
public class ViewInstanceJoin<T extends IViewDef>
{
	//private List<T> viewInstances;		// Maybe set of views???

	/**
	 * The multimap data structure groups view instances by their parent view,
	 * which enables more efficient self join elimination
	 * (There can only be self joins if the instance's parent views are the same) 
	 */
	private ListMultimap<String, ViewInstance<T>> nameToInstances = ArrayListMultimap.create();

	
	private RestrictionManagerImpl restrictions;
	
	//private TwoWayBinding completeBinding; // Not sure if that is needed - maybe the restrictions already contain all information
	
	public List<String> getViewNames() {
		Collection<ViewInstance<T>> viewInstances = getViewInstances();
		
		List<String> result = new ArrayList<String>(viewInstances.size());
		
		for(ViewInstance<T> instance : viewInstances) {
			result.add(instance.getViewDefinition().getName());
		}
		
		return result;
	}

	public static <T extends IViewDef> ListMultimap<String, ViewInstance<T>> toMap(Collection<ViewInstance<T>> viewInstances) {
		ListMultimap<String, ViewInstance<T>> result = ArrayListMultimap.create();
		
		for(ViewInstance<T> viewInstance : viewInstances) {
			String viewName = viewInstance.getViewDefinition().getName();
			result.put(viewName, viewInstance);
		}
		
		return result;
	}
	
	public ListMultimap<String, ViewInstance<T>> getInstancesGroupedByParent() {
		return nameToInstances;
	}
	
	public ViewInstanceJoin(List<ViewInstance<T>> viewInstances, 
			RestrictionManagerImpl restrictions)
	{
		this(toMap(viewInstances), restrictions);
	}

	public ViewInstanceJoin(ListMultimap<String, ViewInstance<T>> nameToInstances, 
			RestrictionManagerImpl restrictions)
	{
		super();
		this.nameToInstances = nameToInstances;
		this.restrictions = restrictions;
		//this.completeBinding = completeBinding;
	}

	
	public Collection<ViewInstance<T>> getViewInstances()
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
