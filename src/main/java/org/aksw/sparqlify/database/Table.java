package org.aksw.sparqlify.database;

import java.util.Collection;
import java.util.List;
import java.util.Map;





/**
 * A table itself only provides metadata. A storage (such as an index) must be attached to it for that.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 * @param <T>
 */
public interface Table<T> {
	IndexMap<String, Column> getColumns();
	
	//Trasformer<T, List<Object>> getBeanToRowTransformer();
	
	//public void addIndex(Index<T> index);
	
	//Collection<Index<T>> getIndexes();
	IndexCollection<T> getIndexes();
	
		
	void add(List<? extends T> row);
	void addIndex(Index<T> index);
	
	
	Collection<List<Object>> select(Map<String, Constraint> constraints);
	
	int[] getIndexes(List<String> columnNames);
}
