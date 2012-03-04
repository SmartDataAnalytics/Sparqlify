package org.aksw.commons.collections.iterators;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

/**
 * Derived from
 * StackCartesianProductIterator<String> it = new StackCartesianProductIterator<String>(a, b,c);
 */
public class DescenderIterator<T> implements Iterator<List<T>> {
	// This list is used as a stack:
	// Every current element may be descended into
	private List<Collection<? extends T>> collections = new ArrayList<Collection<? extends T>>();
	private List<Iterator<? extends T>> iterators;

	private List<T> current;

	private List<T> result;
	private List<T> resultView;

	private Descender<T> descender;
	
	private boolean hasNext = true;

	public DescenderIterator(T base, Descender<T> descender) {
		this.descender = descender;

		
		Collection<T> collection = descender.getDescendCollection(base);
		if(collection == null || collection.isEmpty()) {
			hasNext = false;
			return;
		}

		collections.add(collection);
		init();
	}


	private void init() {
		
		iterators = new ArrayList<Iterator<? extends T>>();
		
		current = new ArrayList<T>();
		result = new ArrayList<T>();

		/*
		Iterator<? extends T> it = collections.get(0).iterator();
		iterators.add(it);
		current.add(it.next());
		result.add(null);
*/
		addIterator(collections.get(0).iterator());
		
		resultView = Collections.unmodifiableList(result);
	}

	public static <T> List<Integer> getIndexesOfEmptySubIterables(List<? extends Iterable<? extends T>> iterables) {
		List<Integer> result = new ArrayList<Integer>();

		for(int i = 0; i < iterables.size(); ++i) {
			Iterable<? extends T> iterable = iterables.get(i);
			
			if(Iterables.isEmpty(iterable)) {
				result.add(i);
			}
		}
		
		return result;
	}
	
	
	public boolean canDescend() {
		return iterators.size() < collections.size();
	}

		
	public void descend() {
		int index = iterators.size();

		if (index >= collections.size()) {
			throw new IndexOutOfBoundsException("Index: " + index + " Size: " + collections.size());
		}

		Iterator<? extends T> it = collections.get(index).iterator();
 

		addIterator(it);
	}

	public void addIterator(Iterator<? extends T> it) {
		iterators.add(it);
		T item = it.next();
		current.add(item);
		
		Collection<T> collection = descender.getDescendCollection(item);
		if(collection != null && !collection.isEmpty()) {
			collections.add(collection);
		}		
	}
	
	@Override
	public boolean hasNext() {
		return hasNext;
	}

	public List<T> peek()
	{
		adjustResultSize();

		for (int i = 0; i < current.size(); ++i)
			result.set(i, current.get(i));

		return result;
	}
	
	private void adjustResultSize()
	{
		// Adjust the size of the result
		while(result.size() < current.size()) {
			result.add(null);
		}
		
		while(result.size() > current.size()) {
			result.remove(result.size() - 1);
		}
	}
	
	@Override
	public List<T> next() {
		if (!hasNext)
			return null;		

		adjustResultSize();
		
		for (int i = 0; i < current.size(); ++i)
			result.set(i, current.get(i));

		// increment iterators
		for (int i = iterators.size() - 1; i >= 0; --i) {
			Iterator<? extends T> it = iterators.get(i);

			// if the iterator overflows => redo the loop and increment the
			// next iterator - otherwise break.
			if (!it.hasNext()) {
				if(i == 0) {
					hasNext = false;
					break;
				}

				iterators.remove(i);
				current.remove(i);

			} else {
				T item = it.next();
				current.set(i, item);
				
				Collection<T> collection = descender.getDescendCollection(item);
				if(collection != null && !collection.isEmpty()) {
					collections.add(collection);
				}
				break;
			}
		}

		return resultView;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Operation not supported");
	}	
}