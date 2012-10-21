package functions;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase2;

public class TypedLiteral
	extends FunctionBase2
{
	@Override
	public NodeValue exec(NodeValue value, NodeValue datatype) {
		String v = value.asUnquotedString();
		String d = datatype.asUnquotedString();
		
		RDFDatatype dt = TypeMapper.getInstance().getSafeTypeByName(d);
		Node node = Node.createLiteral(v, dt);
		
		NodeValue result = NodeValue.makeNode(node);

		return result;
	}
}
