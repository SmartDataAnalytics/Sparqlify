package sparql;

import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;

/**
 * 
 * 
 * A Quad Pattern signature would be the set of QuadSignatures.
 * 
 * A quad signature associates either a variable symbol or a set of (positive and negated) values
 * with each term in the quad.
 * 
 * The idea behind this is to quickly identify compatible patterns.
 * Given a pattern such as
 * { ?s ?p ?o . Filter(?s = Person, ?p != subClass && ?p != type). }
 * 
 * The signature would be
 * (+[person], -[subClassOf, type], var1)
 *
 * 
 * 
 * @author raven
 *
 */
public class QuadSignature
		implements IPatternSignature
{
	private List<Node>	nodes;

	public QuadSignature(List<Node> nodes)
	{
		this.nodes = nodes;
	}

	@Override
	public Set<Node> getVarsMentioned()
	{
		return PatternUtils.getVariables(nodes);
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodes == null) ? 0 : nodes.hashCode());
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
		QuadSignature other = (QuadSignature) obj;
		if (nodes == null) {
			if (other.nodes != null)
				return false;
		} else if (!nodes.equals(other.nodes))
			return false;
		return true;
	}
	
	
}