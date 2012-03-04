package org.aksw.changesets;

import java.util.Collection;

/**
 * Allows to treat an ordinary collection as a partition.
 * 
 * Open, Flush, and close have no effect.
 * 
 * @author raven
 *
 * @param <T>
 */
public class PartitionCollection<T>
	extends CollectionDecorator<T, Collection<T>>
	implements Partition<T>
{
	public PartitionCollection(Collection<T> decoratee)
	{
		this.decoratee = decoratee;
	}
	
	@Override
	public void open() {
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}	
}