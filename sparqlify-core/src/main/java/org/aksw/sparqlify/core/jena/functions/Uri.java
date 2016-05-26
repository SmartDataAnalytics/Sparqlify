package org.aksw.sparqlify.core.jena.functions;

import org.aksw.jena_sparql_api.views.RdfTerm;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase1;

public class Uri
    extends FunctionBase1
{
    @Override
    public NodeValue exec(NodeValue v) {
        String str = RdfTerm.toLexicalForm(v);
        Node uriStr = NodeFactory.createURI(str);

        NodeValue result = NodeValue.makeNode(uriStr);
        return result;
    }
}
