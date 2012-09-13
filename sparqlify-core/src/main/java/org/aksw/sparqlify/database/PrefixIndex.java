package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.commons.collections15.Transformer;

import com.google.common.collect.Sets;


interface PrefixIndexValue
{
	
}

enum LookupDirection
{
	SHORTER,
	LONGER,
	BOTH;
}


public class PrefixIndex<T>
	implements Index<T>
{

	// The table this index is bound to
	private Table<T> table;
	
	private int[] indexColumns;
	private List<String> indexColumnNames;
	
	private Transformer<T, Set<String>> prefixExtractor;
	
	private NavigableMap<String, Object> map = new TreeMap<String, Object>();

	
	public PrefixIndex(Table<T> table, int[] indexColumns, Transformer<T, Set<String>> prefixExtractor) {
		this.table = table;
		this.indexColumns = indexColumns;
		this.prefixExtractor = prefixExtractor;
		
		this.indexColumnNames = new ArrayList<String>();
		for(int index : indexColumns) {
			this.indexColumnNames.add(table.getColumns().getKey(index));
		}
		
	}
	
	//private Transformer<T, String> transformer;
	
	public void index(String ... row) {
		
	}

	@Override
	public void add(List<? extends T> row) {
		
		NavigableMap<String, Object> current = map;
		
		for(int i = 0; i < indexColumns.length; ++i) {
			boolean isLast = i == indexColumns.length - 1;
			int index = indexColumns[i];
			T value = row.get(index);
			
			Set<String> prefixes = prefixExtractor.transform(value);
			
			for(String prefix : prefixes) {
				Object o = current.get(prefix);
				
				if(isLast) {
					Set<Object> values = (Set<Object>)o;
					if(values == null) {
						values = new HashSet<Object>();
						current.put(prefix, values);
					}
					/*
					else {
						throw new RuntimeException("Duplicate row");
					}*/
					
					values.add(row);
				} else {				
					NavigableMap<String, Object> next = (NavigableMap<String, Object>) o;
					if(next == null) {
						next = new TreeMap<String, Object>();
						
						current.put(prefix, next);
					}
					current = next;
				}
			}
		}
	}
	
	///*protected PrefixMap<PrefixIndexElement>
	
	/**
	 * The constraints given as arguments are interpreted as conjunctions.
	 * The set of prefixes within a constraint is interpreted as a disjunction. 
	 * 
	 * 
	 * @param constraints
	 */
	public void lookup(Constraint ... constraints) {
		/*
		for(VariableConstraintPrefix constraint : constraints) {
			String columnName = constraint.getVariableName();
			
			
		}*/
	}
	
	public Collection<List<T>> lookupSimple(List<String> prefixList)
	{
		List<List<T>> result = new ArrayList<List<T>>();
		lookupSimple(prefixList, 0, map, result);
		return result;
	}

	public Collection<List<T>> lookupSimpleLonger(List<String> prefixList)
	{
		List<List<T>> result = new ArrayList<List<T>>();
		lookupSimpleLonger(prefixList, 0, map, result);
		return result;
	}
	
	public void lookupSimple(List<String> prefixList, int i, Object tmp, Collection<List<T>> result)
	{
		boolean isLast = i == prefixList.size();

		//Entry<String, Object> o;

		if(isLast) {
			lookupRemaining(tmp, i, result);
		} else {
			NavigableMap<String, Object> current = (NavigableMap<String, Object>)tmp;
			//int index = indexColumns[i];
			String prefix = prefixList.get(i);

			Map<String, Object> candidates = StringUtils.getAllPrefixes(prefix, true, current);
			for(Entry<String, Object> entry : candidates.entrySet()) {
				Object next = entry.getValue();
				lookupSimple(prefixList, i + 1, next, result);
			}
		
		}		
	}
	
	public void lookupSimpleLonger(List<String> prefixList, int i, NavigableMap<String, Object> current, Collection<List<T>> result)
	{
		boolean isLast = i == prefixList.size();

		//Entry<String, Object> o;

		if(isLast) {
			lookupRemaining(current, i, result);
		} else {
			int index = indexColumns[i];
			String prefix = prefixList.get(index);

			Map<String, Object> candidates = StringUtils.getAllPrefixedEntries(prefix, true, current);
			for(Entry<String, Object> entry : candidates.entrySet()) {
				NavigableMap<String, Object> next = (NavigableMap<String, Object>) entry.getValue();
				lookupSimpleLonger(prefixList, i + 1, next, result);
			}
		
		}		
	}
	
	
	
	public void lookupRemaining(Object tmp, int i, Collection<List<T>> result) {
		// Add everything that is left
		boolean isLast = i == indexColumns.length - 1;
		int index = indexColumns[i];
			
		if(isLast) {
			Set<List<T>> current = (Set<List<T>>)tmp;

			result.addAll(current);
			/*
			for(Object o : current) {				
				Set<List<T>> rows = (Set<List<T>>) o;
				//List<T> row = (List<T>) o;
				result.addAll(rows);
			}*/
		} else {
			NavigableMap<String, Object> current = (NavigableMap<String, Object>)tmp; 
			
			for(Object o : current.values()) {
				NavigableMap<String, Object> next = (NavigableMap<String, Object>) o;
				
				lookupRemaining(next, i + 1, result);
			}
		}
	}

	public Collection<List<T>> lookup(List<Set<String>> prefixesList) {
		List<List<T>> result = new ArrayList<List<T>>();
		
		lookup(prefixesList, 0, map, result);
		
		return result;
	}

	public void lookup(List<Set<String>> prefixesList, int i, NavigableMap<String, Object> current, Collection<List<T>> result) {

		boolean isLast = i == indexColumns.length - 1;
		int index = indexColumns[i];
		Set<String> prefixes = prefixesList.get(index);

		for(String prefix : prefixes) {
			Object o = current.get(prefix);
			
			if(isLast) {
				if(o == null) {
					return;
				}
				
				List<T> row = (List<T>) o;
				
				result.add(row);
			} else {				
				NavigableMap<String, Object> next = (NavigableMap<String, Object>) o;
				if(next == null) {
					return;
				}
				
				lookup(prefixesList, i + 1, next, result);
			}
		}

			
	}
	
	
	public static <T> PrefixIndex<T> attach(Transformer<T, Set<String>> prefixExtractor, Table<T> table, String ... columnNames) {
		List<String> names = Arrays.asList(columnNames);
		
		Set<String> nameSet = new HashSet<String>(names);		
		Set<String> dangling = Sets.difference(nameSet, table.getColumns().keySet());
		if(!dangling.isEmpty()) {
			throw new RuntimeException("Columns " + dangling + " referenced, but not present in table");
		}
		
		int[] indexColumns = new int[columnNames.length];
		for(int i = 0; i < columnNames.length; ++i) {
			Integer index = table.getColumns().getIndex(columnNames[i]);
			if(index == null) {
				throw new NullPointerException("Column name does not have an index");
			}
			indexColumns[i] = index;
		}
		

		PrefixIndex<T> index = new PrefixIndex<T>(table, indexColumns, prefixExtractor);
		
		table.addIndex(index);
		
		return index;
	}

	@Override
	public boolean preAdd(List<? extends T> row) {
		return true;
	}

	@Override
	public void postAdd(List<? extends T> row) {
	}

	@Override
	public Table<T> getTable() {
		return table;
	}

	@Override
	public int[] getIndexColumns() {
		return indexColumns;
	}

	@Override
	public List<String> getIndexColumnNames() {
		return indexColumnNames; 
	}

	@Override
	public String toString() {
		return "PrefixIndex [table=" + table + ", indexColumnNames="
				+ indexColumnNames + "]";
	}

	@Override
	public IndexMetaNode getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getStore() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
