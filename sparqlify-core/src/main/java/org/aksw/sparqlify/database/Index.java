package org.aksw.sparqlify.database;

import java.util.List;

public interface Index<T> {
	Table<T> getTable();
	
	
	
	// These kind of methods are not suitable for hierarchical indexes. 
	@Deprecated
	int[] getIndexColumns();
	
	@Deprecated
	List<String> getIndexColumnNames();
	
	// A node with metadata about the index
	IndexMetaNode getRoot();
	
	Object getStore();
	
	
	/**
	 * Only table should call this.
	 * Return true to accept the row, false to reject it
	 * 
	 * @param row
	 * @return
	 */
	boolean preAdd(List<? extends T> row);
	void add(List<? extends T> row);
	void postAdd(List<? extends T> row);
}
