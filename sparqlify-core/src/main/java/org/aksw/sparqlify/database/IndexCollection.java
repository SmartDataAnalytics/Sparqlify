package org.aksw.sparqlify.database;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


class IndexCandidate {
	IndexMetaNode node;
	int columnDepth;
	int nodeDepth;
	
	int childIndex;
	IndexCandidate child;
	List<Constraint> constraints;
	//Set<String> noRecheckColumns = new HashSet<String>(); // Column names that do not need a recheck thanks to the index
	
	public IndexCandidate(IndexMetaNode node, int columnDepth, int nodeDepth, int childIndex, List<Constraint> constraints) {
		this.node = node;
		this.columnDepth = columnDepth;
		this.nodeDepth = nodeDepth;
		this.constraints = constraints;
		this.childIndex = childIndex;
	}

	@Override
	public String toString() {
		return "IndexCandidate [node=" + node + ", columnDepth=" + columnDepth
				+ ", nodeDepth=" + nodeDepth + ", childIndex=" + childIndex
				+ ", child=" + child + ", constraints=" + constraints + "]";
	}
}



class CollectionComparator<T>
	implements Comparator<Collection<T>>
{

	@Override
	public int compare(Collection<T> o1, Collection<T> o2) {
		// TODO Auto-generated method stub
		return 0;
	}
}


class SubList<T>
	extends AbstractList<T>
{
	private List<T> original;
	private int beginIndex;
	private int endIndex;
	
	public SubList(List<T> original, int beginIndex) {
		this.original = original;
		this.beginIndex = beginIndex;
		this.endIndex = original.size();
	}
	
	public SubList(List<T> original, int beginIndex, int endIndex) {
		this.original = original;
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
	}
	
	public List<T> getOriginal() 
	{
		return original;
	}
	
	@Override
	public T get(int index) {
		return original.get(beginIndex + index);
	}

	@Override
	public int size() {
		return endIndex - beginIndex;
	}
	
	@Override
	public SubList<T> subList(int beginIndex, int endIndex) {
		return new SubList<T>(original, this.beginIndex + beginIndex, this.beginIndex + beginIndex + endIndex);
	}
	
	//@Override
	public SubList<T> subList(int beginIndex) {
		return new SubList<T>(original, this.beginIndex + beginIndex, this.beginIndex + beginIndex + this.endIndex);
	}
	
	
	//public static SubList<T> create(List<T> original, )
}

class TrieMap<K, V>
{
	class Node {
		SubList<K> transition;
		Map<K, Node> map;
		V value;
		boolean isLeaf;
		
		public Node()
		{
			isLeaf = true;
		}
		
		public Node(SubList<K> transition, V value) {
			this.transition = transition;
			this.value = value;
			this.isLeaf = true;
		}
	}
	

	private Node root = new Node();
	
	public void add(List<K> key, V value) {
		Node current = root;
		
		
	}
	
	
	public void add(Node current, int beginIndex, List<K> key, V value) {
		
		if(current.transition != null) {
			for(int j = 0; j < current.transition.size(); ++j) {
				K t = current.transition.get(j);
				K k = key.get(beginIndex + j);
				
				if(!t.equals(k)) {
					// We need to split
					
					Node branchOld = new Node();
					branchOld.map = current.map;
					branchOld.transition = current.transition.subList(j);
					
					
					Node branchNew = new Node();
					branchNew.transition = new SubList<K>(key, j);
					branchNew.isLeaf = true;
					branchNew.value = value;
					
					
					current.transition = current.transition.subList(0, j);
					current.map = new HashMap<K, Node>();
					current.map.put(t, branchOld);
					current.map.put(k, branchNew);
				}
				
			}
		}
	}
		
		/*
		int i;
		for(K k : key) {			

			
			Node o = current.get(k);
			
			if(o == null) {
				
				Leaf leaf = new Leaf(new SubList<K>(key, i), value);
				current.put(k, leaf);
				
			} else if (o instanceof Map) {

				Map<K, Object> next = (Map<K, Object>)o;
				current = next;

			} else if (o instanceof TrieMap.Leaf) {
				
				Leaf leaf = (Leaf)o;
				
				
				
				
			} else {
				throw new RuntimeException("Should not happen");
			}
			
			++i;
		}
	}
	
	
	public V getNearestValue(List<K> key) {
		Node current = root;
		
		int i = 0;
		for(K k : key) {
			if(current.transition != null) {
				
			}
			
			++i;
		}
		
	}
*/
}



/**
 * Checks whether the columns of a row satisfy a set of constraints
 * 
 * @author raven
 *
 */
