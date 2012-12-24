package sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.commons.collections.MapUtils;
import org.aksw.commons.collections.multimaps.IBiSetMultimap;
import org.aksw.commons.collections.multimaps.ISetMultimap;
import org.aksw.commons.jena.util.QuadUtils;
import org.apache.commons.lang.NotImplementedException;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.sparql.core.Var;

class NodeV {
	private Node node;
	private int id;
	public NodeV(Node node, int id)
	{
		super();
		this.node = node;
		this.id = id;
	}
	public Node getNode()
	{
		return node;
	}
	public int getId()
	{
		return id;
	}
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeV other = (NodeV) obj;
		if (id != other.id)
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}
	@Override
	public String toString()
	{
		return node + "(" + id +")";
	}
	

}


/**
 * In its current state this class only hides the copying that should be
 * avoided by a view
 * 
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
class UnionBiMultiMap<K, V>
	implements IBiSetMultimap<K, V>
{
	private Collection<IBiSetMultimap<K, V>> wrapped;
	private UnionBiMultiMap<V, K> inverse;
	

	public static <K, V> UnionBiMultiMap<K, V> create(IBiSetMultimap<K, V> a, IBiSetMultimap<K, V> b)
	{
		Collection<IBiSetMultimap<K, V>> union = new ArrayList<IBiSetMultimap<K, V>>();
		union.add(a);
		union.add(b);
		
		return new UnionBiMultiMap<K, V>(union);
	}
	
	public UnionBiMultiMap(Collection<IBiSetMultimap<K, V>> wrapped)
	{
		this.wrapped = wrapped;
		inverse = new UnionBiMultiMap<V, K>(this);
	}
	
	protected UnionBiMultiMap(UnionBiMultiMap<V, K> original)
	{
		this.inverse = original;
		
		wrapped = new ArrayList<IBiSetMultimap<K, V>>();
		for(IBiSetMultimap<V, K> item : original.wrapped) {
			wrapped.add(item.getInverse());
		}
	}
	
	/**
	 * Making a copy of the values is just what we want to avoid by doing all
	 * that stuff... guava doesn't have a union multimap view it seems
	 * But too much work to implement properly for now. 
	 * 
	 */
	private SetMultimap<K, V> copyAll()
	{
		SetMultimap<K, V> copy = HashMultimap.create();
		
		for(IBiSetMultimap<K, V> item : wrapped) {
			copy.asMap().putAll(item.asMap());
		}
		
		return copy;
	}
	
	@Override
	public Map<K, Collection<V>> asMap()
	{
		return copyAll().asMap();
	}

	@Override
	public boolean put(K key, V value)
	{
		throw new NotImplementedException();
	}

	@Override
	public Set<V> removeAll(Object key)
	{
		throw new NotImplementedException();
	}

	
	// TODO This should be a view
	@Override
	public Set<V> get(Object key)
	{
		Set<V> result = new HashSet<V>();

		for(IBiSetMultimap<K, V> item : wrapped) {
			result.addAll(item.get(key));
		}
		
		return result;
	}

	@Override
	public IBiSetMultimap<V, K> getInverse()
	{
		return inverse;
	}

	@Override
	public Set<Entry<K, V>> entries()
	{
		return copyAll().entries();
	}

	@Override
	public void putAll(ISetMultimap<K, V> other)
	{
		throw new NotImplementedException();		
	}

	@Override
	public void clear()
	{
		throw new NotImplementedException();		
	}

	@Override
	public boolean containsEntry(Object key, Object value) {
		throw new NotImplementedException();		
	}

	@Override
	public boolean containsValue(Object value) {
		throw new NotImplementedException();		
	}

	@Override
	public boolean containsKey(Object key) {
		throw new NotImplementedException();		
	}

	@Override
	public int size() {
		throw new NotImplementedException();		
	}

	@Override
	public void putAll(K key, Collection<V> values) {
		for(V value : values) {
			put(key, value);
		}
	}

	@Override
	public Set<K> keySet() {
		throw new NotImplementedException();
	}

	@Override
	public Collection<K> keys() {
		throw new NotImplementedException();
	}
}


public class TwoWayBinding
{	
	private EquiMap<Var, Node> equiMap = new EquiMap<Var, Node>();
	
	/**
	 * Returns union of the keySets of
	 * equiMap.getEquivalences and equiMap.getKeyToValue
	 * 
	 * @return
	 */
	public Set<Var> keySet() {
		return Sets.union(equiMap.getEquivalences().asMap().keySet(), equiMap.getKeyToValue().keySet()); 
	}

