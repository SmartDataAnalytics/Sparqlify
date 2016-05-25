package org.aksw.sparqlify.core.jena.functions;

import org.aksw.sparqlify.core.RdfTerm;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class BNode
    extends FunctionBase1
{
    @Override
    public NodeValue exec(NodeValue v) {
        String str = RdfTerm.toLexicalForm(v);
        Node node = NodeFactory.createBlankNode(str);

        NodeValue result = NodeValue.makeNode(node);
        return result;
    }
}
