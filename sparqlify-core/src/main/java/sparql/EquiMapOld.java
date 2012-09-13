package sparql;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A map that supports stating that keys are equal.
 * 
 * For each set of equal keys a set is kept, and each element in it points to the set.
 * 
 * Each equivalence set may only map to a single object.
 * The method "resolveConflict" can be implemented for conflict resolution.
 * If the conflict cannot be resolved, the equivalence is not inserted.
 * 
 * This implementation does not support removal.
 * 
 * 
 * WARNING DO NOT USE, THIS IMPLEMENTATION IS DANGEROUS
 */
public class EquiMapOld<K, V>
{
	private Map<Set<K>, V> toValue = new HashMap<Set<K>, V>();
	private Map<K, Set<K>> equalities = new HashMap<K, Set<K>>();
	
	public boolean put(K key, V value)
	{
		Set<K> eqs = equalities.get(key);
		if(eqs == null) {
			eqs = new HashSet<K>();
			eqs.add(key);
			
			toValue.put(eqs, value);
		} else {
			V v = toValue.get(eqs);
			
			if(v == null) {
				toValue.put(eqs, value);
			} else {
				return value.equals(v);
			}


			/*
			V v = toValue.get(eqs);
			
			if(v == null) {
				toValue.put(eqs, value);
			} else {
				if(!value.equals(v)) {

					/*
					V solution = resolveConflict(v, value);
					
					if(solution == null) {
						toValue.remove(eqs);
					} else {
						toValue.put(eqs, solution);
						
						return value.equals(solution);
					}
				}
			}
			*/
		}
		
		return true;
	}
	
	public V resolveConflict(V a, V b) {
		return null;
	}
	

	public V get(Object a)
	{
		Set<K> equalitySet = equalities.get(a);
		return equalitySet == null ? null : toValue.get(equalitySet); 
	}
	
	public boolean makeEqual(K a, K b) {

		Set<K> as = equalities.get(a);
		Set<K> bs = equalities.get(b);
 
		
		V aConst = toValue.get(as);
		V bConst = toValue.get(bs);
		
		if(aConst != null && bConst != null) {
			if(!aConst.equals(bConst)) {				
				V solution = resolveConflict(aConst, bConst);

				if(solution == null) {
					return false;
				}
				
				toValue.remove(as);
				toValue.remove(bs);

				aConst = solution;
			}
		}
				
		// Create equality set a (if not existing)
		if(as == null) {
			as = new HashSet<K>();
			equalities.put(a, as);
		} else {
			toValue.remove(as);
		}

		as.add(a); // each item is equal to itself
		as.add(b);
		equalities.put(b, as);

		// Merge b into a
		if(bs != null) {
			toValue.remove(bs);
			for(K item : bs) {
				equalities.put(item, as);
				as.add(item);
			}
		}

		// Get non-null value (if any)
		aConst = (aConst != null) ? aConst : bConst; 

		if(aConst != null) {
			toValue.put(as, aConst);
		}
		
		return true;
	}

	@Override
	public String toString()
	{
		return "EquiMap [toConst=" + toValue + ", equalities="
				+ equalities + "]";
	}

}