package org.aksw.changesets;

public class DefaultHashFunc<T>
	implements HashFunc<T>
{
	@Override
	public int hashCode(T item) {
		return item.hashCode();
	}
}