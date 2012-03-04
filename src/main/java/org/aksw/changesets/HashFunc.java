package org.aksw.changesets;

public interface HashFunc<T>
{
	int hashCode(T item);
}