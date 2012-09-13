package org.aksw.changesets;

import java.util.ArrayList;

/**
 * A Partition that switches from an in-memory backend
 * to a persistent one, once the in-memory partition becomes full.
 * 
 * @author raven
 *
 * @param <T>
 */
public class BackedPartition<T>
	extends PartitionDecorator<T>
{
	private int maxPartitionSize = 10000;
	private boolean usesPersistentBackend = false;

	// PartitionFactory for the backend
	private PartitionFactory<T> partitionFactory;

	
	private Partition<T> inMemory = new PartitionCollection<T>(new ArrayList<T>());
	
	public BackedPartition(PartitionFactory<T> partitionFactory)
	{
		this.decoratee = inMemory;
		this.partitionFactory = partitionFactory;
	}
	
	@Override
	public boolean add(T item) {
		if(decoratee.size() > maxPartitionSize && !usesPersistentBackend) {
			makePersistent();
		}

		return decoratee.add(item);
	}
	
	/**
	 * Clearing switches back to in-memory mode
	 * 
	 */
	@Override
	public void clear()
	{
		decoratee.clear();
		inMemory.clear();
		decoratee = inMemory;
	}
	
	public void makePersistent()
	{
		Partition<T> persist = partitionFactory.create();
		
		for(T x : decoratee) {
			persist.add(x);
		}
		
		decoratee.clear();

		decoratee = persist;
		usesPersistentBackend = true;		
	}
}