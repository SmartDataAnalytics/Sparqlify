package mapping;

import java.util.Map;

import org.aksw.commons.collections.MapUtils;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.graph.NodeTransform;

public class RenamerNodes implements NodeTransform
{
    private final Map<Node, Node> map;
    
    public RenamerNodes(Map<Node, Node> map)
    {
    	this.map = map;
    }
    
    public final Node convert(Node node)
    {
    	return MapUtils.getOrElse(map, node, node);
    }
}