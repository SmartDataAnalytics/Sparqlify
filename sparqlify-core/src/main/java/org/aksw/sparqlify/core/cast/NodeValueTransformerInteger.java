package org.aksw.sparqlify.core.cast;

import org.aksw.sparqlify.core.TypeToken;
import org.aksw.sparqlify.expr.util.NodeValueUtilsSparqlify;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;

public class NodeValueTransformerInteger implements NodeValueTransformer {
    @Override
    public NodeValue transform(NodeValue nodeValue) throws CastException {
        String str = "" + NodeValueUtilsSparqlify.getValue(nodeValue);
        TypeMapper tm = TypeMapper.getInstance();

        String typeName = TypeToken.Int.toString();
        // String typeName = XSD.integer.toString();
        RDFDatatype dt = tm.getSafeTypeByName(typeName);

        Node node = NodeFactory.createLiteral(str, dt);
        NodeValue result = NodeValue.makeNode(node);
        return result;
    }
}