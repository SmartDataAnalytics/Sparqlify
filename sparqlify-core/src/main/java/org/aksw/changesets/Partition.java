package org.aksw.changesets;

import java.util.Collection;

public interface Partition<T>
	extends Collection<T>
{
	void open();
	
	//void add(T item);

	void flush();
	void close();

	//void clear();
	
	//Iterable<T> getData();
	//int size();
}