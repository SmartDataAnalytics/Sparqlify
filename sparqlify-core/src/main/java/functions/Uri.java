package functions;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class Uri
	extends FunctionBase1
{
	@Override
	public NodeValue exec(NodeValue v) {
		String str = RdfTerm.toLexicalForm(v);
		Node uriStr = Node.createURI(str);
		
		NodeValue result = NodeValue.makeNode(uriStr);
		return result;
	}
}
