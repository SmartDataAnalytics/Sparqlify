package org.aksw.sparqlify.core.jena.functions;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.FunctionBase2;

public class TypedLiteral
    extends FunctionBase2
{
    @Override
    public NodeValue exec(NodeValue value, NodeValue datatype) {
        String v = value.asUnquotedString();
        String d = datatype.asUnquotedString();

        RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(d);
        Node node = NodeFactory.createLiteral(v, dt);

        NodeValue result = NodeValue.makeNode(node);

        return result;
    }
}
