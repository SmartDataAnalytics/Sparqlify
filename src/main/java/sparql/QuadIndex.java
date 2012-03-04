package sparql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;


class HashIndex<T>
{
	public HashIndex(int numColumns)
	{
	}
	
	public void addIndex(int ... columns)
	{
	}
	
	public List<T> lookup(Object ...keys)
	{
		return null;
	}
}

/**
 * Indexes quads for efficient lookups of compatible mappings.
 * 
 * The basic idea is:
 *   For each 'column' (graph subject predicate object) make a map from
 *   
 * 
 * @author raven
 *
 */
public class QuadIndex
{
	private List<Map<Node, Quad>> constToPattern = new ArrayList<Map<Node, Quad>>();
	
	public void add(Quad quad)
	{
	}

}
