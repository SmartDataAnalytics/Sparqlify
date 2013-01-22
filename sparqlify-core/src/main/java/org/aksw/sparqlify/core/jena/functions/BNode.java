package org.aksw.sparqlify.core.jena.functions;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.AnonId;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

public class BNode
	extends FunctionBase1
{
	@Override
	public NodeValue exec(NodeValue v) {
		String str = RdfTerm.toLexicalForm(v);
		Node node = Node.createAnon(new AnonId(str));
		
		NodeValue result = NodeValue.makeNode(node);
		return result;
	}
}
