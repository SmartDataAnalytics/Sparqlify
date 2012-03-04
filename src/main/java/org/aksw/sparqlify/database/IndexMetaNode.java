package org.aksw.sparqlify.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Datastructure for metadata about indexes (hierarchical and non-hierarchical).
 * 
 * Important: Do not modify this structure after attaching it to a table, or
 * behavior is undefined.
 * 
 * 
 * @author Claus Stadler <cstadler@informatik.uni-leipzig.de>
 *
 * @param <T>
 */
public class IndexMetaNode {
	private IndexMetaNode parent;

	private Table table;
	private List<String> columnNames;
	
	// NOTE: The store factory provides the supported constraint classes
	private MapStoreAccessor factory;
	
	
	//private MetaIndexFactory factory;
	private Map<List<String>, List<IndexMetaNode>> subIndexes = new HashMap<List<String>, List<IndexMetaNode>>();
	private List<IndexMetaNode> children = new ArrayList<IndexMetaNode>();
	
	public List<IndexMetaNode> getChildren() {
		return children;
	}
	
	public Map<List<String>, List<IndexMetaNode>> getSubIndexes() {
		return subIndexes;
	}

	public IndexMetaNode(Table table, List<String> columnNames, MapStoreAccessor factory) {
		this.parent = null;
		this.table = table;
		this.columnNames = columnNames;
		this.factory = factory;
	}
	
	public IndexMetaNode(IndexMetaNode parent, List<String> columnNames, MapStoreAccessor factory) {
		this.parent = parent;
		this.table = parent.getTable();
		this.columnNames = columnNames;
		this.factory = factory;
		
		
		List<IndexMetaNode> list = parent.subIndexes.get(columnNames);
		if(list == null) {
			list = new ArrayList<IndexMetaNode>();
			parent.subIndexes.put(columnNames, list);
		}
		list.add(this);
		parent.children.add(this);
	}

	
	public IndexMetaNode getParent() {
		return parent;
	}


	public Table getTable() {
		return table;
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}
		
	public MapStoreAccessor getFactory() {
		return factory;
	}
	
	/*
	public Map<List<String>, List<IndexMetaNode>> getSubIndexes() {
		return subIndexes;
	}*/
	
	public void addSubIndex(IndexMetaNode subIndex) {
		
		//subIndexes.put(subIndex.getColumnNames());
	}
	
	
	public static IndexMetaNode create(Table table, MetaIndexFactory factory, String ... columns)
	{
		MapStoreAccessor f = factory.create(table, Arrays.asList(columns));
		return new IndexMetaNode(table, Arrays.asList(columns), f);
	}
	
	public static IndexMetaNode create(IndexMetaNode parent, MetaIndexFactory factory, String ... columns) {
		MapStoreAccessor f = factory.create(parent.getTable(), Arrays.asList(columns));
		return new IndexMetaNode(parent, Arrays.asList(columns), f);
	}

	@Override
	public String toString() {
		return "IndexMetaNode [columnNames=" + columnNames + "]";
	}
	
	
}