package org.aksw.sparqlify.database;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.commons.collections15.Transformer;
import org.ardverk.collection.PatriciaTrie;
import org.ardverk.collection.StringKeyAnalyzer;

public class PatriciaPrefixMapStoreAccessor
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

	public PatriciaPrefixMapStoreAccessor(int[] indexColumns, Transformer<Object, Set<String>> prefixExtractor) {
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
		
		
		put((PatriciaTrie<String, Object>)store, row, value);
	}
	
	public void put(PatriciaTrie<String, Object> map, List<?> row, Object value) {
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
		PatriciaTrie<String, Object> map = (PatriciaTrie<String, Object>)store; 
		
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
	
	public Collection<Object> lookup(PatriciaTrie<String, Object> map, EqualsConstraint constraint) {
		return Collections.singleton(map.get(constraint.getValue()));
	}
	
	public Collection<Object> lookup(PatriciaTrie<String, Object> map, StartsWithConstraint constraint) {
		if(!constraint.isInclusive()) {
			throw new RuntimeException("Patricia tree does not support 'non-inclusive' constraint, and I haven't hacked that in yet");
		}

		return map.prefixMap(constraint.getPrefix()).values();
	}

	public Collection<Object> lookup(PatriciaTrie<String, Object> map, IsPrefixOfConstraint constraint) {
		String lookup = constraint.getValue();
		if(!constraint.isInclusive()) {
			int n = lookup.length();
			if(n == 0) {
				return Collections.emptySet();
			} else {
				lookup.substring(0, n - 1);
			}
		}
		
		//Object o= map.get("");
		if(true) {
			throw new RuntimeException("Either the patricia tree impl is broken, or I misunderstood something about it..., but the prefix map does not contain all prefixes of a given string");
		}
		
		Collection<Object> result = map.prefixMap(lookup).values();
		return result;
		
		//return StringUtils.getAllPrefixes(constraint.getValue(), constraint.isInclusive(), map).values();
	}

	@Override
	public Object get(Object store, List<Object> row) {
		return get((PatriciaTrie<String, Object>)store, row);
	}
	
	public Object get(PatriciaTrie<String, Object> map, List<Object> row) {
		return map.get(row.get(indexColumn));
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Object> list(Object store) {
		return ((PatriciaTrie<String, Object>)store).values();
	}
	
	
	@Override
	public PatriciaTrie<String, Object> createStore() {
		return new PatriciaTrie<String, Object>(StringKeyAnalyzer.BYTE);
	}
	

}
