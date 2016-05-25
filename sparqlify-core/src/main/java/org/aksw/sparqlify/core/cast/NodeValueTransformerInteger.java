package org.aksw.sparqlify.core.cast;

import org.aksw.jena_sparql_api.utils.expr.NodeValueUtils;
import org.aksw.sparqlify.core.TypeToken;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;

public class NodeValueTransformerInteger implements NodeValueTransformer {
    @Override
    public NodeValue transform(NodeValue nodeValue) throws CastException {
        String str = "" + NodeValueUtils.getValue(nodeValue);
        TypeMapper tm = TypeMapper.getInstance();

        String typeName = TypeToken.Int.toString();
        // String typeName = XSD.integer.toString();
        RDFDatatype dt = tm.getSafeTypeByName(typeName);

        Node node = NodeFactory.createLiteral(str, dt);
        NodeValue result = NodeValue.makeNode(node);
        return result;
    }
}