package org.aksw.changesets;

/**
 * Allows one to delegate calls to the underlying partition.
 * 
 * Useful for switching from an in-memory backend to a persistent one.
 * 
 * @author raven
 *
 * @param <T>
 */
public class PartitionDecorator<T>
	extends CollectionDecorator<T, Partition<T>>
	implements Partition<T>
{
	@Override
	public void open() {
		this.decoratee.open();
	}

	@Override
	public void flush() {
		this.decoratee.flush();
	}

	@Override
	public void close() {
		this.decoratee.close();
	}
}