class RowRechecker
// implements Filter<List<Object>>
{
	private Map<Integer, Constraint> constraints;
	
	public RowRechecker(Map<Integer, Constraint> constraints)
	{
		this.constraints = constraints;
	}
	
	public boolean isAccepted(List<Object> row) {
		

		for(Entry<Integer, Constraint> entry : constraints.entrySet()) {
			Object value = row.get(entry.getKey());
			boolean isSatisfied = entry.getValue().isSatisfiedBy(value);
			if(!isSatisfied) {
				return false;
			}
		}
		
		return true;
	}
}


public class IndexCollection<T>
	extends AbstractCollection<Index<T>>
{

	private List<Index<T>> indexes = new ArrayList<Index<T>>();
	
	
	@Override
	public boolean add(Index<T> index) {
		return indexes.add(index);
	}
	
	
	public void getRows(Map<String, Constraint> constraints, IndexMetaNode node, List<Integer> path) {
	}
	
	/**
	 * 
	 * 
	 * 
	 * 
	 * @param columnNames
	 * @return
	 */
	public Collection<List<Object>> get(Map<String, Constraint> constraints, IndexMap<String, Column> columns) {
		
		//Set<String> columnNames = constraints.keySet();
		
		Index<T> bestMatch = null;
		IndexCandidate bestCandidate = null;
		
		
		for(Index<T> index : indexes) {
			
			IndexCandidate candidate  = get(Collections.singletonList(index.getRoot()), 0, 0, constraints);

			if(candidate == null) {
				continue;
			}
			
			if(bestCandidate == null ||
					(candidate.columnDepth > bestCandidate.columnDepth) ||
					(candidate.columnDepth == bestCandidate.columnDepth && candidate.nodeDepth < bestCandidate.nodeDepth)) {
				bestCandidate = candidate;
				bestMatch = index;
			}
		}
				
		//return bestMatch;
		
		// Determine which constraints are not covered by the index
		Map<String, Constraint> recheck = new HashMap<String, Constraint>(constraints);
		IndexCandidate node = bestCandidate;
		while(node != null) {
			for(String columnName : node.node.getColumnNames()) {
				recheck.remove(columnName);
			}
			
			node = node.child;
		}
		
		RowRechecker rechecker = null;
		
		if(!recheck.isEmpty()) {
			Map<Integer, Constraint> recheckIndex = new HashMap<Integer, Constraint>();
			
			for(Entry<String, Constraint> entry : recheck.entrySet()) {
				int index = columns.getIndex(entry.getKey());
				
				recheckIndex.put(index, entry.getValue());
			}
			
			rechecker = new RowRechecker(recheckIndex);
		}
		
		
		if(bestMatch == null) {
			if(indexes.isEmpty()) {
				throw new RuntimeException("No store or index found for lookup.");
			} else {
				bestMatch = indexes.iterator().next();
			}
		}
		
		
		Collection<List<Object>> result = execute(bestMatch, bestCandidate, rechecker);
		return result;
	}
	

	public Collection<List<Object>> execute(Index index, IndexCandidate pathNode, RowRechecker rechecker) {
		Set<List<Object>> result = new HashSet<List<Object>>();
		
		if(pathNode == null) {
			collectRows(index.getStore(), Collections.singleton(index.getRoot()), result, rechecker);
		} else {		
			_execute(index.getStore(), pathNode, result, rechecker);
		}
		
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public void _execute(Object store, IndexCandidate pathNode, Collection<List<Object>> result, RowRechecker rechecker)
	{
		IndexMetaNode node = pathNode.node;
		MapStoreAccessor accessor = node.getFactory(); 

		Collection<Object> values = accessor.lookup(store, pathNode.constraints.iterator().next());

		
		if(pathNode.child == null) {
			// values could be e.g. List< List< TreeMap< ... > > > (list of lookup, list for nodes,  store for each node)
			for(Object value : values) {
				collectRows(value, node.getChildren(), result, rechecker);
			}
		} else {

		
			switch(node.getChildren().size()) {
			case 0: // No children, the value is a set of rows
				for(Object value : values) {
					List<List<Object>> rows = (List<List<Object>>)value;
					
					if(rows != null) {
						result.addAll(rows);
					}
				}
				break;
			
			case 1: // 1 child, the value is a map
				for(Object value : values) {
					_execute(value, pathNode.child, result, rechecker);
				}
				break;
	
			default: // The value is a list of stores
				for(Object value : values) {
					List<Object> stores = (List<Object>)value;
					
					_execute(stores.get(pathNode.childIndex), pathNode.child, result, rechecker);
				}
				break;			
			}
		}
	}
	
	private void collectRows(Object store, Collection<IndexMetaNode> nodes, Collection<List<Object>> result, RowRechecker rechecker) {
		
		//MapStoreAccessor accessor = node.getFactory().getAccessor(); 
		//Collection<Object> values = accessor.list(store);

		
		switch(nodes.size()) {
		case 0: // No children, the value is a set of rows
			Collection<List<Object>> rows = (Collection<List<Object>>)store;
			
			if(rows != null) {
				if(rechecker == null) {
					result.addAll(rows);
				} else {
					for(List<Object> row : rows) {
						if(rechecker.isAccepted(row)) {
							result.add(row);
						}
					}
				}
			}
			break;
		
		case 1: // 1 child, the value is a map
			IndexMetaNode subNode = nodes.iterator().next();
			Collection<Object> values = subNode.getFactory().list(store);

			for(Object value : values) {
				collectRows(value, subNode.getChildren(), result, rechecker);
			}
			break;

		default: // Each value is a list of stores, just take the first node
			List<Object> stores = (List<Object>)store;

			IndexMetaNode subNodeN = nodes.iterator().next();
			Collection<Object> valuesN = subNodeN.getFactory().list(stores.get(0));

			for(Object value : valuesN) {
				collectRows(value, subNodeN.getChildren(), result, rechecker);
			}
			break;			
		}
	}
	
	
	/*
	class IndexPathNode {
		List<Constraint> constraints;
		int childIndex;
		IndexPathNode childNode;
	}*/
	


	
	
	// Pick the node that
	// .) Covers most columns
	// .) Uses least index nodes
	/**
	 * TODO Given {a = const , b = const} and an index (a, b) with support for equals.
	 * 
	 * How can we pass on the constraints???
	 * 
	 * For now lets assume only single column indexes... :/
	 * 
	 * 
	 * @param nodes
	 * @param baseNodeDepth
	 * @param baseColumnDepth
	 * @param constraints
	 * @return
	 */
	public IndexCandidate get(List<IndexMetaNode> nodes, int baseNodeDepth, int baseColumnDepth, Map<String, Constraint> constraints) {
		Set<String> columnNames = constraints.keySet();
		
		//IndexMetaNode bestMatch = null;
		IndexCandidate bestCandidate = null;
		
		int childIndex = 0;
		for(IndexMetaNode node : nodes) {
			int columnDepth = baseNodeDepth;
			int nodeDepth = baseColumnDepth;

			
			List<String> idxColNames = node.getColumnNames();
			
			if(!columnNames.containsAll(idxColNames)) {
				continue;
			}
			
			Set<Class<?>> requiredConstraintClasses = new HashSet<Class<?>>();
			for(String idxColName : idxColNames) {
				requiredConstraintClasses.add(constraints.get(idxColName).getClass());
			}
			
			if(!node.getFactory().getSupportedConstraintClasses().containsAll(requiredConstraintClasses)) {
				continue;
			}
			
			// Pack the index with the constraint
			List<Constraint> cs = new ArrayList<Constraint>(idxColNames.size());
			for(String idxColName : idxColNames) {
				cs.add(constraints.get(idxColName));
			}
			
			
			columnDepth += idxColNames.size();
			nodeDepth += 1;
			
			IndexCandidate thisCandidate = new IndexCandidate(node, columnDepth, nodeDepth, childIndex, cs);
			
			/*
			if(bestCandidate == null ||
					(columnDepth > bestCandidate.columnDepth) ||
					(columnDepth == bestCandidate.columnDepth && nodeDepth < bestCandidate.nodeDepth)) {
				bestCandidate = thisCandidate;				
			}*/				

			if(!node.getChildren().isEmpty()) {
				IndexCandidate bestChild = get(node.getChildren(), nodeDepth, columnDepth, constraints);
				if(bestChild != null) {				
					thisCandidate.columnDepth = bestChild.columnDepth;
					thisCandidate.nodeDepth = bestChild.nodeDepth;							
					thisCandidate.child = bestChild;
				}
			}

			if(bestCandidate == null ||
					(thisCandidate.columnDepth > bestCandidate.columnDepth) ||
					(thisCandidate.columnDepth == bestCandidate.columnDepth && thisCandidate.nodeDepth < bestCandidate.nodeDepth)) {
				//bestChild = candidate;
				bestCandidate = thisCandidate;
			}
			
			
			++childIndex;
		}
		
		return bestCandidate;
	}
	

	@Override
	public Iterator<Index<T>> iterator() {
		return indexes.iterator();
	}

	@Override
	public int size() {
		return indexes.size();
	}
}



