package org.aksw.changesets;

public interface PartitionFactory<T>
{
	public Partition<T> create() ;
}