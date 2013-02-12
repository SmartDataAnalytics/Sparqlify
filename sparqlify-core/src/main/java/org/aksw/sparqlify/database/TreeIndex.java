package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;










/*
class ListIndexMetaFactory
	implements MetaIndexFactory
{
	public static Set<Class<?>> supportedConstraintClasses = new HashSet<Class<?>>();
	
	static
	{
		supportedConstraintClasses.add(PrefixConstraint.class);
	}
	
	public Set<Class<?>> getSupportedConstraintClasses() {
		return supportedConstraintClasses;
	}
	
	
	private Transformer<Object, Set<String>> prefixExtractor;
	
	public ListIndexMetaFactory(Transformer<Object, Set<String>> prefixExtractor) {
		this.prefixExtractor = prefixExtractor;
	}
	
	@Override
	public MapStoreAccessor create(Table table) {
		
		int[] indexColumns = new int[];
		
		for(int i = 0; i < indexColumns.length; ++i) {
			String columnName = columnNames.get(i);
			indexColumns[i] = table.getColumns().getIndex(columnName);
		}
	
		PrefixMapStoreAccessor accessor = new PrefixMapStoreAccessor(indexColumns, prefixExtractor);
		
		return accessor;
	}
	
}
*/


/*
interface MapStoreFactory
{
	MapStoreAccessor getAccessor();
	Object createStore();
}


class PrefixMapStoreFactory
	implements MapStoreFactory
{
	private PrefixMapStoreAccessor accessor;

	public PrefixMapStoreFactory(PrefixMapStoreAccessor accessor) {
		this.accessor = accessor;
	}
		
	public Object createStore() {
		return new TreeMap<String, Object>();
	}
	
	public MapStoreAccessor getAccessor() {
		return accessor;
	}	
}
*/





/**
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
interface Index2 {
	Table getTable();
	
	Index2 getParent(); // null if there is no parent index
	List<String> getColumnNames();
	
	Set<Class<?>> getSupportedConstraintClasses();
	
	List<?> lookup(List<?> keys);
}

class IndexBase {
	protected Table table;
	protected List<String> columnNames;
	
	public Table getTable() {
		return table;
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}
}


class IndexNode {
	//Map<ColumnName, SubIndex> ;
	
	//Map<String, IndexNode> columnNameToSubIndex; // schemaLevel
		
	
}




/*
class ColumnIndexPair
{
	private List<String> columns;
	private List<MapStoreFactory> factory;
}*/


/**
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 */
/*
class TreeIndexBuilder {
	private List<ColumnIndexPair> cis = new ArrayList<ColumnIndexPair>();
	
	public IndexMetaNode add(String ... columns) {
		
	}
	
	public Index create() {
		
	}
}
*/



public class TreeIndex
	extends AbstractIndex
{
	private IndexMetaNode root;
	private Object store;


	public TreeIndex(Table table, IndexMetaNode root) 
	{
		super(table, null, null);
		this.root = root;
		this.store = root.getFactory().createStore();
	}

	public Object getStore() {
		return store;
	}
	
	
	@Override
	public void add(List row) {
		add(row, root, store);
	}
	
	private void add(List row, List<IndexMetaNode> nodes, List<Object> stores) {

		Iterator<IndexMetaNode> itNode = nodes.iterator();
		Iterator<Object> itStore = stores.iterator();

		while(itNode.hasNext()) {
			IndexMetaNode node = itNode.next();
			Object store = itStore.next();
			
			add(row, node, store);
		}
	}
	
	/**
	 * Adds a row to the index. Depending on the type of node
	 * tree, different datastructures are used.
	 * 
	 * 
	 * @param row
	 * @param node
	 * @param store
	 */
	private void add(List<Object> row, IndexMetaNode node, Object store)
	{
		List<IndexMetaNode> children = node.getChildren();
		MapStoreAccessor accessor = node.getFactory(); 

		Object value = accessor.get(store, row);
		
		switch(children.size()) {
		case 0:
			List<List<Object>> rows = (List<List<Object>>)value;
			if(rows == null) {
				rows = new ArrayList<List<Object>>();
				accessor.put(store, row, rows);
			}
			
			rows.add(row);
			break;

		case 1:
			Object subStore = value;
			IndexMetaNode subNode = node.getChildren().iterator().next();
			if(subStore == null) {
				subStore = subNode.getFactory().createStore();
				accessor.put(store, row, subStore);
			}
			
			add(row, subNode, subStore);
			break;
			
		default:
			List<Object> subStores = (List<Object>)value;
			if(subStores == null) {
				subStores = new ArrayList<Object>(children.size());
				
				for(IndexMetaNode child : children) {
					subStores.add(child.getFactory().createStore());
				}
			
				accessor.put(store, row, subStores);
			}
			
			add(row, children, subStores);
			break;
		}			
	}
	
	
	/*
	private void put() {
		NavigableMap<String, Object> current = map;
		
		for(int i = 0; i < indexColumns.length; ++i) {
			boolean isLast = i == indexColumns.length - 1;
			int index = indexColumns[i];
			Object value = row.get(index);
			
			Set<String> prefixes = prefixExtractor.transform(value);
			
			for(String prefix : prefixes) {
				Object o = current.get(prefix);
				
				if(isLast) {
					if(o == null) {
						current.put(prefix, row);
					} else {
						throw new RuntimeException("Duplicate row");
					}
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

	}*/

	@Override
	public IndexMetaNode getRoot() {
		return root;
	}


	
	
	/*
	List<IndexMetaNode> subMetaIndexes;
	
	TreeMap<Object, IndexBase> map = new TreeMap<Object, IndexBase>();
*/
	
	public static TreeIndex attach(Table table, IndexMetaNode node) {

		TreeIndex index = new TreeIndex(table, node);
		
		table.addIndex(index);
		
		return index;
	}

	@Override
	public String toString() {
		return "TreeIndex [root=" + root + ", store=" + store + "]";
	}
}
