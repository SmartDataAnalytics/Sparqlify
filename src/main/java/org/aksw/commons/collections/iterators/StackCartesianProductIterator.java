package org.aksw.commons.collections.iterators;

import java.awt.Adjustable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;

/**
 * 
 * StackCartesianProductIterator<String> it = new StackCartesianProductIterator<String>(a, b,c);
 *
 * Example loop on how to use the iterator for bailing out early:
 *
 * while (it.hasNext()) {
 *     List<String> current = it.peek();
 *     System.out.println("Path: " + current);
 *     if(!isAccepted(current)) {
 *         it.next();
 *         continue;
 *     }
 *       	
 *     if(!it.canDescend()) {
 *          //System.out.println("got a candidate");
 *          // We reached a leaf node - process it, and move on
 *         	it.next();
 *     }
 *     else {
 *         // We have not yet reached a leaf, therefore descend
 *         it.descend();
 *     }
 * }
 * 
 * @author raven
 *
 * @param <T>
 */
public class StackCartesianProductIterator<T> implements Iterator<List<T>> {
	private List<? extends Iterable<? extends T>> collections;

	// This list is used as a stack:
	// Iterators from the collections can be pushed and popped
	private List<Iterator<? extends T>> iterators;

	private List<T> current;

	private List<T> result;
	private List<T> resultView;

	private boolean hasNext = true;

	public StackCartesianProductIterator(T[]... collections) {
		List<List<T>> tmp = new ArrayList<List<T>>(collections.length);

		for (T[] item : collections)
			tmp.add(Arrays.asList(item));

		this.collections = tmp;

		init();
	}

	public StackCartesianProductIterator(Iterable<? extends T>... collections) {
		this.collections = Arrays.asList(collections);

		init();
	}

	public StackCartesianProductIterator(
			List<? extends Iterable<? extends T>> collections) {
		this.collections = collections;

		init();
	}

	private void init() {
		List<Integer> indexesOfEmptySubIterables = getIndexesOfEmptySubIterables(collections);
		if(!indexesOfEmptySubIterables.isEmpty()) {
			throw new RuntimeException("Cannot create a iterator when there exists an empty sub-iterables at indexes " + indexesOfEmptySubIterables);
		}
		
		iterators = new ArrayList<Iterator<? extends T>>();
		
		current = new ArrayList<T>();
		result = new ArrayList<T>();

		Iterator<? extends T> it = collections.get(0).iterator();
		iterators.add(it);
		current.add(it.next());
		result.add(null);

		
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
 

		iterators.add(it);
		current.add(it.next());
		result.add(null);
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
				current.set(i, it.next());
				break;
			}
		}

		return resultView;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Operation not supported");
	}
	
    public static void main(String[] args) {
        List<String> a = Arrays.asList(new String[] {"a", "b"});
        List<String> b = Arrays.asList(new String[] {"1", "2", "3"});
        List<String> c = Arrays.asList(new String[] {"this", "is", "a" , "test"});
        List<String> d = Arrays.asList(new String[] {"x", "y"});


        StackCartesianProductIterator<String> it = new StackCartesianProductIterator<String>(a, b, c, d);

        while (it.hasNext()) {

            List<String> current = it.peek();

            if(!isAccepted(current)) {
                it.next();
                continue;
            }
        	System.out.println("Accepted Path: " + current);
        	
            
            if(!it.canDescend()) {
                //System.out.println(current);
            	//System.out.println("got a candidate");
            	it.next();
            }
            else {
            	it.descend();
            }



        }

    }

    public static boolean isAccepted(List<String> list) {
        if(list.contains("y") || list.contains("1")) {
        	return false;
        }
    	
        return true;
    }
	
}