package sparql;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.collections.multimaps.BiHashMultimap;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.collections.multimaps.MultimapUtils;

import com.google.common.collect.Sets;


/**
 * A map where keys can be stated as equivalent.
 * 
 * Provides methods for checking consistency (the map gets inconsistent
 * as soon as a key maps to multiple non-equal values)
 * 
 * 
 * TODO Not sure if a conflict resultion strategy for the case that
 * values are not directly equal but not contradictory should go here.
 * For instance if a key x maps to {A, B} and y maps to {A} one resolution stragtegy
 * might be to map both to A.
 * 
 * Essentially I have the ValueSets in mind to quickly reject inconsistent bindings.
 * 
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class EquiMap<K, V>
{
	private IBiSetMultimap<K, K> equivalences = new BiHashMultimap<K, K>();
	private Map<K, V> keyToValue = new HashMap<K, V>();
	
	
	
	/**
	 * 
	 * @return A view of all keys
	 */
	public Set<K> keySet() {
		return Sets.union(equivalences.asMap().keySet(), keyToValue.keySet());
	}
	
	public void clear()
	{
		equivalences.clear();
		keyToValue.clear();
	}
	
	public boolean isEqual(K a, K b) {
		return get(a).contains(b);
	}
	
	public boolean areAllEqual(Collection<K> set) {
		if(set.isEmpty()) {
			return true; // Is that sane? Should it be false?
		}
		
		return get(set.iterator().next()).containsAll(set);
	}

	
	public boolean isConsistentInsertEquiv(K a, K b)
	{
		return isConsistentSet(Sets.union(get(a), get(b)));
	}
	
	public boolean isConsistentSet(Set<V> set)
	{
		return set.size() <= 1;
	}
	
	public boolean isConsistentInsertValue(K a, V b)
	{
		Set<V> values = get(a);
		
		if(values.isEmpty()) {
			return true;
		} else if(values.size() == 1 && values.contains(b)) {
			return true;
		} else if(values.size() > 1) {
			throw new RuntimeException("Should not happen");
		}
		
		return false;
	}
	
	public boolean isSelfConsistent()
	{
		Set<K> open = new HashSet<K>(keyToValue.keySet());
		while(!open.isEmpty()) {
			K key = open.iterator().next();
			open.remove(key);

			Set<K> keys = getEquivalences(key, false);
			open.removeAll(keys);
			
			Set<V> values = get(keys);
			if(!isConsistentSet(values)) {
				return false;
			}
		}			
			
		return true;
	}

	
	public EquiMap()
	{
	}
	
	public EquiMap(EquiMap<K, V> other)
	{		
		for(Map.Entry<K, Collection<K>> entry : equivalences.asMap().entrySet()) {
			for(K value : entry.getValue()) {
				this.equivalences.put(entry.getKey(), value);
			}
		}
		
		
		this.keyToValue.putAll(other.keyToValue);
	}
	
	
	public IBiSetMultimap<K, K> getEquivalences()
	{
		return equivalences;
	}

	public Map<K, V> getKeyToValue()
	{
		return keyToValue;
	}

	
	public void put(K key, V value)
	{
		keyToValue.put(key, value);
	}
	
	public Set<V> getAll(Set<?> keys)
	{
		Set<V> result = new HashSet<V>();

		for(Object key : keys) {
			if(keyToValue.containsKey(key)) {
				result.add(keyToValue.get(key));
			}
		}
		
		return result;		
	}
	
	public Set<K> getEquivalences(Object key, boolean reflexiv)
	{
		Set<K> result = MultimapUtils.transitiveGetBoth(equivalences, key);
		if(reflexiv) {
			result.add((K)key);
		}
		
		return result;
	}
	
	public Set<K> getAllEquivalences(Collection<?> keys, boolean reflexiv)
	{
		Set<K> result = new HashSet<K>();

		Set<Object> open = new HashSet<Object>(keys);
		while(!open.isEmpty()) {
			Object key = open.iterator().next();
			open.remove(key);

			Set<K> equivs = getEquivalences(key, reflexiv);
			open.removeAll(equivs);
			
			result.addAll(equivs);			
		}			

		return result;
	}


	public Set<V> get(Object key)
	{
		return getAll(getEquivalences(key, true));		
	}

	public boolean makeEqual(K a, K b)
	{
		return equivalences.put(a, b);
	}
	
	
	/**
	 * Checks whether the union of two equimaps is again consistent.
	 * 
	 * 
	 * @param other
	 * @return
	 */
	public boolean isCompatible(EquiMap<K, V> other)
	{
		// We have to check whether any of the keys refer to multiple distinct variables
		// So we need to check for each key that has a value attached
		Set<K> open = new HashSet<K>(keyToValue.keySet());				

		while(!open.isEmpty()) {
			K key = open.iterator().next();
			open.remove(key);
			
			Set<K> bothEquivs = new HashSet<K>();
			//bothEquivs.add(key); redundant
			
			// We need to traverse the bipartite graph is order to collect 
			// all equivalences
			//
			//
			Set<K> newEquivs = Collections.singleton(key);
			do {
				Set<K> thisEquivs = getAllEquivalences(newEquivs, true);
				thisEquivs.removeAll(bothEquivs);
				open.removeAll(thisEquivs);
				bothEquivs.addAll(thisEquivs);				
		
				newEquivs = other.getAllEquivalences(thisEquivs, true);
				newEquivs.removeAll(bothEquivs);
				open.removeAll(newEquivs);
				
				bothEquivs.addAll(newEquivs);				
				
			} while(!newEquivs.isEmpty());
				
			Set<V> thisValues = getAll(bothEquivs);
			Set<V> otherValues = other.getAll(bothEquivs);

			Set<V> union = Sets.union(thisValues, otherValues);

			if(!isConsistentSet(union)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public String toString()
	{
		return "[equivalences=" + equivalences + ", keyToValue="
				+ keyToValue + "]";
	}
	
}
