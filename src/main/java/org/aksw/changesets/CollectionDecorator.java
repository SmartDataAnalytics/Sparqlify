package org.aksw.changesets;

import java.util.Collection;
import java.util.Iterator;

public class CollectionDecorator<T, U extends Collection<T>>
	implements Collection<T>
{
	protected U decoratee;

	public U getDecoratee()
	{
		return decoratee;
	}
	
	@Override
	public boolean add(T item) {
		return decoratee.add(item);
	}

	@Override
	public boolean addAll(Collection<? extends T> items) {
		return decoratee.addAll(items);
	}

	@Override
	public void clear() {
		decoratee.clear();
	}

	@Override
	public boolean contains(Object item) {
		return decoratee.contains(item);
	}

	@Override
	public boolean containsAll(Collection<?> items) {
		return decoratee.containsAll(items);
	}

	@Override
	public boolean isEmpty() {
		return decoratee.isEmpty();
	}

	@Override
	public Iterator<T> iterator() {
		return decoratee.iterator();
	}

	@Override
	public boolean remove(Object item) {
		return decoratee.remove(item);
	}

	@Override
	public boolean removeAll(Collection<?> items) {
		return decoratee.removeAll(items);
	}

	@Override
	public boolean retainAll(Collection<?> items) {
		return decoratee.retainAll(items);
	}

	@Override
	public int size() {
		return decoratee.size();
	}

	@Override
	public Object[] toArray() {
		return decoratee.toArray();
	}

	@Override
	public <V> V[] toArray(V[] a) {
		return decoratee.toArray(a);
	}
}