	/**
	 * Copies the map.
	 * Leave null for a deep copy
	 * 
	 * @param map
	 * @return
	 */
	public TwoWayBinding copySubstitute(Map<? extends Node, Node> map)
	{
		//EquiMap<Var, Node>  result = new EquiMap<Var, Node>();
		TwoWayBinding result = new TwoWayBinding();
		
		// Copy equivalences
		for(Entry<Var, Collection<Var>> entry : equiMap.getEquivalences().asMap().entrySet()) {
			
			Var newKey = map == null
					? entry.getKey()
					: (Var)MapUtils.getOrElse(map, entry.getKey(), entry.getKey());
			
			for(Var item : entry.getValue()) {
				Var newValue = map == null
						? item
						:(Var)MapUtils.getOrElse(map, item, item);
				
				result.getEquiMap().getEquivalences().put(newKey, newValue);
			}
		}
		

		// Copy to values
		for(Entry<Var, Node> entry : equiMap.getKeyToValue().entrySet()) {
			Var newKey = map == null
					? entry.getKey()
					: (Var)MapUtils.getOrElse(map, entry.getKey(), entry.getKey());

			Node newValue = map == null
					? entry.getValue()
					: MapUtils.getOrElse(map, entry.getValue(), entry.getValue());
			
			result.getEquiMap().getKeyToValue().put(newKey, newValue);			
		}
		
		
		
		
		return result;
	}
	
	public TwoWayBinding()
	{
		super();
	}

	public void clear()
	{
		equiMap.clear();
	}
	
	/*
	public boolean isConsistent()
	{
		for(NodeV key : equiMap.getKeyToValue().keySet()) {
			if(equiMap.get(key).size() > 1) {
				return false;
			}
		}
		
		return true;
	}
	*/
	
	
	public EquiMap<Var, Node> getEquiMap()
	{
		return equiMap;
	}
	
	/*
	public boolean isConsistent(NodeV a, NodeV b)
	{
		return Sets.union(equiMap.get(a), equiMap.get(b)).size() <= 1;
	}
	
	public boolean makeEqual(NodeV a, NodeV b)
	{
		if(isConsistent(a, b)) {
			equiMap.makeEqual(a, b);
			return true;
		}
		
		return false;
	}
	*/

	/*
	public boolean isInsertConsistent(Var a, Node b)
	{
		Set<Node> values = equiMap.get(a);
		
		if(values.size() > 1) {
			throw new RuntimeException("Should not happen");
		}
		
		return equiMap.isConsistentSet(values);		
	}*/
	
	public boolean put(Var a, Node b)
	{
		if(equiMap.isConsistentInsertValue(a, b)) {
			equiMap.put(a, b);
			return true;
		}
		
		return false;
	}
	
	public boolean makeEqual(Var a, Var b)
	{
		if(equiMap.isConsistentInsertEquiv(a, b)) {
			equiMap.makeEqual(a, b);
			return true;
		}
		
		return false;
	}

	
	
	/**
	 * Returns false if the entry could not be added due to not being compatible
	 * with the remaining mappings.
	 * 
	 * @param entry
	 * @return
	 */
	//public boolean add(Map.Entry<Node, Node> entry)
	//{
	public boolean add(Node a, Node b)
	{
		//Node a = entry.getKey();
		//Node b = entry.getValue();

		//NodeV av = new NodeV(a, 1);
		//NodeV bv = new NodeV(b, 2);;

		if(a.isVariable() && b.isVariable()) {
			return makeEqual((Var)a, (Var)b);
		} else if(a.isVariable() && b.isConcrete()) {
			return put((Var)a, b);
		} else if(a.isConcrete() && b.isVariable()) {
			return put((Var)b, a);
		} else if(a.isConcrete() && b.isConcrete()) {
			return a.equals(b);
		}
		
		throw new RuntimeException("Should not happen.");
	}

	public void addAll(TwoWayBinding other)
	{
		this.equiMap.getEquivalences().putAll(other.getEquiMap().getEquivalences());
		this.equiMap.getKeyToValue().putAll(other.getEquiMap().getKeyToValue());
	}

	@Override
	public String toString()
	{
		return equiMap.toString();
	}

	
	public boolean isCompatible(TwoWayBinding other)
	{
		return equiMap.isCompatible(other.getEquiMap());
	}
	
	
	public static TwoWayBinding getVarMappingTwoWay(Quad a, Quad b)
	{
		TwoWayBinding result = new TwoWayBinding();

		List<Node> nAs = QuadUtils.quadToList(a);
		List<Node> nBs = QuadUtils.quadToList(b);
		
		for(int i = 0; i < 4; ++i) {
			Node nA = nAs.get(i);
			Node nB = nBs.get(i);
			
			if(!result.add(nA, nB)) {
				return null;
			}
		}
		
		return result;
	}
	
	
	
	public Set<Var> getQueryVariables() {
		return this.equiMap.getEquivalences().asMap().keySet();
	}
	
	/**
	 * 
	 * @return The multimap that maps query variables to the corresponding set of view variables 
	 */
	public ISetMultimap<Var, Var> getVariableMap() {
		return this.equiMap.getEquivalences();
	}
	
	/**
	 * 
	 * @return The map that maps query variables to an optionally associated constant
	 */
	public Map<Var, Node> getConstantMap() {
		return this.equiMap.getKeyToValue();
	}

	public Set<Var> getViewVariablesForQueryVariable(Var queryVar) {
		return this.equiMap.getEquivalences().get(queryVar);
	}
	
	public Set<Var> getViewVariables() {
		return this.equiMap.getEquivalences().getInverse().asMap().keySet();		
	}
}



