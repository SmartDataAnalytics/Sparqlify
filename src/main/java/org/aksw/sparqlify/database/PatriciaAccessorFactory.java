package org.aksw.sparqlify.database;

import java.util.List;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

public class PatriciaAccessorFactory
	implements MetaIndexFactory
{
	private Transformer<Object, Set<String>> prefixExtractor;

	
	public PatriciaAccessorFactory(Transformer<Object, Set<String>> prefixExtractor) {
		this.prefixExtractor = prefixExtractor;
	}

	
	@Override
	public MapStoreAccessor create(Table table, List<String> columnNames) {
		
		int[] indexColumns = new int[columnNames.size()];
		
		for(int i = 0; i < indexColumns.length; ++i) {
			String columnName = columnNames.get(i);
			indexColumns[i] = table.getColumns().getIndex(columnName);
		}

		PatriciaPrefixMapStoreAccessor accessor = new PatriciaPrefixMapStoreAccessor(indexColumns, prefixExtractor);
		
		return accessor;
	}
	
}