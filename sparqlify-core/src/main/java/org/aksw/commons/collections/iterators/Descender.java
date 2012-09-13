package org.aksw.commons.collections.iterators;

import java.util.Collection;

public interface Descender<T>
{
	Collection<T> getDescendCollection(T item);
}