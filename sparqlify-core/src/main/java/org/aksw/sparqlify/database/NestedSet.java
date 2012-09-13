package org.aksw.sparqlify.database;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.Sets;

/**
 * IMPORTANT: If you are working with a child set, then do not modify the parent,
 * or things might go out of sync.
 * So the parent must remain unchanged while working with a child set
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 * @param <T>
 */
public class NestedSet<T>
	extends AbstractSet<T>
{
	private Set<T> parent = null;

	// if true, effective items is a view based on the parent't effective clauses
	// if false, effective items is a copy on its own behalf (independent of the parent)	
	
	public boolean isView() {
		return addedItems != null;
	}
	
	
	int parentEffectiveItemCount;
	
	int cachedSize;
	private Set<T> effectiveItems;
	private Set<T> addedItems;
	private Set<T> removedItems;

	public NestedSet(Set<T> items, boolean asView) {
		if(asView) {
			this.addedItems = new HashSet<T>();
			this.removedItems = new HashSet<T>();
			this.parentEffectiveItemCount = items.size();
			this.cachedSize = parentEffectiveItemCount;
			this.effectiveItems = Sets.union(addedItems, Sets.difference(items, removedItems));
		} else {
			if(items == null) {
				this.effectiveItems = new HashSet<T>();
			} else {
				this.effectiveItems = items;
				this.cachedSize = effectiveItems.size();
			}
		}
	}
	
	private void checkCopy(Set<T> deltaSet) {
		if(deltaSet.size() > 4 * parentEffectiveItemCount) {
			this.effectiveItems = new HashSet<T>(effectiveItems);
			this.addedItems = null;
			this.removedItems = null;
		}
	}
	
	/*
	@Override
	public boolean addAll(Collection<? extends T> items) {
		boolean result = false;
		for(T item : items) {
			result = result || add(item);
		}
		
		return result;
	}*/
	
	public boolean add(T item) {
		if(effectiveItems.contains(item)) {
			return false;
		}

		if(isView()) {
			checkCopy(addedItems);
			
			if(isView()) {
				addedItems.add(item);
				removedItems.remove(item);				
			}
		}
		
		if(!isView()){
			effectiveItems.add(item);
		}		
		
		++cachedSize;
		onAdd(item);
		return true;
	}

	/**
	 * State a clause as removed - equivalent to stating the clause as unsatisfiable
	 * 
	 * @param clause
	 */
	public boolean remove(Object item) {
		if(!effectiveItems.contains(item)) {
			return false;
		}

		if(isView()) {	
			checkCopy(removedItems);

			if(isView()) {
				removedItems.add((T)item);
				addedItems.remove(item);
			}
		}
		
		if(!isView()) {
			effectiveItems.remove(item);
		}
		
		onRemove((T)item);
		--cachedSize;
		return true;
	}
	
	protected void onAdd(T item) {
	}
	
	protected void onRemove(T item) {
	}
	
	public int getNestingDepth() {
		if(parent != null && parent instanceof NestedSet) {
			return 1 + ((NestedSet<T>)parent).getNestingDepth();
		}
		
		return 0;
	}
	
	@Override
	public boolean contains(Object item) {
		return effectiveItems.contains(item);
	}
	
	/**
	 * Whether the given item is present in *this* instance, rather
	 * than being actually a member of one of the parents 
	 * 
	 * @param item
	 * @return
	 */
	//@Override
	public boolean containsDirect(Object item) {
		return isView()
				? addedItems.contains(item)
				:effectiveItems.contains(item);
	}
	
	@Override
	public Iterator<T> iterator() {
		return effectiveItems.iterator();
	}

	@Override
	public int size() {
		return cachedSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((effectiveItems == null) ? 0 : effectiveItems.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NestedSet other = (NestedSet) obj;
		if (effectiveItems == null) {
			if (other.effectiveItems != null)
				return false;
		} else if (!effectiveItems.equals(other.effectiveItems))
			return false;
		return true;
	}
	
	
}