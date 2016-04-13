package org.aksw.sparqlify.trash;

import java.util.Map;

import org.aksw.commons.collections.MapUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.graph.NodeTransform;

public class RenamerNodes implements NodeTransform
{
    private final Map<Node, Node> map;

    public RenamerNodes(Map<Node, Node> map)
    {
        this.map = map;
    }

    @Override
    public final Node apply(Node node)
    {
        return MapUtils.getOrElse(map, node, node);
    }
}