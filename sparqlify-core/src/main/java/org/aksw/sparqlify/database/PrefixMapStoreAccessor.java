package org.aksw.sparqlify.database;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.commons.collections15.Transformer;


interface MapStoreAccessor {
	//void add(Object store, List<?> row);
	//void put(Object store, Object )
	
	Set<Class<?>> getSupportedConstraintClasses();
	
	//
	// Insertion and lookup of rows 
	//
	void put(Object store, List<?> row, Object value);
	Object get(Object store, List<Object> row);
	
	// TODO: Is this the same as lookup without constraints? Guess so!
	Collection<Object> list(Object store);
	
	//
	// Lookup with constraints (e.g. greater(1.0), equals('foo'), ... 
	//
	Collection<Object> lookup(Object store, Constraint constraint);
	
	
	/**
	 * Create a new store that can be used with this accessor
	 * 
	 * @return
	 */
	Object createStore();
}


interface IndexStore {
	public List<Object> lookup(List<Constraint> constraints);
}



/**
 * Accessor for a NavigableMap<List<?>, Object>
 * 
 * We separate the accessor from the store, in order to avoid duplicating
 * (referenences to) the index metadata at each node of the index tree -
 * this way there is just one accessor with the metadata, which can
 * work on any corresponding store at some level in an hierarchical index.  
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
public class PrefixMapStoreAccessor
	implements MapStoreAccessor
{
	public static Set<Class<?>> supportedConstraintClasses = new HashSet<Class<?>>();

	static
	{
		supportedConstraintClasses.add(EqualsConstraint.class);
		supportedConstraintClasses.add(StartsWithConstraint.class);
		supportedConstraintClasses.add(IsPrefixOfConstraint.class);		
	}
	
	public Set<Class<?>> getSupportedConstraintClasses() {
		return supportedConstraintClasses;
	}

	
	private int indexColumn;
	private Transformer<Object, Set<String>> prefixExtractor;

	public PrefixMapStoreAccessor(int[] indexColumns, Transformer<Object, Set<String>> prefixExtractor) {
		if(indexColumns.length != 1) {
			throw new RuntimeException("Prefix index can only operate on single columns");
		}
		
		this.indexColumn = indexColumns[0];
		this.prefixExtractor = prefixExtractor;
	}
	
	/**
	 * We assume that
	 * . only classes that are in supportedConstraintClasses are passed to this method.
	 * . only constraints for which the index has the right columns are passed
	 * 
	 * This method should only be used by the engine
	 * 
	 * @param constraints
	 * @return
	 */
	/*
	@Override
	public Collection<Object> lookup(Object store, List<Constraint> constraints)
	{
		return lookup((NavigableMap<String, Object>)store, constraints);
	}*/
		

	@Override
	public void put(Object store, List<?> row, Object value) {
		/*
		PatriciaTrie<String, Integer> x;
		x.prefixMap(arg0)
		*/
		
		
		put((NavigableMap<String, Object>)store, row, value);
	}
	
	public void put(NavigableMap<String, Object> map, List<?> row, Object value) {
		map.put((String)row.get(indexColumn), value);
	}
	
	///*protected PrefixMap<PrefixIndexElement>
	
	/**
	 * The constraints given as arguments are interpreted as conjunctions.
	 * The set of prefixes within a constraint is interpreted as a disjunction. 
	 * 
	 * 
	 * @param constraints
	 */
	public Collection<Object> lookup(Object store, Constraint constraint) {
		NavigableMap<String, Object> map = (NavigableMap<String, Object>)store; 
		
		if(constraint instanceof IsPrefixOfConstraint) {
			return lookup(map, (IsPrefixOfConstraint)constraint);
		} else if(constraint instanceof StartsWithConstraint) {
			return lookup(map, (StartsWithConstraint)constraint);
		} else if(constraint instanceof EqualsConstraint) {
			return lookup(map, (EqualsConstraint)constraint);
		} else {
			throw new RuntimeException("Could not handle constraint " + constraint);
		}
	}
	
	public Collection<Object> lookup(NavigableMap<String, Object> map, EqualsConstraint constraint) {
		return Collections.singleton(map.get(constraint.getValue()));
	}
	
	public Collection<Object> lookup(NavigableMap<String, Object> map, StartsWithConstraint constraint) {
		return StringUtils.getAllPrefixedEntries(constraint.getPrefix(), constraint.isInclusive(), map).values();
	}

	public Collection<Object> lookup(NavigableMap<String, Object> map, IsPrefixOfConstraint constraint) {
		return StringUtils.getAllPrefixes(constraint.getValue(), constraint.isInclusive(), map).values();
	}

	@Override
	public Object get(Object store, List<Object> row) {
		return get((NavigableMap<String, Object>)store, row);
	}
	
	public Object get(NavigableMap<String, Object> map, List<Object> row) {
		return map.get(row.get(indexColumn));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Object> list(Object store) {
		return ((NavigableMap<String, Object>)store).values();
	}

	@Override
	public Object createStore() {
		return new TreeMap<String, Object>();
	}
	
	

	/*
	public Collection<List<T>> lookupSimple(List<String> prefixList)
	{
		List<List<T>> result = new ArrayList<List<T>>();
		lookupSimple(prefixList, 0, map, result);
		return result;
	}

	public Collection<List<T>> lookupSimpleLonger(List<String> prefixList)
	{
		List<List<T>> result = new ArrayList<List<T>>();
		lookupSimpleLonger(prefixList, 0, map, result);
		return result;
	}
	*/
	
	/*
	public void lookupSimple(List<String> prefixList, int i, NavigableMap<String, Object> current, Collection<List<T>> result)
	{
		boolean isLast = i == prefixList.size();

		//Entry<String, Object> o;

		if(isLast) {
			lookupRemaining(current, i, result);
		} else {
			int index = indexColumns[i];
			String prefix = prefixList.get(index);

			Map<String, Object> candidates = StringUtils.getAllPrefixes(prefix, true, current);
			for(Entry<String, Object> entry : candidates.entrySet()) {
				NavigableMap<String, Object> next = (NavigableMap<String, Object>) entry.getValue();
				lookupSimple(prefixList, i + 1, next, result);
			}
		
		}		
	}
	
	public void lookupSimpleLonger(List<String> prefixList, int i, NavigableMap<String, Object> current, Collection<List<T>> result)
	{
		boolean isLast = i == prefixList.size();

		//Entry<String, Object> o;

		if(isLast) {
			lookupRemaining(current, i, result);
		} else {
			int index = indexColumns[i];
			String prefix = prefixList.get(index);

			Map<String, Object> candidates = StringUtils.getAllPrefixedEntries(prefix, true, current);
			for(Entry<String, Object> entry : candidates.entrySet()) {
				NavigableMap<String, Object> next = (NavigableMap<String, Object>) entry.getValue();
				lookupSimpleLonger(prefixList, i + 1, next, result);
			}
		
		}		
	}
	
	
	
	public void lookupRemaining(NavigableMap<String, Object> current, int i, Collection<List<T>> result) {
		// Add everything that is left
		boolean isLast = i == indexColumns.length - 1;
		int index = indexColumns[i];
			
		if(isLast) {
			for(Object o : current.values()) {				
				List<T> row = (List<T>) o;
				result.add(row);
			}
		} else {
			for(Object o : current.values()) {
				NavigableMap<String, Object> next = (NavigableMap<String, Object>) o;
				
				lookupRemaining(next, i + 1, result);
			}
		}
	}

	/*
	public Collection<List<T>> lookup(List<Set<String>> prefixesList) {
		List<List<T>> result = new ArrayList<List<T>>();
		
		lookup(prefixesList, 0, map, result);
		
		return result;
	}*/

	/*
	public void lookup(List<Set<String>> prefixesList, int i, NavigableMap<String, Object> current, Collection<List<T>> result) {

		boolean isLast = i == indexColumns.length - 1;
		int index = indexColumns[i];
		Set<String> prefixes = prefixesList.get(index);

		for(String prefix : prefixes) {
			Object o = current.get(prefix);
			
			if(isLast) {
				if(o == null) {
					return;
				}
				
				List<T> row = (List<T>) o;
				
				result.add(row);
			} else {				
				NavigableMap<String, Object> next = (NavigableMap<String, Object>) o;
				if(next == null) {
					return;
				}
				
				lookup(prefixesList, i + 1, next, result);
			}
		}

			
	}
	*/
}
